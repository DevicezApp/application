#!/bin/bash

if ([[ -f DeviceZAgent-update.jar ]]) && (! [[ -f update-lock ]]); then
  /usr/bin/env java -jar DeviceZAgent-update.jar
  /usr/bin/env touch update-lock
else
  /usr/bin/env java -jar DeviceZAgent.jar
fi
