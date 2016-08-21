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
    (in-db
      (fn [conn] (let [{[iid] :generated_keys}
                       (-> (r/db "wilson")
                         (r/table "items")
                         (r/insert item)
                         (r/run conn))]
                   (created (str base-url "/items/" iid)
                            (assoc item :id iid)))))))

(defn get-items []
  (in-db
    (fn [conn] (ok (-> (r/db "wilson")
                       (r/table "items")
                       (r/get-field :id)
                       (r/run conn))))))

(defn get-item [iid]
  (in-db
    (fn [conn]
      (let [item (-> (r/db "wilson")
                   (r/table "items")
                   (r/get iid)
                   (r/run conn))]
        (if (some? item)
          (ok item)
          (not-found))))))

(defn delete-item [iid]
  (let [res (get-item iid)]
    (if (not (ok? res))
      res
      (in-db
        (fn [conn]
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
          (no-content))))))
