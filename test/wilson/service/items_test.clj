(ns wilson.service.items-test
  (:require [clojure.test :refer :all]
            [ring.util.http-predicates :refer :all]
            [wilson.common :refer :all]
            [wilson.service.items :as is]
            [wilson.service.votes :as vs]
            [wilson.service.fixtures :refer [db-fixture]]
            [wilson.score :refer [score not-average]]))

(use-fixtures :each db-fixture)

(deftest post-items!
  (testing "Post items"
    (testing "With no votes"
      (let [{item :body :as res} (is/post-items! {:ups 0 :n 0})]
        (is (some? res))
        (is (created? res))
        (is (= 0 (:ups item)))
        (is (= 0 (:n item)))
        (is (= 0 (:score item)))
        (is (= 0 (:wilson item)))))
    (testing "With some votes"
      (let [{item :body} (is/post-items! {:ups 3 :n 3})]
        (is (= 3 (:ups item)))
        (is (= 3 (:n item)))
        (is (= (score 3 3)
               (:score item)))
        (is (= (not-average 3 3)
               (:wilson item)))))))

(deftest get-items
  (testing "Get items"
    (let [iid-1 (-> (is/post-items! {:up 0 :n 0}) :body :id)
          iid-2 (-> (is/post-items! {:up 0 :n 0}) :body :id)
          {:keys [body] :as res} (is/get-items)]
      (is (ok? res))
      (is (sequential? body))
      (is (= 2 (count body)))
      (is (= #{iid-1 iid-2}
             #{(-> body first :id)
               (-> body second :id)})))))

(deftest get-item
  (testing "Get item"
    (testing "With invalid id"
      (let [{:keys [body] :as res} (is/get-item :invalid)]
        (is (not-found? res))))
    (testing "With valid id"
      (let [iid (-> (is/post-items! {:up 0 :n 0}) :body :id)
            {:keys [body] :as res} (is/get-item iid)]
        (is (ok? res))
        (is (= iid (:id body)))))))

(deftest delete-item!
  (testing "Delete item"
    (testing "With invalid id"
      (let [{:keys [body] :as res} (is/delete-item! :invalid)]
        (is (not-found? res))))
    (testing "With valid id"
      (let [iid (-> (is/post-items! {:up 0 :n 0}) :body :id)
            vid-1 (-> (vs/post-votes! iid {:up true}) :body :id)
            vid-2 (-> (vs/post-votes! iid {:up true}) :body :id)
            {:keys [body] :as res} (is/delete-item! iid)]
        (is (no-content? res))
        (is (not-found? (is/get-item iid)))
        (is (not-found? (vs/get-vote vid-1)))
        (is (not-found? (vs/get-vote vid-2)))))))
