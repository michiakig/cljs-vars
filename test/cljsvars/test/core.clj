(ns cljsvars.test.core
  (:use [cljsvars.core])
  (:use [clojure.test]))

(deftest test-parse-file
  (is (= (parse-file "src/cljsvars/core.clj")
         '[[defs 8]
           [parse-file 11]])))
