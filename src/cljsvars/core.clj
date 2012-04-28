(ns cljsvars.core
  (:require marginalia.parser)
  (:import (clojure.lang LineNumberingPushbackReader)
           (java.io BufferedReader StringReader)))

;; definition forms used in ClojureScript
;; grep -hor '^(def[[:alpha:]]*[[:space:]]' clojurescript/src/cljs | sort - | uniq -
(def defs '#{def defn defprotocol deftype})

;; basic usage of marginalia.parser/parse*
(defn parse-file [fname]
  (reduce
   (fn [acc {:keys [form start]}]
     (if (and (list? form) (defs (first form)))
       (conj acc [(second form) start])
       acc))
   []
   (marginalia.parser/parse*
    (LineNumberingPushbackReader. (BufferedReader. (StringReader. (slurp fname)))))))
