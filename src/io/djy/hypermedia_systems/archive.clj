(ns io.djy.hypermedia-systems.archive
  "A fake archiver implementation for the book's purposes.

   The idea is that there could be many, many contacts to include in the
   archive, and the content presumably needs to be generated live (perhaps
   different contacts are included based on who is requesting the archive?),
   and that has implications on the UX we are going to provide.

   Reference Python implementation:
   https://github.com/bigskysoftware/contact-app/blob/master/contacts_model.py"
  (:require [io.djy.hypermedia-systems.database :as db]
            [jsonista.core                      :as json]))

(defn- contacts-archive
  []
  (json/write-value-as-string
    (db/list-contacts nil nil)))

(defn archiver
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
