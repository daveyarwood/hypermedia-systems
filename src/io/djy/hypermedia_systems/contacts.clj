(ns io.djy.hypermedia-systems.contacts
  (:require
    [clojure.string                   :as str]
    [io.djy.hypermedia-systems.layout :as layout]))

;; If this were a real system, we'd be querying a database.
(def fake-contacts
  [{:id         1
    :first-name "Eleanor"
    :last-name  "Vance"
    :phone      "555-0101"
    :email      "eleanor@example.com"}
   {:id         2
    :first-name "Marcus"
    :last-name  "Thorne"
    :phone      "555-0102"
    :email      "marcus.t@example.com"}
   {:id         3
    :first-name "Isla"
    :last-name  "Finch"
    :phone      "555-0103"
    :email      "isla.finch@example.com"}
   {:id         4
    :first-name "Jasper"
    :last-name  "Reed"
    :phone      "555-0104"
    :email      "j.reed@example.com"}
   {:id         5
    :first-name "Seraphina"
    :last-name  "Hayes"
    :phone      "555-0105"
    :email      "seraphina.h@example.com"}])

(defn- search-form
  [q]
  [:form {:action "/contacts" :method "get"}
   [:label {:for "search"} "Search Term"]
   [:input {:id "search" :type "search" :name "q" :value (or q "")}]
   [:input {:type "submit" :value "Search"}]])

(defn- contacts-table
  [q]
  (let [matches? (fn [contact]
                   (or (empty? q)
                       (some #(str/includes?
                                (str/lower-case (str (contact %)))
                                (str/lower-case q))
                             [:first-name :last-name :phone :email])))]
    [:table
     [:thead
      [:tr
       [:th "First Name"]
       [:th "Last Name"]
       [:th "Phone"]
       [:th "Email"]]]
     [:tbody
      (for [{:keys [id first-name last-name phone email]}
            (filter matches? fake-contacts)]
        [:tr
         [:td first-name]
         [:td last-name]
         [:td phone]
         [:td email]
         [:td
          [:a {:href (format "/contacts/%d/edit" id)} "Edit"]
          " "
          [:a {:href (format "/contacts/%d" id)} "View"]]])]]))

(defn list-contacts
  [{:keys [query-params]}]
  (let [{:strs [q]} query-params]
    (str
      (layout/page
        [:h1 "Contacts"]
        [:hr]
        (search-form q)
        [:hr]
        (contacts-table q)
        [:hr]
        [:p [:a {:href "/contacts/new"} "Add Contact"]]))))
