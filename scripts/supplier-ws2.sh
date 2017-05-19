#!/bin/bash
cd ../supplier-ws
mvn compile exec:java -Dws.i=2
echo ""
echo "supplier-ws"
echo "[ENTER] to exit"
read word
