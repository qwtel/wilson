(ns wilson.resources.votes
  (:require [clojure.spec :as s]
            [ring.util.http-response :refer :all]
            [ring.util.http-predicates :refer [ok?]]
            [rethinkdb.query :as r]
            [wilson.helpers :refer :all]
            [wilson.score :refer :all]
            [wilson.spec :as ws]
            [wilson.resources.items :refer [get-item update-item]]))

(defn- with-time [x]
  (let [now (date)]
    (merge x {::ws/created now
              ::ws/updated now})))

(def vote-default-values {::ws/up nil})

(defn- prep-vote [vote]
  (with-time
    (merge vote-default-values vote)))

(defn post-votes! [iid vote]
  (let [{item :body :as res} (get-item iid)]
    (if (not (ok? res))
      res
      (let [vote  (-> vote (prep-vote) (assoc ::ws/iid iid))
            item' (update-item item vote)]
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
              (created (str "/votes/" vid) (assoc vote :id vid))))
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
