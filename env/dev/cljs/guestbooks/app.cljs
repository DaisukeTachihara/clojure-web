(ns ^:dev/once guestbooks.app
  (:require
   [devtools.core :as devtools]
   [guestbooks.core :as core]))

(enable-console-print!)

(println "loading env/dev/cljs/guestbook/app.cljs")

(devtools/install!)

(core/init!)