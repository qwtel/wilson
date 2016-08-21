(ns wilson.database.common
  (:require [wilson.spec :as ws]
            [wilson.helpers :refer :all]))

(defn created-timestamped [x]
  (let [now (date)]
    (merge x {::ws/created now
              ::ws/updated now})))

(defn timestamped [x]
  (assoc x ::ws/updated (date)))
