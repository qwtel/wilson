(ns wilson.service.votes-test
  (:require [clojure.test :refer :all]
            [ring.util.http-predicates :refer :all]
            [wilson.common :refer :all]
            [wilson.service.items :as is]
            [wilson.service.votes :as vs]
            [wilson.service.fixtures :refer [db-fixture]]
            [wilson.score :refer [score not-average]]))

(use-fixtures :each db-fixture)

(deftest post-votes!
  (testing "Post votes"
    (let [iid-1 (-> (is/post-items! {:up 0 :n 0}) :body :id)]
      (testing "For a invalid item id"
        (let [res (vs/post-votes! :invalid {:up true})]
          (is (not-found? res))))

      (testing "For a valid id"
        (let [{vote :body :as res} (vs/post-votes! iid-1 {:up true})
              {item :body} (is/get-item iid-1)]
          (is (created? res))
          (is (some? (:id vote)))
          (is (= (score 1 1)
                 (:score item)))
          (is (= (not-average 1 1)
                 (:wilson item))))))))

(deftest get-votes
  (testing "Get votes"
    (testing "For an item that has no votes"
      (let [iid-1 (-> (is/post-items! {:up 0 :n 0}) :body :id)
            {:keys [body] :as res} (vs/get-votes iid-1)]
        (is (ok? res))
        (is (sequential? body))
        (is (= 0 (count body)))))
    (testing "For an item has votes"
      (let [iid-1 (-> (is/post-items! {:up 0 :n 0}) :body :id)
            iid-2 (-> (is/post-items! {:up 0 :n 0}) :body :id)
            vid-1 (-> (vs/post-votes! iid-1 {:up true}) :body :id)
            vid-2 (-> (vs/post-votes! iid-1 {:up true}) :body :id)
            vid-3 (-> (vs/post-votes! iid-2 {:up true}) :body :id)
            {:keys [body] :as res} (vs/get-votes iid-1)]
        (is (ok? res))
        (is (sequential? body))
        (is (= 2 (count body)))
        (is (= #{vid-1 vid-2}
               (into #{} (map :id body))))))))

(deftest get-all-votes
  (testing "Get all votes"
    (let [iid-1 (-> (is/post-items! {:up 0 :n 0}) :body :id)
          iid-2 (-> (is/post-items! {:up 0 :n 0}) :body :id)
          vid-1 (-> (vs/post-votes! iid-1 {:up true}) :body :id)
          vid-2 (-> (vs/post-votes! iid-1 {:up true}) :body :id)
          vid-3 (-> (vs/post-votes! iid-2 {:up true}) :body :id)
          {:keys [body] :as res} (vs/get-all-votes)]
      (is (ok? res))
      (is (sequential? body))
      (is (= 3 (count body)))
      (is (= #{vid-1 vid-2 vid-3}
             (into #{} (map :id body)))))))

(deftest get-vote
  (testing "Get vote"
    (let [iid-1 (-> (is/post-items! {:up 0 :n 0}) :body :id)
          vid-1 (-> (vs/post-votes! iid-1 {:up true}) :body :id)]
      (testing "For an invalid id"
        (let [{:keys [body] :as res}
              (vs/get-vote :invalid)]
          (is (not-found? res))))
      (testing "For a valid id"
        (let [{:keys [body] :as res} (vs/get-vote vid-1)]
          (is (ok? res))
          (is (= vid-1 (:id body))))))))

(deftest patch-vote!
  (testing "Patch vote"
    (let [iid-1 (-> (is/post-items! {:up 0 :n 0}) :body :id)
          vid-1 (-> (vs/post-votes! iid-1 {:up true}) :body :id)]
      (testing "From up vote to down vote"
        (let [{:keys [body] :as res} (vs/patch-vote! vid-1 {:up false})
              {item :body} (is/get-item iid-1)]
          (is (ok? res))
          (is (= false (:up body)))
          (is (= (score 0 1)
                 (:score item)))
          (is (= (not-average 0 1)
                 (:wilson item)))))
      (testing "From down vote to no vote"
        (let [{:keys [body] :as res} (vs/patch-vote! vid-1 {:up nil})
              {item :body} (is/get-item iid-1)]
          (is (ok? res))
          (is (= nil (:up body)))
          (is (= (score 0 0)
                 (:score item)))
          (is (= (not-average 0 0)
                 (:wilson item))))))))
