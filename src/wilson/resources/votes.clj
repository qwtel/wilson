(ns wilson.resources.votes
  (:require [clojure.spec :as s]
            [ring.util.http-response :refer :all]
            [ring.util.http-predicates :refer [ok?]]
            [rethinkdb.query :as r]
            [wilson.helpers :refer :all]
            [wilson.score :refer :all]
            [wilson.spec :as ws]
            [wilson.resources.items :refer [get-item recalc-scores]]))

(defn- with-time [x]
  (let [now (date)]
    (merge x {::ws/created now
              ::ws/updated now})))

(def vote-default-values {::ws/up nil})

(defn- prep-vote [vote iid]
  (with-time
    (merge vote-default-values vote
      {::ws/iid iid})))

(defn- update-vote [vote new-vote]
  (merge vote new-vote {::ws/updated (date)}))

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
    (recalc-scores item')))

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
  (let [{item :body :as res} (get-item iid)]
    (if (not (ok? res))
      res
      (let [vote  (prep-vote vote iid)
            item' (vote-on-item item vote)]
        (try
          (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
            (-> (r/db "wilson")
              (r/table "items")
              (r/insert item' {:conflict :replace
                               :durability :hard})
              (r/run conn))
            (let [{[vid] :generated_keys}
                  (-> (r/db "wilson")
                    (r/table "votes")
                    (r/insert vote)
                    (r/run conn))]
              (created (str base-url "/votes/" vid)
                       (assoc vote :id vid))))
          (catch Exception e
            (.printStackTrace e)
            (internal-server-error (format "IOException: %s" (.getMessage e)))))))))

(defn get-votes [iid]
  (try
    (let [votes
          (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
            (-> (r/db "wilson")
              (r/table "votes")
              (r/get-all [iid] {:index "iid"})
              (r/get-field :id)
              (r/run conn)))]
      (ok votes))
    (catch Exception e
      (.printStackTrace e)
      (internal-server-error (format "IOException: %s" (.getMessage e))))))

(defn get-all-votes []
  (try
    (let [votes
          (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
            (-> (r/db "wilson")
              (r/table "votes")
              (r/get-field :id)
              (r/run conn)))]
      (ok votes))
    (catch Exception e
      (.printStackTrace e)
      (internal-server-error (format "IOException: %s" (.getMessage e))))))

(defn get-vote [vid]
  (try
    (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
      (let [vote (-> (r/db "wilson")
                   (r/table "votes")
                   (r/get vid)
                   (r/run conn))]
        (if (some? vote)
          (ok vote)
          (not-found))))
    (catch Exception e
      (.printStackTrace e)
      (internal-server-error (format "IOException: %s" (.getMessage e))))))

(defn patch-vote [vid new-vote]
  (let [res (get-vote vid)]
    (if (not (ok? res))
      res
      (let [vote (:body res)
            res (get-item (::ws/iid vote))]
        (if (not (ok? res))
          res
          (let [item (:body res)
                vote' (update-vote vote new-vote)
                item' (-> item (undo-vote vote)
                               (vote-on-item vote'))]
            (try
              (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
                (-> (r/db "wilson")
                  (r/table "items")
                  (r/insert item' {:conflict :replace
                                   :durability :hard})
                  (r/run conn))
                (-> (r/db "wilson")
                  (r/table "votes")
                  (r/insert vote' {:conflict :replace
                                   :durability :hard})
                  (r/run conn))
                (ok vote'))
              (catch Exception e
                (.printStackTrace e)
                (internal-server-error (format "IOException: %s" (.getMessage e)))))))))))
