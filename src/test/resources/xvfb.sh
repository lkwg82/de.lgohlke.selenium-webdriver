#!/bin/bash

set -e
#set +x

display=$1
cmd=$2

case $cmd in
 start)
    Xvfb :$display &
    echo -n $! > xvfb.$display.pid
    echo "started xvfb with display $display"
 ;;
 stop)
    echo -n "kill xfvb on display $display ... "
    kill $(cat xvfb.$display.pid) && echo ok && rm xvfb.$display.pid || echo failed
 ;;
 restart)
    $0 $display start
    $0 $display stop
 ;;
esac