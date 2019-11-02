@echo off
set JAVA_OPTS=-Dspring.boot.admin.client.enabled=false
set JAVA_OPTS=%JAVA_OPTS% -Deureka.client.enabled=false
set JAVA_OPTS=%JAVA_OPTS% -Ddeployment.manager.sa.dir=C:\opt\springboot
java %JAVA_OPTS% -jar target\deploymentManager.jar
