(ns wilson.database.items
  (:require [clojure.tools.logging :as log]
            [rethinkdb.query :as r]
            [wilson.common :refer :all]
            [wilson.database.common :refer [with-conn created-timestamped timestamped]]))

(defn save-item! [item]
  (log/info "Saving item" item)
  (with-conn [conn]
    (let [item (created-timestamped item)
          {[iid] :generated_keys}
          (-> (r/db "wilson")
              (r/table "items")
              (r/insert item)
              (r/run conn))]
      (assoc item :id iid))))

(defn patch-item! [item]
  (log/info "Patching item" item)
  (with-conn [conn]
    (let [item (timestamped item)]
      (-> (r/db "wilson")
        (r/table "items")
        (r/insert item {:conflict :replace
                        :durability :hard})
        (r/run conn)))))

(defn fetch-items []
  (log/info "Fetching all items")
  (with-conn [conn]
    (-> (r/db "wilson")
        (r/table "items")
        (r/get-field :id)
        (r/run conn))))

(defn fetch-item [iid]
  (log/info "Fetching item with id " iid)
  (with-conn [conn]
    (-> (r/db "wilson")
        (r/table "items")
        (r/get iid)
        (r/run conn))))

(defn remove-item! [iid]
  (log/info "Removing item with id " iid)
  (with-conn [conn]
    (-> (r/db "wilson")
      (r/table "items")
      (r/get iid)
      (r/delete {:durability :hard
                 :return-changes false})
      (r/run conn))))
