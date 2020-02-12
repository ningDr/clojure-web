(ns clojure-web.test.handler
  (:require [clojure.test :refer [deftest testing is]]
        [ring.mock.request :refer [request]]
        [clojure-web.handler :refer [app]]))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= (:status response) 200))
      (is (.contains (:body response) "Hello World"))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))
