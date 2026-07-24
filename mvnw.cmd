@echo off
setlocal
set MAVEN_HOME=%~dp0.mvn
if exist "%MAVEN_HOME%\apache-maven-3.9.9\bin\mvn.cmd" (
  call "%MAVEN_HOME%\apache-maven-3.9.9\bin\mvn.cmd" %*
) else if exist "%MAVEN_HOME%\bin\mvn.cmd" (
  call "%MAVEN_HOME%\bin\mvn.cmd" %*
) else (
  echo Maven distribution not found. Please run the setup step first.
  exit /b 1
)
