(ns io.djy.hypermedia-systems.layout
  (:require [hiccup.page :as p]))

(defn page
  [& content]
  (p/html5
    {:lang "en"}
    [:head
     [:meta {:charset "utf-8"}]
     [:title "Contact.app"]]
    [:body content]))
