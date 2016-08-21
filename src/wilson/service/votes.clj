(ns wilson.service.votes
  (:require [ring.util.http-response :refer :all]
            [ring.util.http-predicates :refer [ok?]]
            [wilson.helpers :refer :all]
            [wilson.spec :as ws]
            [wilson.service.items :as is]
            [wilson.database.items :as idb]
            [wilson.database.votes :as vdb]))

(def vote-default-values {::ws/up nil})

(defn- prep-vote [vote iid]
  (merge vote-default-values vote {::ws/iid iid}))

(defn- vote-on-item
  "Takes an item and a vote and updates the scores accordingly."
  [item vote]
  (let [ups   (+ (::ws/ups item)
                 (tf->10 (::ws/up vote)))
        n     (+ (::ws/n item)
                 (tf->10 (some? (::ws/up vote))))
        item' (assoc item ::ws/ups ups
                          ::ws/n n
                          ::ws/updated (date))]
    (is/recalc-scores item')))

(defn- undo-vote
  "Removes the influence of the vote from the item.
   Does not recalculate the score!
   Meant to be used in conjunction with `vote-on-item`."
  [item vote]
  (let [ups   (- (::ws/ups item)
                 (tf->10 (::ws/up vote)))
        n     (- (::ws/n item)
                 (tf->10 (some? (::ws/up vote))))]
    (assoc item ::ws/ups ups
                ::ws/n n)))

(defn post-votes! [iid vote]
  (let [{item :body :as res} (is/get-item iid)]
    (if (not (ok? res))
      res
      (let [{vid :id :as vote}
            (-> vote
                (prep-vote iid)
                (vdb/save-vote!))]
        (-> item
            (vote-on-item vote)
            (idb/patch-item!))
        (created (str base-url "/votes/" vid)
                 vote)))))

(defn get-votes [iid]
  (let [res (is/get-item iid)]
    (if (not (ok? res))
      res
      (ok (vdb/fetch-votes iid)))))

(defn get-all-votes []
  (ok (vdb/fetch-all-votes)))

(defn get-vote [vid]
  (if-let [vote (vdb/fetch-vote vid)]
    (ok vote)
    (not-found)))

(defn patch-vote! [vid new-vote]
  (let [res (get-vote vid)]
    (if (not (ok? res))
      res
      (let [{:keys [::ws/iid] :as vote} (:body res)
            res (is/get-item iid)]
        (if (not (ok? res))
          res
          (let [item  (:body res)
                vote' (merge vote new-vote)]
            (-> item
                (undo-vote vote)
                (vote-on-item vote')
                (idb/patch-item!))
            (vdb/patch-vote! vote')
            (ok vote')))))))
