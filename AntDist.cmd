@echo off


REM Change the folder path in the following line if, for example, you have extracted ant without
REM renaming the folder afterwards (even though renaming it is highly recommended)
REM Never leave any space between the variable's name and the equal sign
REM Otherwise, it will simply not work



set ANT_HOME=E:\ant



REM Change the folder path in the following line to the folder path of your
REM own version of Java's JDK
REM Never leave any space between the variable's name and the equal sign
REM Otherwise, it will simply not work



set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_181



REM Do not change the rest of this file

set Path=%Path%;%ANT_HOME%\bin
cmd /c ant dist
pause