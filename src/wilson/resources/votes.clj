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

(defn- prep-vote [parsed]
  (with-time
    (merge vote-default-values parsed)))

(defn post-votes! [{{:keys [iid]} :params
                    :keys [body]
                    :as req}]
  (let [parsed (s/conform ::ws/vote body)]
    (if (= parsed ::s/invalid)
      (bad-request (s/explain-data ::ws/vote body))
      (let [{item :body :as res} (get-item req)]
        (if (not (ok? res))
          res
          (let [vote  (-> parsed (prep-vote) (assoc ::ws/iid iid))
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
                (internal-server-error (format "IOException: %s" (.getMessage e)))))))))))

(defn get-votes [{{:keys [iid]} :params}]
  (try
    (let [votes
          (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
            (-> (r/db "wilson")
              (r/table "votes")
              (r/get-all [iid] {:index "iid"})
              ; (r/eq-join :id (r/table "votes") {:index :part-id})
              (r/get-field :id)
              (r/run conn)))]
      (ok votes))
    (catch Exception e
      (.printStackTrace e)
      (internal-server-error (format "IOException: %s" (.getMessage e))))))

(defn get-vote [{{:keys [vid]} :params}]
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
