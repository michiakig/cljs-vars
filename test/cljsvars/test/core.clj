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

(deftest test-merge-seqs
  (is (= (merge-seqs + compare [1 3 5] [2 3 4])
         '[1 2 6 4 5]))
  (is (= (merge-seqs (fn ([x] x) ([x y] x)) compare '[a b c] '[c d e])
         '[a b c d e])))