(ns io.djy.hypermedia-systems.database
  (:require [clojure.string :as str]))

;; If this were a real system, we'd be querying a database.
(def ^:private fake-contacts
  (atom
    [{"id"         1
      "first-name" "Eleanor"
      "last-name"  "Vance"
      "phone"      "555-0101"
      "email"      "eleanor@example.com"}
     {"id"         2
      "first-name" "Marcus"
      "last-name"  "Thorne"
      "phone"      "555-0102"
      "email"      "marcus.t@example.com"}
     {"id"         3
      "first-name" "Isla"
      "last-name"  "Finch"
      "phone"      "555-0103"
      "email"      "isla.finch@example.com"}
     {"id"         4
      "first-name" "Jasper"
      "last-name"  "Reed"
      "phone"      "555-0104"
      "email"      "j.reed@example.com"}
     {"id"         5
      "first-name" "Seraphina"
      "last-name"  "Hayes"
      "phone"      "555-0105"
      "email"      "seraphina.h@example.com"}]))

(defn list-contacts
  [& [search]]
  (if search
    (filter
      (fn [contact]
        (some #(str/includes?
                 (str/lower-case (str (get contact %)))
                 (str/lower-case search))
              ["first-name" "last-name" "phone" "email"]))
      @fake-contacts)
    @fake-contacts))

(defn get-contact
  [id]
  (first (filter #(= (get % "id") id) @fake-contacts)))

(defn create-contact!
  [contact]
  (swap!
    fake-contacts
    #(concat % [(merge contact {"id" (inc (count %))})])))

(defn update-contact!
  [id updated-contact]
  (swap!
    fake-contacts
    #(map
       (fn [{contact-id "id" :as existing-contact}]
         (if (= id contact-id)
           (merge existing-contact updated-contact)
           existing-contact))
       %)))

(defn delete-contact!
  [id]
  (swap!
    fake-contacts
    #(filter
       (fn [{contact-id "id"}]
         (not= id contact-id))
       %)))
