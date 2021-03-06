(ns wilson.database.common
  (:require [environ.core :refer [env]]
            [rethinkdb.query :as r]
            [wilson.common :refer :all]))

(def db-name (or (env :db-name)
                 (throw (ex-info "No DB name specified!"))))
(def db-url (java.net.URI. (or (env :db-url)
                               (throw (ex-info "No DB URL specified!")))))
(def db-host (.getHost db-url))
(def db-port (.getPort db-url))

(defmacro with-conn [[name] & forms]
  `(with-open [~name (r/connect :host db-host
                                :port db-port
                                :db   db-name)]
    ~(cons 'do forms)))

(defn create-timestamped [x]
  (let [now (date)]
    (assoc x :created now
             :updated now)))

(defn timestamp [x]
  (assoc x :updated (date)))
