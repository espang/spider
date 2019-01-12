(defproject spider "0.1.0-SNAPSHOT"
  :description "Crawl a homepage and creates a graph of internal links."
  :url "https://github.com/espang/spider"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/core.async "0.4.490"]
                 [clj-http "3.9.1"]
                 [com.cemerick/url "0.1.1"]
                 [enlive "1.1.6"]])
