#!/bin/bash
cd translators_src/nwn
ant install -Ddebug.mode=true
cd ../unity
ant install -Ddebug.mode=true
cd ../..
echo "Both translators installed"

