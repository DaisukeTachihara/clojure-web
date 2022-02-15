(ns guestbooks.messages
  (:require
   [guestbooks.db.core :as db]
   [guestbooks.shared-validation :refer [validate-message]]))

(defn message-list []
  {:messages (vec (db/get-messages))})

(defn save-message! [message]
  (if-let [errors (validate-message message)]
    (throw (ex-info "Message is invalid"
                    {:guestbooks/error-id :validation
                     :errors errors}))
    (db/save-message! message)))

