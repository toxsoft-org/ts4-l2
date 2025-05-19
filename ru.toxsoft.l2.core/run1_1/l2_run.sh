
while true; do
   rm -f /var/lock/LCK..ttyUSB0
   rm -f /var/lock/LCK..ttyUSB1
   rm -f /var/lock/LCK..ttyUSB2

   java -Dlog4j.configuration=log4j.xml -Djava.library.path=/usr/lib/jni -cp .:libs/*:libs/wildfly/*:libs-app/* ru.toxsoft.l2.core.main.L2CoreMain 
   retcode=$?
   if((retcode == 130)); then
      # Ctrl+C
      break;
   fi
done

