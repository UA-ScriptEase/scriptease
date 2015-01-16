#!/bin/bash
cd translators_src/nwn
ant install -Ddebug.mode=true
cd ../unity
ant install -Ddebug.mode=true
cd ../hackebot
ant install -Ddebug.mode=true
cd ../..
echo "All translators installed"

