@echo off
call ant compile_polanieonlinetools server_build

set LOCALCLASSPATH=build\build_polanieonlinetools;build\build_server;libs\marauroa.jar;libs\log4j.jar;libs\mysql-connector-java-5.1.jar;libs\h2.jar
javaw -cp "%LOCALCLASSPATH%" games.stendhal.tools.npcparser.WordListUpdate
