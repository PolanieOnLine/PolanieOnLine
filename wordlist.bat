@echo off
call ant compile_polskagratools server_build

set LOCALCLASSPATH=build\build_polskagratools;build\build_server;libs\marauroa.jar;libs\log4j.jar;libs\mysql-connector-java-5.1.jar;libs\h2.jar
javaw -cp "%LOCALCLASSPATH%" games.stendhal.tools.npcparser.WordListUpdate
