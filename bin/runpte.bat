@echo off
call ant compile_polanieonlinetools server_build

set LOCALCLASSPATH=build\build_polanieonlinetools;build\build_server;build\build_server_maps;libs\marauroa.jar;libs\log4j.jar;libs\commons-lang.jar;libs\groovy.jar;libs\mysql-connector-java-8.0.13.jar;libs\h2.jar;libs\swing-layout.jar;.
javaw -cp "%LOCALCLASSPATH%" games.stendhal.tools.npcparser.TestEnvDlg
