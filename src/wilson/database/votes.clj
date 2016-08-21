(ns wilson.database.votes
  (:require [rethinkdb.query :as r]
            [wilson.helpers :refer :all]
            [wilson.spec :as ws]
            [wilson.database.common :refer [created-timestamped timestamped]]))

(defn save-vote! [vote]
  (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
    (let [vote (created-timestamped vote)
          {[vid] :generated_keys}
          (-> (r/db "wilson")
              (r/table "votes")
              (r/insert vote)
              (r/run conn))]
      (assoc vote :id vid))))

(defn remove-votes-for! [iid]
  (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
    (-> (r/db "wilson")
      (r/table "votes")
      (r/get-all [iid] {:index "iid"})
      (r/delete {:durability :hard
                 :return-changes false})
      (r/run conn))))

(defn fetch-votes [iid]
  (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
    (-> (r/db "wilson")
        (r/table "votes")
        (r/get-all [iid] {:index "iid"})
        (r/get-field :id)
        (r/run conn))))

(defn fetch-all-votes []
  (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
    (-> (r/db "wilson")
        (r/table "votes")
        (r/get-field :id)
        (r/run conn))))

(defn fetch-vote [vid]
  (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
    (-> (r/db "wilson")
        (r/table "votes")
        (r/get vid)
        (r/run conn))))

(defn patch-vote! [vote]
  (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
    (let [vote (timestamped vote)]
      (-> (r/db "wilson")
        (r/table "votes")
        (r/insert vote {:conflict :replace
                        :durability :hard})
        (r/run conn)))))
