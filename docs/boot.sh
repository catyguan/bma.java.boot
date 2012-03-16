#!/bin/bash

BOOTPATH=./
BOOTPID=${BOOTPATH}boot.pid
BOOTLOG=${BOOTPATH}boot.log

JAVAHOME=/usr/local/jdk1.7.0_03
JAVAOPT=
JAVACP='./classes:./boot-1.0.0.jar:./libs/*.jar'

MAINCLASS='Boot$Wait'
MAINARGS="p1 p2 p3 p4"
WORKHOME=./

startBoot()
{

$JAVAHOME/bin/java $JAVAOPT -cp "$JAVACP" -Duser.dir="$WORKHOME" $MAINCLASS $MAINARGS >> $BOOTLOG 2>&1 &

pid="$!";
echo "$pid" > $BOOTPID
echo "start ... ">>${BOOTLOG}
echo "child pid is $pid">>${BOOTLOG}
echo "status is $?">>${BOOTLOG}
}

if [ -f $BOOTPID ]
then
    pid="`cat $BOOTPID`"
    if test `ps -p $pid | wc -l` -gt 1
    then
        echo "progress $pid exists!"
        exit       
    fi
fi

startBoot

while [ 1 ]
do
wait $pid
exitstatus="$?"
echo "**************************" >>${BOOTLOG}
echo "child pid=$pid is gone, $exitstatus" >>${BOOTLOG}
echo `date` >> ${BOOTLOG}
echo "**************************" >>${BOOTLOG}

sleep 5

startBoot

done
