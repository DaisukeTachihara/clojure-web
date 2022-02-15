(ns guestbooks.handler
  (:require
   [guestbooks.middleware :as middleware]
   [guestbooks.layout :refer [error-page]]
   [guestbooks.routes.home :refer [home-routes]]
   [guestbooks.routes.services :refer [service-routes]]
   [reitit.ring :as ring]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.webjars :refer [wrap-webjars]]
   [guestbooks.env :refer [defaults]]
   [mount.core :as mount]
   [reitit.ring.middleware.dev :as dev]))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(mount/defstate init-app
  :start ((or (:init defaults) (fn [])))
  :stop  ((or (:stop defaults) (fn []))))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(mount/defstate app-routes
  :start
  (ring/ring-handler
   (ring/router
    [(home-routes)
     (service-routes)]
    {:reitit.middleware/transform dev/print-request-diffs})
   (ring/routes
    (ring/create-resource-handler
     {:path "/"})
    (wrap-content-type
     (wrap-webjars (constantly nil)))
    (ring/create-default-handler
     {:not-found
      (constantly (error-page {:status 404, :title "404 - Page not found"}))
      :method-not-allowed
      (constantly (error-page {:status 405, :title "405 - Not allowed"}))
      :not-acceptable
      (constantly (error-page {:status 406, :title "406 - Not acceptable"}))}))))

(defn app []
  #_{:clj-kondo/ignore [:unresolved-symbol]}
  (middleware/wrap-base #'app-routes))
