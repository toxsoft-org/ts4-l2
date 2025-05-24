#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

# run configuration ID
export USKAT_L2CORE_RUN_ID=run1_1

# locale
export USKAT_COUNTRY=EN
export USKAT_LANG=en
# timezone
export USKAT_TZ=Asia/Almaty


while true; do
   rm -f /var/lock/LCK..ttyUSB0
   rm -f /var/lock/LCK..ttyUSB1
   rm -f /var/lock/LCK..ttyUSB2

   ${JAVA_HOME}/bin/java                      \
     -Dl2.core.run_id=${USKAT_L2CORE_RUN_ID}  \
     -Dlog4j.configuration=log4j.xml          \
     -Djava.library.path=/usr/lib/jni         \
     -cp .:libs/*:libs/wildfly/*:libs-app/*   \
     -Duser.country=${USKAT_COUNTRY}          \
     -Duser.language=${USKAT_LANG}            \
     -Duser.timezone=${USKAT_TZ}              \
     ru.toxsoft.l2.core.main.L2CoreMain

   retcode=$?
   if((retcode == 130)); then
      # Ctrl+C
      break;
   fi
done

