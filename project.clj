(defproject com.lemonodor/pronouncing "0.0.2-SNAPSHOT"
  :description "Clojure interface to the CMU pronouncing dictionary. Provides ways to look up word pronounciations, count syllables, and find rhyming words."
  :url "https://github.com/wiseman/clj-pronouncing"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :deploy-repositories [["releases" :clojars]]
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :profiles {:dev {:plugins [[lein-cloverage "1.0.2"]]}})
