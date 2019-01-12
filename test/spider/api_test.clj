(ns spider.api-test
  (:require [clojure.test :refer :all]
            [spider.api :refer :all])
  (:import  [cemerick.url URL]))

(deftest test-url1
  (is (= nil (enrich-url "www.google.com"))))

(deftest test-url2
  (is (= (URL. "https" nil nil "www.google.com" -1 "" nil nil)
         (enrich-url "https://www.google.com"))))
