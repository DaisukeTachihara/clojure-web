(ns guestbooks.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as dom]
   [ajax.core :refer [GET POST]]
   [re-frame.core :as rf]
   [clojure.string :as string]
   [guestbooks.shared-validation :refer [validate-message]]))

(rf/reg-event-fx
 :app/initialize
 (fn [_]
   {:db {:messages/loading? true}}))

(rf/reg-sub
 :messages/loading?
 (fn [db _]
   (:messages/loading? db)))

(rf/reg-event-db
 :messages/set
 (fn [db [_ messages]]
   (-> db
       (assoc :messages/loading? false
              :messages/list messages))))

(rf/reg-sub
 :messages/list
 (fn [db _]
   (:messages/list db [])))

(rf/reg-event-db
 :messages/add
 (fn [db [_ message]]
   (update db :messages/list conj message)))

(defn get-messages []
  (GET "/api/messages"
    {:headers {"Accept" "application/transit+json"}
     :handler #(rf/dispatch [:messages/set (:messages %)])}))

(defn send-message! [fields errors]
  (if-let [validation-errors (validate-message @fields)]
    (reset! errors validation-errors)
    ;; else
    (POST "/api/message"
      {:params @fields
       :formt :json
       :headers
       {"Accept" "application/transit+json"
        "x-csrf-token" (.-value (.getElementById js/document "token"))}
       :handler (fn [_]
                  (rf/dispatch
                   [:messages/add (-> @fields
                                      (assoc :timestamp (js/Date.)))])
                  (reset! fields nil)
                  (reset! errors nil))
       :error-handler (fn [e]
                        (.log js/console (str e))
                        (reset! errors (-> e :response :errors)))})))

(defn message-list [messages]
  [:ul.messages
   (for [{:keys [timestamp message name]} @messages]
     ^{:key timestamp}
     [:li
      [:time (.toLocaleString timestamp)]
      [:p message]
      [:p " - " name]])])

(defn errors-component [errors id]
  (when-let [error (id @errors)]
    [:div.notification.is-danger (string/join error)]))

(defn message-form []
  (let [fields (r/atom {})
        errors (r/atom nil)]
    (fn []
      [:div
       [errors-component errors :server-error]
       [:div.fields
        [:label.label {:for :name} "Name"]
        [errors-component errors :name]
        [:input.input
         {:type :text
          :name :name
          :on-change #(swap! fields
                             assoc :name (-> % .-target .-value))
          :value (:name @fields)}]]
       [:div.fields
        [:label.label {:for :message} "Message"]
        [errors-component errors :message]
        [:textarea.textarea
         {:name :message
          :value (:message @fields)
          :on-change #(swap! fields
                             assoc :message (-> % .-target .-value))}]]
       [:input.button.is-primary
        {:type :submit
         :value "comment"
         :on-click #(send-message! fields errors)}]])))

(defn home []
  (let [messages (rf/subscribe [:messages/list])]
    (get-messages)
    (fn []
      [:div.content>div.columns.is-centered>div.column.is-two-thirds]
      (if @(rf/subscribe [:messages/loading?])
        [:h3 "Loading Messages..."]
        ;; complete loading
        [:div
         [:div.columns>div.column
          [:h3 "Messages"]
          [message-list messages]]
         [:div.columns>div.column
          [message-form messages]]]))))

(defn ^:dev/after-load mount-components []
  (rf/clear-subscription-cache!)
  (js/console.log "Mounting Components...")
  (dom/render [#'home] (js/document.getElementById "content"))
  (js/console.log "Component Mounted!"))

(defn init! []
  (js/console.log "Initializing App...")
  (rf/dispatch [:app/initialize])
  (get-messages)
  (mount-components))

(js/console.log "guestbooks.core evaluated!")