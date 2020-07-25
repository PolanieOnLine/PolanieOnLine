#!/bin/sh
POL_VERSION="1.14"

LOCALCLASSPATH=.:data/script/:data/conf/:polanieonline-server-$POL_VERSION.jar:marauroa.jar:mysql-connector.jar:log4j.jar:commons-lang.jar:h2.jar

java -Xmx400m -cp "${LOCALCLASSPATH}" games.stendhal.server.StendhalServer -c server.ini -l

