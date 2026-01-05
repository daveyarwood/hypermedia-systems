(ns io.djy.hypermedia-systems.layout
  (:require
    [hiccup2.core :as h]
    [hiccup.page  :as p]))

(defn html
  [& content]
  (str (h/html content)))

(defn page
  [& content]
  (p/html5
    {:lang "en"}
    [:head
     [:meta {:charset "utf-8"}]
     [:title "Contact.app"]
     [:link {:rel "stylesheet" :href "/css/style.css"}]
     [:script {:src "/js/htmx-2.0.8.min.js"}]]
    [:body {:hx-boost "true"} content]))
