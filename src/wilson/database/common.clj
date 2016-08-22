(ns wilson.database.common
  (:require [environ.core :refer [env]]
            [rethinkdb.query :as r]
            [wilson.spec :as ws]
            [wilson.common :refer :all]))

(def db-url (java.net.URI. (env :db-url)))
(def db-host (.getHost db-url))
(def db-port (or (.getPort db-url) 80))
(def db-name (or (env :db-name) "wilson"))

(defmacro with-conn [[name] & forms]
  `(with-open [~name (r/connect :host db-host
                                :port db-port
                                :db   db-name)]
    ~(cons 'do forms)))

(defn created-timestamped [x]
  (let [now (date)]
    (merge x {::ws/created now
              ::ws/updated now})))

(defn timestamped [x]
  (assoc x ::ws/updated (date)))
