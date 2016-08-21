(ns wilson.handler
  (:require [schema.core :as s]
            [compojure.route :as route]
            [compojure.api.sweet :refer :all]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.http-response :refer :all]
            [rethinkdb.query :as r]
            [wilson.helpers :refer :all]
            [wilson.score :refer :all]
            [wilson.spec :as ws]
            [wilson.schema :as wsc]
            [wilson.resources.items :refer :all]
            [wilson.resources.votes :refer :all]))

(defn- post-setup! []
  (try
    (with-open [conn (r/connect :host "127.0.0.1" :port 28015 :db "test")]
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
          (r/run conn))
      {:status 201 :body "Setup complete"})
    (catch Exception e
      (.printStackTrace e)
      {:status 500 :body (format "IOException: %s" (.getMessage e))})))

(defn- update-body-if-success [{:keys [status body] :as res} f]
  (if (or (< status 200) (>= status 300))
    res
    (update res :body f)))

(defn- items->resource [res]
  (update-body-if-success res
    (fn [item-ids]
      (map #(str base-url "/items/" %) item-ids))))

(defn- votes->resource [res]
  (update-body-if-success res
    (fn [vote-ids]
      (map #(str base-url "/votes/" %) vote-ids))))

(defn- item->resource [res] res)

(defn- vote->resource [res]
  (update-body-if-success res
    #(assoc % ::ws/iid
      (str base-url "/items/" (::ws/iid %)))))

(def app
  (api
    {:swagger {:ui "/api-docs"
               :spec "/swagger.json"
               :data {:info {:title "Sample API"
                             :description "Compojure API Example"}
                      :tags [{:name "api", :description "some apis"}]}}}

    (context "/items" []
      :tags ["items"]

      (POST "/" []
        :body [item wsc/NewItem]
        :return wsc/Item
        :summary "Create a new item, optionally with pre-existings votes"
        (item->resource (post-items! item)))

      (GET "/" []
        :return [s/Str]
        :summary "Get a list of all item ids"
        (items->resource (get-items)))

      (GET "/:iid" [iid]
        :return wsc/Item
        :summary "Get a specific item by id"
        (item->resource (get-item iid)))

      (DELETE "/:iid" [iid]
        :summary "Delete the item and all associated votes"
        (delete-item iid))

      (GET "/:iid/votes" [iid]
        :return [s/Str]
        :summary "Get a list of all vote ids for the item"
        (votes->resource (get-votes iid)))

      (POST "/:iid/votes" [iid]
        :body [vote wsc/NewVote]
        :return wsc/Vote
        :summary "Up or down vote the item"
        (vote->resource (post-votes! iid vote))))

    (context "/votes" []
      :tags ["votes"]

      (GET "/" []
        :return [s/Str]
        :summary "Get all vote ids"
        (votes->resource (get-all-votes)))

      (GET "/:vid" [vid]
        :return wsc/Vote
        :summary "Get a specific vote per id"
        (vote->resource (get-vote vid)))

      (PATCH "/:vid" [vid]
        :body [vote wsc/NewVote]
        :return wsc/Vote
        :summary "Change a vote"
        (vote->resource (patch-vote vid vote))))

    ;; TODO: There has to be a better way to set up the database...
    (context "/setup" []
      :tags ["setup"]

      (POST "/" []
        :body [_ s/Any]
        :return s/Bool
        (post-setup!)))))
