(ns wilson.database.common
  (:require [rethinkdb.query :as r]
            [wilson.spec :as ws]
            [wilson.helpers :refer :all]))

;; TODO: 12 factor app
(defmacro with-conn [[name] & forms]
  `(with-open [~name (r/connect :host "127.0.0.1" :port 28015 :db "test")]
    ~(cons 'do forms)))

(defn created-timestamped [x]
  (let [now (date)]
    (merge x {::ws/created now
              ::ws/updated now})))

(defn timestamped [x]
  (assoc x ::ws/updated (date)))
