(ns wilson.resources.items
  (:require [clojure.spec :as s]
            [ring.util.http-response :refer :all]
            [ring.util.http-predicates :refer [ok?]]
            [rethinkdb.query :as r]
            [wilson.helpers :refer :all]
            [wilson.score :refer [score not-average]]
            [wilson.spec :as ws]))

(defn- with-time [x]
  (let [now (date)]
    (merge x {::ws/created now
              ::ws/updated now})))

(def item-default-values {::ws/ups 0
                          ::ws/n 0})

(defn- prep-item [item]
  (with-time
    (merge item-default-values item)))

(defn recalc-scores [item]
  (let [{:keys [::ws/ups ::ws/n]} item]
    (assoc item ::ws/score   (score ups n)
                ::ws/wilson  (not-average ups n))))

(defn post-items! [item]
  (let [item (-> item (prep-item) (recalc-scores))]
    (try
      (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
        (let [{[iid] :generated_keys}
              (-> (r/db "wilson")
                (r/table "items")
                (r/insert item)
                (r/run conn))]
          (created (str base-url "/items/" iid)
                   (assoc item :id iid))))
      (catch Exception e
        (.printStackTrace e)
        (internal-server-error (format "IOException: %s" (.getMessage e)))))))

(defn get-items []
  (try
    (let [items
          (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
            (-> (r/db "wilson")
              (r/table "items")
              (r/get-field :id)
              (r/run conn)))]
      (ok items))
    (catch Exception e
      (.printStackTrace e)
      (internal-server-error (format "IOException: %s" (.getMessage e))))))

(defn get-item [iid]
  (try
    (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
      (let [item (-> (r/db "wilson")
                   (r/table "items")
                   (r/get iid)
                   (r/run conn))]
        (if (some? item)
          (ok item)
          (not-found))))
    (catch Exception e
      (.printStackTrace e)
      (internal-server-error (format "IOException: %s" (.getMessage e))))))

(defn delete-item [iid]
  (let [res (get-item iid)]
    (if (not (ok? res))
      res
      (try
        (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
          (-> (r/db "wilson")
            (r/table "items")
            (r/get iid)
            (r/delete {:durability :hard
                       :return-changes false})
            (r/run conn))
          (-> (r/db "wilson")
            (r/table "votes")
            (r/get-all [iid] {:index "iid"})
            (r/delete {:durability :hard
                       :return-changes false})
            (r/run conn))
          (no-content))
        (catch Exception e
          (.printStackTrace e)
          (internal-server-error (format "IOException: %s" (.getMessage e))))))))
