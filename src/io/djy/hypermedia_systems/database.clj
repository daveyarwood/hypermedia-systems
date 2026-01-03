(ns io.djy.hypermedia-systems.database
  (:require [clojure.data.csv :as csv]
            [clojure.java.io  :as io]
            [clojure.string   :as str]))

(defn- simulate-db-delay
  []
  (Thread/sleep 500))

;; If this were a real system, we'd be querying a database.
(def ^:private fake-contacts
  (atom
    (with-open [rdr (io/reader (io/resource "contacts.csv"))]
      (let [[header & rows] (csv/read-csv rdr)]
        (doall
          (map #(as-> % ?
                  (zipmap header ?)
                  (update ? "id" parse-long))
               rows))))))

(def page-size 10)

(defn list-contacts
  [query page]
  (simulate-db-delay)
  (cond->> @fake-contacts
    query
    (filter
      (fn [contact]
        (some #(str/includes?
                 (str/lower-case (str (get contact %)))
                 (str/lower-case query))
              ["first-name" "last-name" "phone" "email"])))
    page
    (drop (* page-size (dec (parse-long page))))

    true
    (take page-size)))

(defn get-contact
  [id]
  (simulate-db-delay)
  (first (filter #(= (get % "id") id) @fake-contacts)))

(defn create-contact!
  [contact]
  (simulate-db-delay)
  (swap!
    fake-contacts
    #(concat % [(merge contact {"id" (inc (count %))})])))

(defn update-contact!
  [id updated-contact]
  (simulate-db-delay)
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
  (simulate-db-delay)
  (swap!
    fake-contacts
    #(filter
       (fn [{contact-id "id"}]
         (not= id contact-id))
       %)))
