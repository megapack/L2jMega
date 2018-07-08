#! /bin/sh

java -Xmx512m -cp ./libs/*; net.sf.l2j.geodataconverter.GeoDataConverter > log/stdout.log 2>&1

