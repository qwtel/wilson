(ns wilson.database.setup
  (:require [rethinkdb.query :as r]
            [wilson.common :refer :all]
            [wilson.spec :as ws]
            [wilson.database.common :refer [with-conn]]))

(defn setup! []
  (with-conn [conn]
    (r/run (r/db-create "wilson") conn)
    (-> (r/db "wilson")
        (r/table-create "items")
        (r/run conn))
    (-> (r/db "wilson")
        (r/table-create "votes")
        (r/run conn))
    (-> (r/db "wilson")
        (r/table "votes")
        (r/index-create "iid" (r/fn [row]
                                (r/get-field row ::ws/iid)))
        (r/run conn))))
