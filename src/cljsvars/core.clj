(ns cljsvars.core
  (:require marginalia.parser
            [net.cgrand.enlive-html :as html])
  (:import (clojure.lang LineNumberingPushbackReader)
           (java.io BufferedReader StringReader)))

(def ^:dynamic *branch* "master")
(def ^:dynamic *fname* "src/cljs/cljs/core.cljs")
(def ^:dynamic *namespace* 'clojure.core) ;used for links to clojuredocs.org

;; definition forms used in ClojureScript
;; grep -hor '^(def[[:alpha:]]*[[:space:]]' clojurescript/src/cljs | sort - | uniq -
(def defs '#{def defn defprotocol deftype})

(def cljs-github-url "https://github.com/clojure/clojurescript")
(defn make-cljs-source-link [treeish fname linenum]
  (str cljs-github-url "/blob/" treeish "/" fname "#L" linenum))
(def clojuredocs-url "http://clojuredocs.org") ;/clojure_core/clojure.core/partition
(defn make-clojuredocs-link [ns varname]
  (str clojuredocs-url "/clojure_core/" ns "/" varname))

(defn parse-string [s]
  (filter
   #(defs (first (:form %)))
  (marginalia.parser/parse*
   (LineNumberingPushbackReader. (BufferedReader. (StringReader. s))))))

(defn parse-file [fname]
  (parse-string (slurp fname)))

(defn merge-seqs
  "Given a combination fn, a comparison fn, and two sorted seqs, merges
  the seqs into one. Combination fn will be called with two args when
  first of both seqs are equal, and called with one arg when they are
  not equal, while comparison fn should take two args and return a
  negative number, zero, or a positive number (see
  clojure.core/compare). Note that resulting seq may not be sorted,
  depending on what combinefn returns.

  Example: (merge-seqs + compare [1 2 3] [2 3 4]) => [1 2 6 4]"

  [combinefn comparefn s1 s2]
  (loop [a s1 b s2 acc []]
    (if (or (nil? a) (nil? b))
      (into acc (map combinefn (if (nil? a) b a)))
      (let [c (comparefn (first a) (first b))]
        (cond
          (= c 0) (recur (next a) (next b) (conj acc (combinefn (first a) (first b))))
          (< c 0) (recur (next a) b (conj acc (combinefn (first a))))
          (> c 0) (recur a (next b) (conj acc (combinefn (first b)))))))))

(def var-sel [:.var])

(html/defsnippet var-snippet "index.html" var-sel [{[_ name & _] :form linenum :start}]
  [:.name] (html/content (str name))
  [:.github] (html/set-attr :href (make-cljs-source-link *branch* *fname* linenum))
  [:.clojuredocs] (html/set-attr :href (make-clojuredocs-link *namespace* name)))

(html/deftemplate top-level-template "index.html" [ctx]
  [:.vars] (html/content (map var-snippet ctx)))
