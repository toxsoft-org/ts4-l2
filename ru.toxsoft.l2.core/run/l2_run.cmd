@echo on

rem set _REMOTE_DEBUG=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000 

rem 2023-03-06 mvk---+++ memory sequestor
rem java -Xmx1536m -Xms1536m -Dlog4j.configuration=-filelog4j.xml -Dfile.encoding=CP866 -cp .;libs/*;libs/wildfly/* ru.toxsoft.l2.core.main.L2CoreMain

rem locale
set USKAT_COUNTRY=RU
set USKAT_LANG=ru

rem timezone
set USKAT_TZ=Asia/Irkutsk
 

java %_REMOTE_DEBUG% -Xmx512m -Xms512m  -Duser.country=%USKAT_COUNTRY% -Duser.language=%USKAT_LANG% -Duser.timezone=%USKAT_TZ% -Dlog4j2.configurationFile=log4j2.xml -Dfile.encoding=CP866 -cp ./;libs/* ru.toxsoft.l2.core.main.L2CoreMain 

