(ns wilson.handler.votes
  (:require [wilson.helpers :refer :all]
            [wilson.spec :as ws]
            [wilson.handler.common :refer [update-body-if-success]]
            [wilson.service.votes :as vs]))

(defn- votes->resource [res]
  (update-body-if-success res
    (fn [vote-ids]
      (map #(str base-url "/votes/" %) vote-ids))))

(defn- vote->resource [res]
  (update-body-if-success res
    #(assoc % ::ws/iid
      (str base-url "/items/" (::ws/iid %)))))

(defn get-votes [iid]
  (votes->resource (vs/get-votes iid)))

(defn post-votes! [iid vote]
  (vote->resource (vs/post-votes! iid vote)))

(defn get-all-votes []
  (votes->resource (vs/get-all-votes )))

(defn get-vote [vid]
  (vote->resource (vs/get-vote vid)))

(defn patch-vote! [vid vote]
  (vote->resource (vs/patch-vote! vid vote)))
