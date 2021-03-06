(ns video-optimize.core
  (:gen-class)
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clj-yaml.core :as yaml]
   [hawk.core :as hawk]
   [clojure.java.shell :as shell]))

;; VARIABLES
(def config (yaml/parse-string (slurp "config.yaml")))
(def extension_thumbnail (:extension_thumbnail config))
(def width_thumbnail (:width_thumbnail config))
(def path_videos (:path_videos config))

(defn -main [& args]
  ;; Watch
  (hawk/watch! [{:paths   [path_videos]
                 :handler (fn [ctx e]
                            (let [path_raw       (.getAbsolutePath (:file e))
                                  is_thumbnail   (doall (re-find (re-pattern extension_thumbnail) path_raw))
                                  path_thumbnail (str/join (concat (drop-last (str/split path_raw #"\.")) extension_thumbnail))]
                              (if (and (.exists (io/file path_raw)) (not is_thumbnail) (not (.exists (io/file path_thumbnail))))
                                (do
                                  (prn (str "Optimizing: " path_raw))
                                  ;; Optimizing with ffmpeg
                                  (shell/sh "ffmpeg" "-y" "-i" path_raw "-vf" (str "scale=" width_thumbnail ":-2") "-c:v" "libx264" "-crf" "23" "-profile:v" "high" "-pix_fmt" "yuv420p" "-color_primaries" "1" "-color_trc" "1" "-colorspace" "1" "-movflags" "+faststart" "-an" "-acodec" "aac" "-ab" "128kb" path_thumbnail)
                                  (prn (str "Finish: " path_thumbnail))))))}])
  (println "Running: Feed me!"))
