(ns io.djy.hypermedia-systems.http)

(defn redirect-to [location]
  {:status 301, :headers {"Location" location}})
