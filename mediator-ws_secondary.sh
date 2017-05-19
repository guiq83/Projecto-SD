#!/bin/bash
cd mediator-ws
mvn compile exec:java -Dws.i=2
echo ""
echo "mediator-ws_secondary"
echo "[ENTER] to exit"
read word
