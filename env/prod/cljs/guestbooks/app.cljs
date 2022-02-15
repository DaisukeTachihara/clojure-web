(ns guestbooks.app
  (:require
   [guestbooks.core :as core]))

(set! *print-fn* (fn [& _]))

(core/init!);; => nil

