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
       (if (= "new" (:id route-params))
         (contacts/new-contact-form)
         (contacts/view-contact req)))
  (DELETE "/contacts/:id" [id] (contacts/delete-contact! id))
  (GET "/contacts/:id/edit" [id] (contacts/edit-contact-form id))
  (POST "/contacts/:id/edit" req (contacts/edit-contact! req))
  (POST "/contacts/new" req (contacts/new-contact! req))
  (route/not-found "Page not found"))

(def handler
  (-> app
      (wrap-resource "public")
      wrap-params
      wrap-flash
      wrap-session))

(defn -main
  [& [port]]
  (run-jetty
    handler
    {:port (if port (Integer/parseInt port) 3000)}))
