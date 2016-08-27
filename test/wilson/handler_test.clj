(ns wilson.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [ring.util.http-predicates :refer :all]
            [cheshire.core :as cheshire]
            [wilson.common :refer :all]
            [wilson.handler :refer :all]
            [wilson.service.items :as is]))

(defn parse-body [res]
  (cheshire/parse-string (slurp (:body res)) true))

(deftest test-app
  (testing "/invalid"
    (let [res (app (mock/request :get "/invalid"))]
      (is (not-found? res)))
    (let [res (app (mock/request :post "/invalid"))]
      (is (not-found? res)))
    (let [res (app (mock/request :put "/invalid"))]
      (is (not-found? res)))
    (let [res (app (mock/request :patch "/invalid"))]
      (is (not-found? res)))
    (let [res (app (mock/request :delete "/invalid"))]
      (is (not-found? res)))
    (let [res (app (mock/request :head "/invalid"))]
      (is (not-found? res))))

  (testing "GET /items"
    (with-redefs [is/get-items (fn [] {:status 200 :body []})]
      (let [res (app (mock/request :get "/items"))
            body (parse-body res)]
        (is (ok? res))
        (is (= "application/json; charset=utf-8"
               (get-in res [:headers "Content-Type"])))
        (is (= [] body))))))

(run-tests)
