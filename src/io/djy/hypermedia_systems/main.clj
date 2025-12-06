(ns io.djy.hypermedia-systems.main
  (:require
    [compojure.core                     :refer [defroutes GET]]
    [compojure.route                    :as    route]
    [io.djy.hypermedia-systems.contacts :as    contacts]
    [ring.adapter.jetty                 :refer [run-jetty]]
    [ring.middleware.params             :refer [wrap-params]]))

(defn- redirect-to [location]
  {:status 301, :headers {"Location" location}})

(defroutes app
  (GET "/" _req (redirect-to "/contacts"))
  (GET "/contacts" req (contacts/list-contacts req))
  (route/not-found "Page not found"))

(def handler
  (wrap-params app))

(defn -main
  [& [port]]
  (run-jetty
    handler
    {:port (if port (Integer/parseInt port) 3000)}))
