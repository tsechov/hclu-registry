#!/bin/bash
docker stop `docker ps|grep "5432->5433"|cut -f1 -d" "`
CONTAINER=$(docker run -d -e 'DB_USER=hregdev' -e 'DB_PASS=hregdev' -e 'DB_NAME=hregdev' -p 5432:5433 sameersbn/postgresql:9.4-3)
POSTGRE_IP=$(docker inspect ${CONTAINER} | grep \"IPAddress\" | awk '{ print $2 }' | tr -d ',"')
echo "postgre ip: " ${POSTGRE_IP}
. setenv
sbt -jvm-debug 5005 "~;container:start; container:reload /"
docker stop ${CONTAINER}
