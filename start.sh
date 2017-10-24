#!/bin/bash

cd /home/apps/apidb

LIBDIR="lib/"
LIB="./classes"
for i in `ls $LIBDIR`
do
	LIB=$LIB:$LIBDIR$i;
	echo $i"\n"
done
echo ${LIB}



if [ ! -f app.pid ]
         then
~/src/jdk1.8.0_65/bin/java -Xss128m -Xms256m -Xmn128m -Xmx512m -verbose:gc -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -cp $LIB org.api.db.App conf/mdb.conf > out.txt 2>&1 & echo $! > app.pid
echo "done"
        else echo "udah jalan ? "
fi

