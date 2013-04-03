@echo off

:: Licensed to the Apache Software Foundation (ASF) under one or more
:: contributor license agreements.  See the NOTICE file distributed with
:: this work for additional information regarding copyright ownership.
:: The ASF licenses this file to You under the Apache License, Version 2.0
:: (the "License"); you may not use this file except in compliance with
:: the License.  You may obtain a copy of the License at
::
::     http:\\www.apache.org\licenses\LICENSE-2.0
::
:: Unless required by applicable law or agreed to in writing, software
:: distributed under the License is distributed on an "AS IS" BASIS,
:: WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
:: See the License for the specific language governing permissions and
:: limitations under the License.


:: The command script
::
:: Environment Variables
::
::   JAVA_HOME        The java implementation to use.  Overrides JAVA_HOME.
::
::   HEAPSIZE  The maximum amount of heap to use, in MB.
::                    Default is 1000.
::
::   OPTS      Extra Java runtime options.
::
::   CONF_DIR  Alternate conf dir. Default is .\config.
::
:: This script creates the benchmark data and then runs the workload
:: on it

setlocal enableextensions

SET BASE_DIR=%~dp0..

:: LINKBENCH_HOME so that LinkBench Java code can access env var
pushd %BASE_DIR%
SET LINKBENCH_HOME=%CD%

if "%JAVA_HOME%" NEQ "" (
  for /f %%i in ('which java') do @SET JAVA=%%i
  IF %ERRORLEVEL% NEQ 0 (
    echo "Error: java not found, SET JAVA_HOME or add java to PATH."
    goto :end
  )
) else (
  JAVA=%JAVA_HOME%\bin\java
)

echo "Using java at: %JAVA%"

SET JAVA_HEAP_MAX=-Xmx1000m

:: CLASSPATH initially contains CONF_DIR
if "%CONF_DIR%" NEQ "" (
  SET CLASSPATH="%CONF_DIR%"
) else (
  SET CLASSPATH="%BASE_DIR%\config"
) 
SET CLASSPATH=%CLASSPATH%:%JAVA_HOME%\lib\tools.jar

:: add libs to CLASSPATH
for %%i in ("%LINKBENCH_HOME%\lib\*.jar") do (call :append_classpath "%%i")

:: add libs to CLASSPATH
for %%i in ("%LINKBENCH_HOME%\build\lib\*.jar") do (call :append_classpath "%%i")

:: add latest jar to CLASSPATH
SET CLASSPATH=%CLASSPATH%;dist\FacebookLinkBench.jar

goto :run

:append_classpath
set CLASSPATH=%CLASSPATH%;%1
goto :eof

:run
:: figure out which class to run
SET CLASS=com.facebook.LinkBench.LinkBenchDriver

:: run it
"%JAVA%" %JAVA_HEAP_MAX% %OPTS% -classpath "%CLASSPATH%" %CLASS% %*

:end

endlocal