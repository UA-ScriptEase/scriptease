#!/bin/bash
ant zip
cd translators_src/nwn
ant install -Ddebug.mode=true
cd ../unity
ant install -Ddebug.mode=true
cd ../hackebot
ant install -Ddebug.mode=true
echo "All translators installed"

