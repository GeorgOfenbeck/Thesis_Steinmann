#!/bin/sh

COMMAND=$0
JDEPEND_DIR=`dirname $COMMAND`


java -classpath jdepend-2.9.1.jar:. jdepend.xmlui.JDepend -file jdepend.xml -components ch.ethz.ruediste.roofline.sharedEntities bin/

xsltproc jdepend2dot.xsl jdepend.xml > jdepend.dot

dot -Tsvg jdepend.dot -o jdepend-report.svg
