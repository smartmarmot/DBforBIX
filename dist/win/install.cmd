@echo off

SET mypath=%~dp0

if "%PROCESSOR_ARCHITECTURE%"=="AMD64" goto 64BIT
echo Installing 32bit service
%mypath%dbforbix32.exe //IS//DBforBIX --DisplayName="DBforBIX Zabbix Agent" --StartPath=%mypath% --LogPath="logs" --Classpath "dbforbix.jar;.;lib/commons-cli-1.3.1.jar;lib/httpcore-4.4.4.jar;lib/commons-codec-1.9.jar;lib/jtds-1.3.1.jar;lib/commons-collections-3.2.2.jar;lib/log4j-1.2.17.jar;lib/commons-configuration-1.10.jar;lib/logback-classic-1.0.13.jar;lib/commons-daemon-1.0.15.jar;lib/logback-core-1.0.13.jar;lib/commons-dbcp-1.4.jar;lib/mysql-connector-java-5.1.38.jarcommons-lang-2.6.jar;lib/ojdbc6.jar;lib/commons-logging-1.2.jar;lib/postgresql-9.4.1207.jre7.jar;lib/commons-pool-1.6.jar;lib/slf4j-api-1.7.5.jar;lib/dom4j-1.6.1.jar;lib/xml-apis-1.0.b2.jar;lib/fastjson-1.2.8.jar;lib/zabbix-sender-0.0.1.jar;lib/httpclient-4.5.2.jar" --Install=%mypath%dbforbix32.exe --Jvm=auto --StartMode=jvm --StartClass=net.qdevzone.dbforbix.DBforBIX --StartMethod=start --StopMode=jvm --StopClass=com.smartmarmot.dbforbix.DBforBIX --StopMethod=stop
goto END

:64BIT
echo Installing 64bit service
%mypath%dbforbix64.exe //IS//DBforBIX --DisplayName="DBforBIX Zabbix Agent" --StartPath=%mypath% --LogPath="logs" --Classpath "dbforbix.jar;.;lib/commons-cli-1.3.1.jar;lib/httpcore-4.4.4.jar;lib/commons-codec-1.9.jar;lib/jtds-1.3.1.jar;lib/commons-collections-3.2.2.jar;lib/log4j-1.2.17.jar;lib/commons-configuration-1.10.jar;lib/logback-classic-1.0.13.jar;lib/commons-daemon-1.0.15.jar;lib/logback-core-1.0.13.jar;lib/commons-dbcp-1.4.jar;lib/mysql-connector-java-5.1.38.jarcommons-lang-2.6.jar;lib/ojdbc6.jar;lib/commons-logging-1.2.jar;lib/postgresql-9.4.1207.jre7.jar;lib/commons-pool-1.6.jar;lib/slf4j-api-1.7.5.jar;lib/dom4j-1.6.1.jar;lib/xml-apis-1.0.b2.jar;lib/fastjson-1.2.8.jar;lib/zabbix-sender-0.0.1.jar;lib/httpclient-4.5.2.jar" --Install=%mypath%dbforbix64.exe --Jvm=auto --StartMode=jvm --StartClass=net.qdevzone.dbforbix.DBforBIX --StartMethod=start --StopMode=jvm --StopClass=com.smartmarmot.dbforbix.DBforBIX --StopMethod=stop

:END
pause

