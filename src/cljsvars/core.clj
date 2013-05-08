(ns cljsvars.core
  (:require marginalia.parser
            [net.cgrand.enlive-html :as html])
  (:import (clojure.lang LineNumberingPushbackReader)
           (java.io BufferedReader StringReader)))

(def ^:dynamic *branch* "master")
(def ^:dynamic *fname* "src/cljs/cljs/core.cljs")
(def ^:dynamic *namespace* 'clojure.core) ;used for links to clojuredocs.org

;; definition forms
;; grep -hor '^(def[[:alpha:]]*[[:space:]]' clojurescript/src/cljs | sort - | uniq -
;; (def cljs-defs '#{def defn defprotocol deftype})
(def defs '#{def defmacro defmulti defn defprotocol defrecord defstruct deftype}) 

(def cljs-github "https://github.com/clojure/clojurescript")
(def clj-github "https://github.com/clojure/clojure")
(defn make-source-link [base treeish fname linenum]
  (str base "/blob/" treeish "/" fname "#L" linenum))
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

  Example: (merge-seqs + compare [1 2 3] [2 3 4]) => [1 4 6 4]"

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

(let [either (fn [a b] (if a
                         (html/content (str (second (:form a))))
                         (html/do->
                          (html/content (str (second (:form b))))
                          (html/add-class "hidden"))))]
    (html/defsnippet var-snippet "template.html" var-sel [{clj :clj cljs :cljs}]
      [:.clj] (either clj cljs)
      [:.cljs] (either cljs clj)
      ;; [:.github] (html/set-attr :href
      ;;                           (make-source-link cljs-github *branch* *fname* linenum))
      ;; [:.clojuredocs] (html/set-attr :href (make-clojuredocs-link *namespace* name))
      ))

(html/deftemplate top-level-template "template.html" [ctx]
  [:.vars] (html/content (map var-snippet ctx)))

;; parse cljs, clj -> two lists of {:form '(defn ...) }
;; sort
;; merge
;; {:clj {:form '(def foo ...) :linenum 1} :cljs nil}

(defn -main []
  (let [clj-fnames [".lein-git-deps/clojure/src/clj/clojure/core.clj"
                    ".lein-git-deps/clojure/src/clj/clojure/core_deftype.clj"
                    ".lein-git-deps/clojure/src/clj/clojure/core_print.clj"
                    ".lein-git-deps/clojure/src/clj/clojure/core_proxy.clj"
                    ]
        cljs-fnames [".lein-git-deps/clojurescript/src/cljs/cljs/core.cljs"]
        out-fname (str (.getCanonicalPath (java.io.File. ".")) "/resources/index.html")
        compare-parsed (fn [x y] (compare (second (:form x)) (second (:form y))))
        cljvars (map #(assoc % :clj true) (sort compare-parsed (mapcat parse-file clj-fnames)))
        cljsvars (map #(assoc % :cljs true) (sort compare-parsed (mapcat parse-file cljs-fnames)))
        combine (fn
                  ([x] (if (:clj x)
                         {:clj x :cljs nil}
                         {:clj nil :cljs x}))
                  ([x y] (if (:clj x)
                           {:clj x :cljs y}
                           {:clj y :cljs x})))
        blob (merge-seqs combine compare-parsed cljvars cljsvars)]
    (spit out-fname (apply str (top-level-template blob)))))
