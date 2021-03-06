(ns wilson.service.items
  (:require [ring.util.http-response :refer :all]
            [ring.util.http-predicates :refer [ok?]]
            [wilson.common :refer :all]
            [wilson.score :refer [score not-average]]
            [wilson.database.items :as idb]
            [wilson.database.votes :as vdb]
            [wilson.service.common :refer :all]))

(def item-default-values {:ups 0
                          :n 0})

(defn- prep-item [item]
  (merge item-default-values item))

(defn recalc-scores [item]
  (let [{:keys [ups n]} item]
    (assoc item :score  (score ups n)
                :wilson (not-average ups n))))

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
