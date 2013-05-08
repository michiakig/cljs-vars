(defproject cljsvars "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [marginalia "0.7.0"]
                 [enlive "1.0.0"]]
  :plugins [[lein-git-deps "0.0.1-SNAPSHOT"]]
  :git-dependencies [["https://github.com/clojure/clojurescript.git" "master"]
                     ["https://github.com/clojure/clojure.git" "master"]]
  :main cljsvars.core)
