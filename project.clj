(defproject minimal-chat "0.1.0-SNAPSHOT"
  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2913" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.cognitect/transit-cljs "0.8.205"]

                 [cljsjs/react "0.12.2-5"]
                 [reagent "0.5.0-alpha3"]
                 [reagent-forms "0.4.3"]
                 [reagent-utils "0.1.2"]
                 [secretary "1.2.1"]
                 [cljsjs/firebase "2.1.2-1"]
                 [sunog "0.0.4" :exclusions
                  [org.apache.httpcomponents/httpclient]]

                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.3"]
                 [prone "0.8.0"]
                 [compojure "1.3.2"]
                 [selmer "0.8.0"]
                 [environ "1.0.0"]]

  :plugins [[lein-cljsbuild "1.0.5"]
            [lein-environ "1.0.0"]
            [lein-ring "0.9.1"]]

  :ring {:handler minimal-chat.handler/app
         :uberwar-name "minimal-chat.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "minimal-chat.jar"

  :main minimal-chat.server

  :hooks [leiningen.cljsbuild]

  :clean-targets ^{:protect false} ["resources/public/js"]

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        ;;:externs       ["react/externs/react.js"]
                                        :asset-path   "js/out"
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :profiles {:dev {:repl-options {:init-ns minimal-chat.handler
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[ring-mock "0.1.5"]
                                  [ring/ring-devel "1.3.2"]
                                  [leiningen "2.5.1"]
                                  [figwheel "0.2.5"]
                                  [weasel "0.6.0-SNAPSHOT"]
                                  [com.cemerick/piggieback "0.1.6-SNAPSHOT"]
                                  [pjstadig/humane-test-output "0.7.0"]
                                  [com.cemerick/double-check "0.6.1"]]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.2.5"]
                             [com.cemerick/clojurescript.test "0.3.3"]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :figwheel {:http-server-root "public"
                              :server-port 3449
                              :nrepl-port 54424
                              :css-dirs ["resources/public/css"]
                              :ring-handler minimal-chat.handler/app}

                   :env {:dev? true}

                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                                              :compiler {:main "minimal-chat.dev"
                                                         :source-map true}}
                                        :test {:source-paths ["src/cljs"  "test/cljs"]
                                               :notify-command ["phantomjs"
                                                                :cljs.test/runner
                                                                "test/vendor/es5-shim.js"
                                                                "test/vendor/es5-sham.js"
                                                                "test/vendor/console-polyfill.js"
                                                                "target/test.js"]
                                               :compiler {:output-to "target/test.js"
                                                          :optimizations :whitespace
                                                          :pretty-print true
                                                          :preamble ["react/react.js"]}}
                                        :fuck {:source-paths ["src/cljs"  "test/cljs"]
                                               :notify-command ["phantomjs"
                                                                :cljs.test/runner
                                                                "test/vendor/es5-shim.js"
                                                                "test/vendor/es5-sham.js"
                                                                "test/vendor/console-polyfill.js"
                                                                "target/test.js"
                                                                ]
                                               :compiler {:output-to "target/test.js"
                                                          :optimizations :whitespace
                                                          :pretty-print true
                                                          :preamble ["react/react.js"]}}}
                               :test-commands {"unit" ["phantomjs" :runner
                                                       "test/vendor/es5-shim.js"
                                                       "test/vendor/es5-sham.js"
                                                       "test/vendor/console-polyfill.js"
                                                       "target/test.js"]}}}

             :uberjar {:env {:production true}
                       :aot :all
                       :omit-source true
                       :cljsbuild {:jar true
                                   :builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :whitespace
                                              :pretty-print false}}}}}

             :production {:ring {:open-browser? false
                                 :stacktraces?  false
                                 :auto-reload?  false}
                          :cljsbuild {:builds {:app {:compiler {:main "minimal-chat.prod"}}}}}}

  :aliases {"auto-test" ["do" "clean"
                         ["cljsbuild" "auto" "fuck"]]})
