(ns wilson.service.fixtures
  (:require [clojure.test :refer :all]
            [wilson.common :refer :all]
            [wilson.spec :as ws]
            [wilson.database.items :as idb]
            [wilson.database.votes :as vdb]))

(defn db-fixture [f]
  (let [items-db (atom {})
        votes-db (atom {})]
    (with-redefs [idb/save-item!
                  (fn [item]
                    (let [id   (uuid)
                          item (assoc item :id id)]
                      (swap! items-db assoc id item)
                      item))

                  idb/fetch-items
                  (fn [] (vals @items-db))

                  idb/fetch-item
                  (fn [iid] (get @items-db iid))

                  idb/remove-item!
                  (fn [iid]
                    (swap! items-db dissoc iid))

                  idb/patch-item!
                  (fn [item] (swap! items-db assoc (:id item) item))

                  vdb/save-vote!
                  (fn [vote]
                    (let [id   (uuid)
                          vote (assoc vote :id id)]
                      (swap! votes-db assoc id vote)
                      vote))

                  vdb/remove-votes-for!
                  (fn [iid]
                    (reset! votes-db
                      (into {}
                        (filter (fn [[_ {iid' ::ws/iid}]] (not= iid iid'))
                                @votes-db))))

                  vdb/fetch-all-votes
                  (fn [] (vals @votes-db))

                  vdb/fetch-votes
                  (fn [iid] (filter (fn [{iid' ::ws/iid}] (= iid iid'))
                                    (vals @votes-db)))

                  vdb/fetch-vote
                  (fn [vid] (get @votes-db vid))

                  vdb/patch-vote!
                  (fn [vote] (swap! votes-db assoc (:id vote) vote))]
      (f))))
