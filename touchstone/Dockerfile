FROM adoptopenjdk:11-jre-hotspot as builder
LABEL maintaner="Callum Van Zyl <cy006007@student.reading.ac.uk>"


### BUILD TOUCHSTONE BY LAYER ###

ARG JAR_FILE=target/*-SNAPSHOT.jar
COPY $JAR_FILE touchstone.jar
RUN java -Djarmode=layertools -jar touchstone.jar extract

FROM adoptopenjdk:11-jre-hotspot
COPY --from=builder dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]


### INSTALL GOOGLE CHROME AND CHROME WEB DRIVER ###
### https://github.com/SeleniumHQ/docker-selenium/blob/trunk/NodeChrome/Dockerfile ###

RUN apt-get update -qqy \
  && apt-get -qqy install gnupg2 sudo unzip wget \
  && wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
  && echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list \
  && apt-get update -qqy \
  && apt-get -qqy install google-chrome-stable \
  && rm /etc/apt/sources.list.d/google-chrome.list \
  && rm -rf /var/lib/apt/lists/* /var/cache/apt/*

ARG CHROME_WEB_DRIVER_PATH

RUN CHROME_MAJOR_VERSION=$(google-chrome --version | sed -E "s/.* ([0-9]+)(\.[0-9]+){3}.*/\1/") \
  && CHROME_DRIVER_VERSION=$(wget --no-verbose -O - "https://chromedriver.storage.googleapis.com/LATEST_RELEASE_$CHROME_MAJOR_VERSION") \
  && wget --no-verbose -O /tmp/chromedriver_linux64.zip https://chromedriver.storage.googleapis.com/$CHROME_DRIVER_VERSION/chromedriver_linux64.zip \
  && rm -rf $CHROME_WEB_DRIVER_PATH \
  && unzip /tmp/chromedriver_linux64.zip -d $(dirname $CHROME_WEB_DRIVER_PATH) \
  && rm /tmp/chromedriver_linux64.zip \
  && chmod 755 $CHROME_WEB_DRIVER_PATH


### NETWORKING ###
ARG SERVER_PORT
EXPOSE $SERVER_PORT


### TOUCHSTONE USER ###

# ENABLE THIS WHEN I CAN BE BOTHERED TO FIGURE OUT PERMISSIONS FOR THE MOUNTED DATA VOLUME
#RUN groupadd touchstone \
#  && useradd -m touchstone -g touchstone \
#  && chown -R touchstone:touchstone /home/touchstone
#
#USER touchstone:touchstone
