(ns io.djy.hypermedia-systems.archive
  "A fake archiver implementation for the book's purposes.

   The idea is that there could be many, many contacts to include in the
   archive, and the content presumably needs to be generated live (perhaps
   different contacts are included based on who is requesting the archive?),
   and that has implications on the UX we are going to provide.

   Reference Python implementation:
   https://github.com/bigskysoftware/contact-app/blob/master/contacts_model.py"
  (:require [io.djy.hypermedia-systems.database :as db]
            [io.djy.hypermedia-systems.layout   :as layout]
            [jsonista.core                      :as json]
            [ring.util.response                 :as res]))

(defn- contacts-archive
  []
  (json/write-value-as-string
    (db/list-contacts nil nil)))

(defn new-archiver
  "Returns an archiver instance, which is a stateful object that is in some
   state of creating the archive. Includes functions to check the status and
   progress, start archiving, and reset the state."
  []
  (let [initial-state {:status :waiting, :progress 0}
        state         (atom initial-state)]
    {:current-state #(deref state)
     :reset!        #(reset! state initial-state)
     :run!          #(future
                       (swap! state assoc :status :running)
                       ;; Simulate archiving process
                       (dotimes [_ 10]
                         (Thread/sleep (rand-int 1000))
                         (swap! state update :progress + 10))
                       (swap! state assoc
                              :status :complete
                              :result (contacts-archive)))}))

(defn archive-ui
  [{:keys [archiver]}]
  (let [{:keys [current-state]}   archiver
        {:keys [status progress]} (current-state)]
    [:div {:hx-target "this" :hx-swap "outerHTML"}
     (case status
       :waiting
       [:button
        {:hx-post "/contacts/archive"}
        "Download Contact Archive"]

       :running
       [:div
        {:hx-get     "/contacts/archive"
         :hx-trigger "load delay:500ms"}
        "Creating Archive..."
        [:div
         {:class "progress"}
         [:div
          {:id            "archive-progress"
           :class         "progress-bar"
           :role          "progressbar"
           :aria-valuenow (str progress)
           :style         (str "width:" progress "%")}]]]

       :complete
       [:div
        [:a
         {:hx-boost "false"
          :href     "/contacts/archive/file"}
         "Archive Ready! Click here to download. ↓"]
        [:button
         {:hx-delete "/contacts/archive/file"}
         "Clear Download"]])]))

(defn start-archiver!
  [{:keys [archiver] :as req}]
  (let [{:keys [run!]} archiver]
    (run!)
    (Thread/sleep 100)
    (layout/html (archive-ui req))))

(defn download-archive
  [{:keys [archiver]}]
  (let [{:keys [current-state]} archiver
        {:keys [result]}        (current-state)]
    (if result
      {:status  200
       :headers {"Content-Type"
                 "application/json"

                 "Content-Disposition"
                 "attachment; filename=\"contacts.json\""}
       :body    result}
      (res/not-found "Archive file not found"))))

(defn clear-download!
  [{:keys [archiver] :as req}]
  (let [{:keys [reset!]} archiver]
    (reset!)
    (layout/html (archive-ui req))))
