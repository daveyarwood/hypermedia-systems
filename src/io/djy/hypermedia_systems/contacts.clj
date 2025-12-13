(ns io.djy.hypermedia-systems.contacts
  (:require
    [clojure.string                   :as str]
    [io.djy.hypermedia-systems.layout :as layout]
    [ring.util.response               :as res]))

;; If this were a real system, we'd be querying a database.
(def fake-contacts
  (atom
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
      :email      "seraphina.h@example.com"}]))

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
            (filter matches? @fake-contacts)]
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
  [{:keys [query-params flash]}]
  (let [{:strs [q]} query-params]
    (layout/page
      [:h1 "Contacts"]
      (when flash
        (list
          [:hr]
          [:em {:class "flash"} flash]
          [:hr]))
      (search-form q)
      [:hr]
      (contacts-table q)
      [:hr]
      [:p [:a {:href "/contacts/new"} "Add Contact"]])))

(defn new-contact-form
  [& [{:keys [email first-name last-name phone errors]}]]
  (layout/page
    [:h1 "New Contact"]
    [:form {:action "/contacts/new" :method "post"}
     [:fieldset
      [:legend "Contact Values"]
      [:p
       [:label {:for "email"} "Email"]
       [:input
        {:id          "email"
         :name        "email"
         :type        "email"
         :placeholder "Email"
         :value       (or email "")}]
       [:span {:class "error"} (:email errors)]]
      [:p
       [:label {:for "first-name"} "First Name"]
       [:input
        {:id          "first-name"
         :name        "first-name"
         :type        "text"
         :placeholder "First Name"
         :value       (or first-name "")}]
       [:span {:class "error"} (:first-name errors)]]
      [:p
       [:label {:for "last-name"} "Last Name"]
       [:input
        {:id          "last-name"
         :name        "last-name"
         :type        "text"
         :placeholder "Last Name"
         :value       (or last-name "")}]
       [:span {:class "error"} (:last-name errors)]]
      [:p
       [:label {:for "phone"} "Phone"]
       [:input
        {:id          "phone"
         :name        "phone"
         :type        "phone"
         :placeholder "Phone"
         :value       (or phone "")}]
       [:span {:class "error"} (:phone errors)]]
      [:button "Save"]]]
    [:p [:a {:href "/contacts"} "Back"]]))

(defn new-contact!
  [{:keys [form-params]}]
  (let [{:strs [email first-name last-name phone]} form-params
        errors  (cond-> {}
                  (or (nil? email) (str/blank? email))
                  (assoc :email "Email is required.")
                  (or (nil? first-name) (str/blank? first-name))
                  (assoc :first-name "First name is required.")
                  (or (nil? last-name) (str/blank? last-name))
                  (assoc :last-name "Last name is required.")
                  (or (nil? phone) (str/blank? phone))
                  (assoc :phone "Phone is required."))
        contact {:email      email
                 :first-name first-name
                 :last-name  last-name
                 :phone      phone}]
    (if (seq errors)
      ;; Render the form again with errors
      (new-contact-form (merge contact {:errors errors}))
      ;; In a real system, we'd save the new contact to the database here.
      (do
        (swap!
          fake-contacts
          conj
          (merge contact {:id (inc (count @fake-contacts))}))
        (merge
          (res/redirect "/contacts" :see-other)
          {:flash "Contact created successfully."})))))
