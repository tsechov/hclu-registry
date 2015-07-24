#!/bin/sh

# # # # # # # # # # # # # # # # # # # # # # # # #
# Script used to deploy project on demo server  #
# # # # # # # # # # # # # # # # # # # # # # # # #

JAVA_HOME="/opt/java/jdk1.7.0_09"
export JAVA_HOME

# shutdown jetty
jetty/bin/jetty.sh stop

# clear logs
rm jetty/logs/*

# update source code
cd hreg
git pull

# wait 5 sec
sleep 5

# copy configuration file to proper location
cp /home/bootstrap/hreg/application.conf /home/bootstrap/hreg/hreg/hreg-rest/src/main/resources/

# package
sbt compile package

# deploy
cp hreg-ui/target/scala-2.10/hreg.war ../jetty/webapps/

# start jetty
cd ../
jetty/bin/jetty.sh start