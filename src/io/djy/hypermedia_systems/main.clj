(ns io.djy.hypermedia-systems.main
  (:require
    [compojure.core                     :refer [defroutes DELETE GET POST]]
    [compojure.route                    :as    route]
    [io.djy.hypermedia-systems.contacts :as    contacts]
    [ring.adapter.jetty                 :refer [run-jetty]]
    [ring.middleware.flash              :refer [wrap-flash]]
    [ring.middleware.params             :refer [wrap-params]]
    [ring.middleware.resource           :refer [wrap-resource]]
    [ring.middleware.session            :refer [wrap-session]]
    [ring.util.response                 :as    res]))

(defroutes app
  (GET "/" _req (res/redirect "/contacts"))
  (GET "/contacts" req (contacts/list-contacts req))
  (GET "/contacts/:id"
       {:keys [route-params] :as req}
       (condp = (:id route-params)
         "new" (contacts/new-contact-form)
         "count" (contacts/count-contacts)
         (contacts/view-contact req)))
  (DELETE "/contacts/:id" req (contacts/delete-contact! req))
  (GET "/contacts/:id/edit" [id] (contacts/edit-contact-form id))
  (POST "/contacts/:id/edit" req (contacts/edit-contact! req))
  (GET "/contacts/:id/validate-email" req (contacts/validate-email req))
  (POST "/contacts/new" req (contacts/new-contact! req))
  (route/not-found "Page not found"))

(defn- wrap-handle-exceptions
  [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        (println e)
        (format
          "<h1>Internal Server Error</h1><p>An unexpected error occurred:%s</p>"
          (.getMessage e))))))

(def handler
  (-> app
      (wrap-resource "public")
      wrap-params
      wrap-flash
      wrap-session
      wrap-handle-exceptions))

(defn -main
  [& [port]]
  (run-jetty
    handler
    {:port (if port (Integer/parseInt port) 3000)}))
