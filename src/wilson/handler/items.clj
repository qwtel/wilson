(ns wilson.handler.items
  (:require [wilson.helpers :refer :all]
            [wilson.spec :as ws]
            [wilson.handler.common :refer [update-body-if-success]]
            [wilson.service.items :as is]))

(defn- items->resource [res]
  (update-body-if-success res
    (fn [item-ids]
      (map #(str base-url "/items/" %) item-ids))))

(defn- item->resource [res] res)

(defn get-items []
  (items->resource (is/get-items)))

(defn post-items! [item]
  (item->resource (is/post-items! item)))

(defn get-item [iid]
  (item->resource (is/get-item iid)))

(defn delete-item! [iid]
  (is/delete-item! iid))
