(ns wilson.database.setup
  (:require [rethinkdb.query :as r]
            [wilson.common :refer :all]
            [wilson.database.common :refer [with-conn db-name]]))

(defn -main []
  (with-conn [conn]
    (r/run (r/db-create db-name) conn)
    (-> (r/table-create "items")
        (r/run conn))
    (-> (r/table-create "votes")
        (r/run conn))
    (-> (r/table "votes")
        (r/index-create "iid" (r/fn [row]
                                (r/get-field row :iid)))
        (r/run conn))))
