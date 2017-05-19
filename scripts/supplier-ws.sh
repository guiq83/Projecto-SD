#!/bin/bash
cd ../supplier-ws
mvn compile exec:java
echo ""
echo "supplier-ws"
echo "[ENTER] to exit"
read word
