#!/bin/sh
ant compile_polskagratools server_build

LOCALCLASSPATH=build/build_polskagratools:build/build_server:libs/marauroa.jar:libs/log4j.jar:libs/mysql-connector-java-5.1.5-bin.jar:libs/h2.jar
java -cp "${LOCALCLASSPATH}" games.stendhal.tools.npcparser.WordListUpdate
