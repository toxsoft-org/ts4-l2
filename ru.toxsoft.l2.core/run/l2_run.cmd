@echo on

set _REMOTE_DEBUG=-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000 

rem 2023-03-06 mvk---+++ memory sequestor
rem java -Xmx1536m -Xms1536m -Dlog4j.configuration=-filelog4j.xml -Dfile.encoding=CP866 -cp .;libs/*;libs/wildfly/* ru.toxsoft.l2.core.main.L2CoreMain 


java %_REMOTE_DEBUG% -Xmx512m -Xms512m -Dlog4j.configuration=-filelog4j.xml -Dfile.encoding=CP866 -cp ./;libs/*;libs/wildfly/* ru.toxsoft.l2.core.main.L2CoreMain 

