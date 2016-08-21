(ns wilson.service.items
  (:require [ring.util.http-response :refer :all]
            [ring.util.http-predicates :refer [ok?]]
            [wilson.helpers :refer :all]
            [wilson.score :refer [score not-average]]
            [wilson.spec :as ws]
            [wilson.database.items :as idb]
            [wilson.database.votes :as vdb]))

(def item-default-values {::ws/ups 0
                          ::ws/n 0})

(defn- prep-item [item]
  (merge item-default-values item))

(defn recalc-scores [item]
  (let [{:keys [::ws/ups ::ws/n]} item]
    (assoc item ::ws/score  (score ups n)
                ::ws/wilson (not-average ups n))))

(defn post-items! [item]
  (let [{:keys [id] :as item}
        (-> item
            (prep-item)
            (recalc-scores)
            (idb/save-item!))]
    (created (str base-url "/items/" id)
             (assoc item :id id))))

(defn get-items []
  (ok (idb/fetch-items)))

(defn get-item [iid]
  (if-let [item (idb/fetch-item iid)]
    (ok item)
    (not-found)))

(defn delete-item! [iid]
  (let [res (get-item iid)]
    (if (not (ok? res))
      res
      (do
        (vdb/remove-votes-for! iid)
        (idb/remove-item! iid)
        (no-content)))))
