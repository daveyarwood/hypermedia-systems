(ns io.djy.hypermedia-systems.database
  (:require [clojure.string :as str]))

(defn- simulate-db-delay
  []
  (Thread/sleep 200))

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
      "email"      "seraphina.h@example.com"}
     {"id"         6
      "first-name" "Liam"
      "last-name"  "O'Connor"
      "phone"      "555-0106"
      "email"      "liam.oconnor@example.com"}
     {"id"         7
      "first-name" "Chloe"
      "last-name"  "Dubois"
      "phone"      "555-0107"
      "email"      "chloe.d@example.com"}
     {"id"         8
      "first-name" "Benjamin"
      "last-name"  "Carter"
      "phone"      "555-0108"
      "email"      "ben.carter@example.com"}
     {"id"         9
      "first-name" "Olivia"
      "last-name"  "Chen"
      "phone"      "555-0109"
      "email"      "olivia.chen@example.com"}
     {"id"         10
      "first-name" "Noah"
      "last-name"  "Rodriguez"
      "phone"      "555-0110"
      "email"      "noah.r@example.com"}
     {"id"         11
      "first-name" "Ava"
      "last-name"  "Petrova"
      "phone"      "555-0111"
      "email"      "ava.p@example.com"}
     {"id"         12
      "first-name" "Ethan"
      "last-name"  "Schmidt"
      "phone"      "555-0112"
      "email"      "ethan.s@example.com"}
     {"id"         13
      "first-name" "Mia"
      "last-name"  "Kowalski"
      "phone"      "555-0113"
      "email"      "mia.k@example.com"}
     {"id"         14
      "first-name" "Lucas"
      "last-name"  "Nakamura"
      "phone"      "555-0114"
      "email"      "lucas.n@example.com"}
     {"id"         15
      "first-name" "Sophia"
      "last-name"  "Rossi"
      "phone"      "555-0115"
      "email"      "sophia.rossi@example.com"}
     {"id"         16
      "first-name" "Mason"
      "last-name"  "Kim"
      "phone"      "555-0116"
      "email"      "m.kim@example.com"}
     {"id"         17
      "first-name" "Harper"
      "last-name"  "Singh"
      "phone"      "555-0117"
      "email"      "harper.singh@example.com"}
     {"id"         18
      "first-name" "Logan"
      "last-name"  "van der Berg"
      "phone"      "555-0118"
      "email"      "logan.v@example.com"}
     {"id"         19
      "first-name" "Evelyn"
      "last-name"  "Al-Farsi"
      "phone"      "555-0119"
      "email"      "evelyn.af@example.com"}
     {"id"         20
      "first-name" "Alexander"
      "last-name"  "Müller"
      "phone"      "555-0120"
      "email"      "alex.muller@example.com"}
     {"id"         21
      "first-name" "Amelia"
      "last-name"  "Jensen"
      "phone"      "555-0121"
      "email"      "amelia.j@example.com"}
     {"id"         22
      "first-name" "Jacob"
      "last-name"  "O'Sullivan"
      "phone"      "555-0122"
      "email"      "jacob.os@example.com"}
     {"id"         23
      "first-name" "Emily"
      "last-name"  "Dubois"
      "phone"      "555-0123"
      "email"      "emily.d@example.com"}
     {"id"         24
      "first-name" "Michael"
      "last-name"  "Ivanov"
      "phone"      "555-0124"
      "email"      "michael.i@example.com"}
     {"id"         25
      "first-name" "Abigail"
      "last-name"  "Williams"
      "phone"      "555-0125"
      "email"      "abby.w@example.com"}
     {"id"         26
      "first-name" "Daniel"
      "last-name"  "Garcia"
      "phone"      "555-0126"
      "email"      "daniel.g@example.com"}
     {"id"         27
      "first-name" "Madison"
      "last-name"  "Martinez"
      "phone"      "555-0127"
      "email"      "m.martinez@example.com"}
     {"id"         28
      "first-name" "William"
      "last-name"  "Johnson"
      "phone"      "555-0128"
      "email"      "will.j@example.com"}
     {"id"         29
      "first-name" "Charlotte"
      "last-name"  "Brown"
      "phone"      "555-0129"
      "email"      "charlotte.b@example.com"}
     {"id"         30
      "first-name" "James"
      "last-name"  "Smith"
      "phone"      "555-0130"
      "email"      "james.smith@example.com"}]))

(def page-size 10)

(defn list-contacts
  [{:strs [q page]}]
  (simulate-db-delay)
  (cond->> @fake-contacts
    q
    (filter
      (fn [contact]
        (some #(str/includes?
                 (str/lower-case (str (get contact %)))
                 (str/lower-case q))
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
