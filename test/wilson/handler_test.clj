(ns wilson.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :as cheshire]
            [wilson.handler :refer :all]
            [wilson.handler.items :as ih]))

(defn parse-body [res]
  (cheshire/parse-string (slurp (:body res)) true))

(deftest test-app
  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404))))

  (testing "get items"
    (with-redefs [ih/get-items (fn [] {:status 200 :body []})]
      (let [res (app (mock/request :get "/items"))]
        (is (= (:status res)
               200))
        (is (= (get-in res [:headers "Content-Type"])
               "application/json; charset=utf-8"))
        (is (= (parse-body res)
               []))))))

; (run-tests)
