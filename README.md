# mjpeg-streamer

MJPEG Streamer of jpeg images.

#Build

mvn clean package

#Running

java -jar mjpeg-streamer-1.0-SNAPSHOT-jar-with-dependencies.jar `<directory with images>`

Instantiates Jetty Embedded server on port 8080 with application context "camock"

#Usage

* `/` - show dbs
* `/view/<image id>` - download image by id
* `/mjpeg/<image id>` - get MJPEG stream of selected image. Optional query parameters: 
    * `maxFPS=<integer>` - set stream's FPS
    * `rotation=<rotaion angle of [0,90,180,270]>` - set image's rotation
