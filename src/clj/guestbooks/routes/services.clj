(ns guestbooks.routes.services
  (:require
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [reitit.ring.coercion :as coercion]
   [reitit.coercion.spec :as spec-coercion]
   [reitit.ring.middleware.muuntaja :as muutanja]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.parameters :as parameters]
   [guestbooks.messages :as msg]
   [guestbooks.middleware :as middleware]
   [guestbooks.middleware.formats :as formats]
   [ring.util.http-response :as response]))

(defn service-routes []
  ["/api"
   {:middleware [middleware/wrap-formats
                 ;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negosiation
                 muutanja/format-negotiate-middleware
                 ;; encoding response body
                 muutanja/format-response-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; decoding request body
                 muutanja/format-request-middleware
                 ;; coercing response body
                 coercion/coerce-response-middleware
                 ;; coercing request body
                 coercion/coerce-request-middleware
                 ;; multipart params
                 multipart/multipart-middleware]
    :muuntaja formats/instance
    :coercion spec-coercion/coercion
    :swagger {:id ::api}}
   ["" {:no-doc true}
    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]
    ["/swagger-ui*"
     {:get (swagger-ui/create-swagger-ui-handler
            {:url "/api/swagger.json"})}]]
   ["/messages" {:get
                 {:responses
                  {200
                   {:body ;; Data Spec for response body
                    {:messages
                     [{:id pos-int?
                       :name string?
                       :message string?
                       :timestamp inst?}]}}}
                  :handler
                  (fn [_] (response/ok (msg/message-list)))}}]
   ["/message" {:post
                {:parameters
                 {:body ;; Data Spec for Request body parameters
                  {:name string?
                   :message string?}}
                 :response
                 {200
                  {:body map?}
                  400
                  {:body map?}
                  500
                  {:errors map?}}}
                :handler
                (fn [{:keys [params]}]
                  (try
                    (msg/save-message! params)
                    (response/ok {:status :ok})
                    (catch Exception e
                      (let [{id :guestbooks/error-id
                             errors :errors} (ex-data e)]
                        (case id
                          :validation
                          (response/bad-request {:errors errors})
                          ;;else
                          (response/internal-server-error
                           {:errors
                            {:server-error ["Faild to save message!"]}}))))))}]])