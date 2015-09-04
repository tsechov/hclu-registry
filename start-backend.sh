#!/bin/bash
. setenv
sbt -jvm-debug 5005 "~;container:start; container:reload /"
