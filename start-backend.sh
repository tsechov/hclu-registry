#!/bin/bash
. setenv
sbt "~;container:start; container:reload /"
