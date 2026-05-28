#!/bin/bash
# export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export JAVA_HOME=/usr/lib/jvm/jdk-21-oracle-x64

# run configuration ID
export USKAT_L2CORE_RUN_ID=run

# memory
export USKAT_MEMORY=1024M

# locale
export USKAT_COUNTRY=RU
export USKAT_LANG=ru
# timezone
export USKAT_TZ=Asia/Irkutsk

_JVM_OPS_MEMORY="-Xms${USKAT_MEMORY} -Xmx${USKAT_MEMORY} -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m"
_JVM_OPS_DEBUG="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8790"

while true; do
   rm -f /var/lock/LCK..ttyUSB0
   rm -f /var/lock/LCK..ttyUSB1
   rm -f /var/lock/LCK..ttyUSB2

   ${JAVA_HOME}/bin/java                      \
     -Dl2.core.run_id=${USKAT_L2CORE_RUN_ID}  \
     ${_JVM_OPS_MEMORY}                       \
     ${_JVM_OPS_DEBUG}                        \
     -Dlog4j2.configurationFile=log4j2.xml    \
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

