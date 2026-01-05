(ns io.djy.hypermedia-systems.contacts
  (:require
    [clojure.string                     :as str]
    [io.djy.hypermedia-systems.database :as db]
    [io.djy.hypermedia-systems.layout   :as layout]
    [ring.util.codec                    :as codec]
    [ring.util.response                 :as res]))

(defn- maybe-parse-long
  [x]
  (if (string? x)
    (parse-long x)
    x))

(def ^:private spinner
  [:img
   {:id    "spinner"
    :class "htmx-indicator"
    :src   "/img/spinner.gif"
    :alt   "Loading..."
    :style "width: 30px; height: 30px"}])

(defn count-contacts
  []
  (format "(%d total contacts)" (db/count-contacts)))

(defn- search-form
  [q]
  [:form {:action "/contacts" :method "get" :class "tool-bar"}
   [:label {:for "search"} "Search Term"]
   [:input
    {:id           "search"
     :type         "search"
     :name         "q"
     :value        (or q "")
     :hx-get       "/contacts"
     :hx-trigger   "search, keyup delay:200ms changed"
     :hx-target    "tbody"
     :hx-push-url  "true"
     :hx-indicator "#spinner"}]
   spinner
   [:input {:type "submit" :value "Search"}]])

(defn- contact-rows
  [{:keys [query-params]} contacts]
  (let [{:strs [page]} query-params]
    (concat
      (for [{:strs [id first-name last-name phone email]} contacts]
        [:tr
         [:td first-name]
         [:td last-name]
         [:td phone]
         [:td email]
         [:td
          [:a {:href (format "/contacts/%d/edit" id)} "Edit"]
          " "
          [:a {:href (format "/contacts/%d" id)} "View"]
          " "
          [:a
           {:href       "#"
            :hx-delete  (format "/contacts/%d" id)
            :hx-target  "closest tr"
            :hx-confirm "Are you sure you want to delete this contact?"}
           "Delete"]]])
      (when (= db/page-size (count contacts))
        (list
          [:tr]
          [:td {:colspan "5" :style "text-align: center"}
           [:span
            {:hx-target  "closest tr"
             :hx-trigger "revealed"
             :hx-swap    "outerHTML"
             :hx-select  "tbody > tr"
             :hx-get     (str
                           "/contacts?"
                           (-> query-params
                               (merge
                                 {"page"
                                  (str (inc (or (maybe-parse-long page) 1)))})
                               codec/form-encode))}
            [:em "Loading more..."]]])))))

(defn- contacts-table
  [req contacts]
  [:table
   [:thead
    [:tr
     [:th "First Name"]
     [:th "Last Name"]
     [:th "Phone"]
     [:th "Email"]]]
   [:tbody
    (contact-rows req contacts)]])

(defn list-contacts
  [{:keys [headers query-params flash] :as req}]
  (let [{:strs [hx-trigger]} headers
        {:strs [q page]}     query-params
        contacts             (db/list-contacts q page)]
    (if (= "search" hx-trigger)
      ;; Return only the table rows for htmx "active search" requests
      (layout/html
        (contact-rows req contacts))
      ;; Otherwise, return the entire page
      (layout/page
        [:h1 "Contacts"]
        (when flash
          (list
            [:hr]
            [:em {:class "flash"} flash]
            [:hr]))
        (search-form q)
        [:hr]
        (contacts-table req contacts)
        [:hr]
        [:p
         [:a {:href "/contacts/new"} "Add Contact"]
         " "
         [:span
          {:hx-get "/contacts/count" :hx-trigger "revealed"}
          spinner]]))))

(defn view-contact
  [{:keys [route-params flash]}]
  (let [id
        (maybe-parse-long (:id route-params))

        {:strs [first-name last-name email phone] :as contact}
        (db/get-contact id)]
    (if contact
      (layout/page
        [:h1 (format "%s %s" first-name last-name)]
        (when flash
          (list
            [:hr]
            [:em {:class "flash"} flash]
            [:hr]))
        [:div
         [:div "Phone: " phone]
         [:div "Email: " email]]
        [:p [:a {:href (format "/contacts/%d/edit" id)} "Edit"]]
        [:p [:a {:href "/contacts"} "Back"]])
      (res/not-found
        (layout/page
          [:h1 "Contact Not Found"]
          [:p "The contact you are looking for does not exist."]
          [:p [:a {:href "/contacts"} "Back"]])))))

(defn- validate-email-impl
  [id email]
  (let [existing-contact (->> (db/list-contacts nil nil)
                              (filter
                                (fn [{contact-id "id" contact-email "email"}]
                                  (and (= email contact-email)
                                       (not= id contact-id))))
                              first)]
    (if existing-contact
      "Email is already in use."
      "")))

(defn validate-email
  [{:keys [route-params query-params]}]
  (let [id               (maybe-parse-long (:id route-params))
        {:strs [email]}  query-params]
    (validate-email-impl id email)))

(defn- contact-form
  [action & [{:strs [id email first-name last-name phone]
              :keys [errors]}]]
  [:form {:action action :method "post"}
   [:fieldset
    [:legend "Contact Values"]
    [:p
     [:label {:for "email"} "Email"]
     [:input
      {:id          "email"
       :name        "email"
       :type        "email"
       :placeholder "Email"
       :value       (or email "")
       :hx-get      (format "/contacts/%s/validate-email" (or id "new"))
       :hx-target   "next .error"
       :hx-trigger  "change, keyup delay:200ms changed"}]
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
    [:button "Save"]]])

(defn new-contact-form
  [& [form-state]]
  (layout/page
    [:h1 "New Contact"]
    (contact-form "/contacts/new" form-state)
    [:p [:a {:href "/contacts"} "Back"]]))

(defn- validate-contact
  [{:strs [id email first-name last-name phone]}]
  (let [email-error (validate-email-impl id email)]
    (cond-> {}
      (str/blank? email)
      (assoc :email "Email is required.")
      (seq email-error)
      (assoc :email email-error)
      (str/blank? first-name)
      (assoc :first-name "First name is required.")
      (str/blank? last-name)
      (assoc :last-name "Last name is required.")
      (str/blank? phone)
      (assoc :phone "Phone is required."))))

(defn new-contact!
  [{:keys [form-params]}]
  (let [contact (select-keys
                  form-params
                  ["email" "first-name" "last-name" "phone"])
        errors  (validate-contact contact)]
    (if (seq errors)
      ;; Render the form again with errors
      (new-contact-form (merge contact {:errors errors}))
      ;; Save the new contact and redirect to the List Contacts page
      (do
        (db/create-contact! contact)
        (merge
          (res/redirect "/contacts" :see-other)
          {:flash "Contact created successfully."})))))

(defn edit-contact-form
  [id & [form-state]]
  (let [id         (maybe-parse-long id)
        form-state (or form-state (db/get-contact id))]
    (layout/page
      [:h1 "Edit Contact"]
      (contact-form (format "/contacts/%d/edit" id) form-state)
      [:button
       {:id          "delete-button"
        :hx-delete   (format "/contacts/%d" id)
        :hx-target   "body"
        :hx-push-url "true"
        :hx-confirm  "Are you sure you want to delete this contact?"}
       "Delete Contact"]
      [:p [:a {:href "/contacts"} "Back"]])))

(defn edit-contact!
  [{:keys [route-params form-params]}]
  (let [id      (maybe-parse-long (:id route-params))
        contact (merge
                  (select-keys
                    form-params
                    ["email" "first-name" "last-name" "phone"])
                  {"id" id})
        errors  (validate-contact contact)]
    (if (seq errors)
      ;; Render the form again with errors
      (edit-contact-form id (merge contact {:errors errors}))
      ;; Save the new contact and redirect to the List Contacts page
      (do
        (db/update-contact! id contact)
        (merge
          (res/redirect (format "/contacts/%d" id) :see-other)
          {:flash "Contact updated successfully."})))))

(defn delete-contact!
  [{:keys [headers route-params]}]
  (let [{:strs [hx-trigger]} headers
        {:keys [id]}         route-params
        id                   (maybe-parse-long id)]
    (db/delete-contact! id)
    (if (= "delete-button" hx-trigger)
      ;; "Edit Contact" page:
      ;; Redirect back to the contact list page with a flash message
      (merge
        (res/redirect "/contacts" :see-other)
        {:flash "Contact deleted successfully."})
      ;; "List Contacts" page:
      ;; Replace the contact row with nothing, i.e. remove it
      "")))
