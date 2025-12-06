(ns io.djy.hypermedia-systems.main
  (:require
    [compojure.core                     :refer [defroutes GET POST]]
    [compojure.route                    :as    route]
    [io.djy.hypermedia-systems.contacts :as    contacts]
    [io.djy.hypermedia-systems.http     :as    http]
    [ring.adapter.jetty                 :refer [run-jetty]]
    [ring.middleware.flash              :refer [wrap-flash]]
    [ring.middleware.params             :refer [wrap-params]]
    [ring.middleware.session            :refer [wrap-session]]))

(defroutes app
  (GET "/" _req (http/redirect-to "/contacts"))
  (GET "/contacts" req (contacts/list-contacts req))
  (GET "/contacts/new" _req (contacts/new-contact-form))
  (POST "/contacts/new" req (contacts/new-contact! req))
  (route/not-found "Page not found"))

(def handler
  (-> app wrap-params wrap-flash wrap-session))

(defn -main
  [& [port]]
  (run-jetty
    handler
    {:port (if port (Integer/parseInt port) 3000)}))
