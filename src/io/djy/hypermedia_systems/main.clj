(ns io.djy.hypermedia-systems.main
  (:require
    [compojure.core     :refer [defroutes GET]]
    [compojure.route    :as    route]
    [ring.adapter.jetty :refer [run-jetty]]))

(defroutes app
  (GET "/" [] "Hello World!")
  (route/not-found "Page not found"))

(defn -main
  [& [port]]
  (run-jetty
    app
    {:port (if port (Integer/parseInt port) 3000)}))
