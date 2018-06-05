#!/bin/bash

PROCESSES=$(ps aux | grep apache-maven | awk -F' ' '{print $2}')

for p in $PROCESSES ; do
  kill -9 $p 2>/dev/null
done 