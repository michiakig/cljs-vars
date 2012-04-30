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

(def var-sel [:.var])

(html/defsnippet var-snippet "index.html" var-sel [{[_ name & _] :form linenum :start}]
  [:.name] (html/content (str name))
  [:.github] (html/set-attr :href (make-cljs-source-link *branch* *fname* linenum))
  [:.clojuredocs] (html/set-attr :href (make-clojuredocs-link *namespace* name)))

(html/deftemplate top-level-template "index.html" [ctx]
  [:.vars] (html/content (map var-snippet ctx)))
