(ns spider.api
  (:require [cemerick.url :as c]
            [clj-http.client :as http]
            [clojure.core.async :refer [go-loop chan <! >!]]
            [net.cgrand.enlive-html :as enlive])
  (:import  [java.net URL]))

(def default-http-opts {:socket-timeout 10000
                        :conn-timeout 10000
                        :insecure? true
                        :throw-entire-message? false})

(defn enrich-url [url]
  (try
    (c/url url)
    (catch java.net.MalformedURLException _ nil)))

(defn- default-handler [url urls content]
  (println "handled url " url ". Found Links to: " urls))

(defn- make-pred [host]
  (fn [url]
    (= host (:host url))))

(defn- actor
  "Handles values '[url urls content]' form channel out.
   Puts all urls that haven't been handled and fulfil the
   predicate pred."
  [in out starting-url pred handler]
  (go-loop [seen #{starting-url}]
    (let [val (<! out)]
      (when-not (nil? val)
        (println "received value: " (val :url) (val :urls))
        (let [{:keys [url urls content]} val
              to-work (into #{} (comp (remove nil?) (filter pred))
                                (map enrich-url urls))]
          (handler url urls content)
          (doseq [u to-work]
            (>! in u))
          (recur (into seen)))))))

(defn- link->url [base-url link]
  (enrich-url (URL. base-url (-> link :attrs :href))))

(defn- requester [in out opts base-url]
  (go-loop []
    (let [url (<! in)]
      (when-not (nil? in)
        (println "request " url)
        (let [body  (http/get url opts)
              html  (enlive/html-resource body)
              links (enlive/select html [:a])
              urls  (remove nil? (map (partial link->url base-url) links))]
          (>! out {:url     url
                   :urls    urls
                   :content body}))))))

(defn crawl
  "Crawl the given url."
  [starting-url]
  (let [base-url     (clojure.java.io/as-url starting-url)
        starting-url (c/url starting-url)
        pred         (make-pred (:host starting-url))
        work-ch      (chan 10)
        resp-ch      (chan 10)]
    (actor work-ch resp-ch starting-url pred default-handler)
    (requester work-ch resp-ch default-http-opts base-url)
    (requester work-ch resp-ch default-http-opts base-url)))
