(ns minimal-chat.core
  (:require [cljs.core.async :refer [<!]]
            [clojure.string :as str]
            [cognitect.transit :as t]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [reagent.core :as reagent]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [sunog.async :as pa]
            [sunog.core :as p])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:import goog.History))

(defn random-four-characters []
  (->> (repeatedly #(rand-nth ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z"]))
       (take 4)
       (str/join)
       (str/upper-case)))

;; -----------------------------
;; Firebase helpers

(defn bind-to-ratom [ratom ref & [korks]]
  (let [ch (pa/listen-to< ref korks :value)]
    (go-loop [[key val] (<! ch)]
      (reset! ratom val)
      (recur (<! ch)))))

(defn firebase-swap! [fb-ref f & args]
  (apply p/swap! fb-ref f args))

;; Commented out code is useful if you want to store your data as
;; transit. Sort of defeats some of Firebase's functionality, but oh
;; well. 

;; (def transit-reader (t/reader :json))
;; (def transit-writer (t/writer :json))

;; (defn bind-to-ratom [ratom ref & [korks]]
;;   (let [ch (pa/listen-to< ref korks :value)]
;;     (go-loop [[key val] (<! ch)]
;;       (let [v (t/read transit-reader val)]
;;         (reset! ratom v)
;;         (recur (<! ch))))))

;; (defn to-transit [x]
;;   (t/write transit-writer x))

;; (defn wrap-with-transit [f]
;;   (fn [& args]
;;     (let [x (t/read transit-reader (first args))
;;           z (rest args)]
;;       (t/write transit-writer (apply f x z)))))

;; (defn firebase-swap! [fb-ref f & args]
;;   (apply p/swap! fb-ref (wrap-with-transit f) args))


;; -----------------------------
;; Global Data

(def firebase-io-root "<YOUR URL>")

(defonce show-state? (reagent/atom true))
(defonce username (reagent/atom "username"))
(defonce chat-room (reagent/atom nil))

(defonce fb-messages (atom nil))
(defonce messages (reagent/atom []))

;; ------------------
;; logic

(defn join-room [id]
  (reset! chat-room id)
  (let [fb-root (p/connect firebase-io-root)]
    (let [fb-item (p/get-in fb-root [(str/lower-case id) :messages])]
      (reset! fb-messages fb-item)
      (bind-to-ratom messages fb-item))))

;; -------------------------
;; Views

(defn ^:export toggle-showing-state! []
  (swap! show-state? not))

(defn text-input [value]
  [:input {:type "text"
           :value @value
           :on-change #(reset! value (-> % .-target .-value))}])

(defn on-going-chat []
  [:div
   [:label "Your username:"]
   [text-input username]

   [:div#messages
    (for [[message index] (map vector (take-last 30 @messages) (range))]
      ^{:key (str "message-" index)}
      [:div [:span.username (first message) ": "] [:span.message (second message)]])]

   (let [users-message (reagent/atom "")]
     [:form {:on-submit (fn [e]
                          (.preventDefault e)
                          (firebase-swap! @fb-messages conj [@username @users-message]))}
      [text-input users-message]
      [:button {:type "submit"} "Send"]])])

(defn join-room-view []
  (let [this-room (atom (random-four-characters))
        change-location #(set! (.-location js/window) (str "#/room/" @this-room))]
    [:div
     [:p [:div
          [:button {:on-click change-location
                    :type "submit"}
           "New Room"]]]

     [:p [:div
          [:input {:placeholder "Room Name"
                   :type "text"
                   :on-change #(reset! this-room (-> % .-target .-value))}]
          [:button {:type "submit"
                    :on-click change-location}
           "Join Room"]]]]))

(defn home-page []
  [:div
   [:div {:class "container"}
    [:div {:class "page-header"}
     [:h2 "Minimal Chat"]]

    (if @chat-room
      [on-going-chat]
      [join-room-view])

    (when @show-state?
      [:div [:br] [:br] [:br] "STATE!!!!"
       [:div "Messages: " (pr-str @messages)]])]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (reset! chat-room nil)
  (session/put! :current-page #'home-page))

(secretary/defroute "/room/:id" {:as params}
  (when (not= @chat-room (:id params))
    (join-room (:id params)))
  (session/put! :current-page #'home-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn init! []
  (hook-browser-navigation!)

  (reagent/render-component [current-page] (.getElementById js/document "app")))
