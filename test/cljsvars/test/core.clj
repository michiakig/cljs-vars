(ns cljsvars.test.core
  (:use [cljsvars.core])
  (:use [clojure.test]))

(deftest test-parse-string
  (is (= (parse-string "(def foo 1)")
         '({:form (def foo 1)
            :start 1
            :end 1})))
  (is (= (parse-string "(defn bar [] \nnil)\n(defprotocol IQux)\n")
         '({:form (defn bar [] nil)
            :start 1
            :end 2}
           {:form (defprotocol IQux)
            :start 3
            :end 3}))))
