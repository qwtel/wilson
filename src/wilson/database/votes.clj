(ns wilson.database.votes
  (:require [clojure.tools.logging :as log]
            [rethinkdb.query :as r]
            [wilson.common :refer :all]
            [wilson.spec :as ws]
            [wilson.database.common :refer [with-conn create-timestamped timestamp]]))

(defn save-vote! [vote]
  (log/info "Saving vote" vote)
  (with-conn [conn]
    (let [vote (create-timestamped vote)
          {[vid] :generated_keys}
          (-> (r/table "votes")
              (r/insert vote)
              (r/run conn))]
      (assoc vote :id vid))))

(defn remove-votes-for! [iid]
  (log/info "Removing all votes for " iid)
  (with-conn [conn]
    (-> (r/table "votes")
        (r/get-all [iid] {:index "iid"})
        (r/delete {:durability :hard
                   :return-changes false})
        (r/run conn))))

(defn fetch-votes [iid]
  (log/info "Fetching all votes for " iid)
  (with-conn [conn]
    (-> (r/table "votes")
        (r/get-all [iid] {:index "iid"})
        (r/get-field :id)
        (r/run conn))))

(defn fetch-all-votes []
  (log/info "Fetching all votes")
  (with-conn [conn]
    (-> (r/table "votes")
        (r/get-field :id)
        (r/run conn))))

(defn fetch-vote [vid]
  (log/info "Fetching vote with id " vid)
  (with-conn [conn]
    (-> (r/table "votes")
        (r/get vid)
        (r/run conn))))

(defn patch-vote! [vote]
  (log/info "Patching vote " vote)
  (with-conn [conn]
    (let [vote (timestamp vote)]
      (-> (r/table "votes")
          (r/insert vote {:conflict :replace
                          :durability :hard})
          (r/run conn)))))
