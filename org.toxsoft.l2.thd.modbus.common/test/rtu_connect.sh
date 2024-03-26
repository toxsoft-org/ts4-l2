
java -Dlog4j.configuration=12345log4j.xml -cp .:libs-patches/tsjboss-ejb-client-1.0.5.Final.jar:libs/*:libs/wildfly/*:libs-app/* ru.toxsoft.l2.thd.modbus.common.test.TestRtuConnect "/dev/ttyUSB116" 

