#!/bin/bash
cd ../supplier-ws-cli
mvn compile exec:java
echo ""
echo "supplier-ws-cli"
echo "[ENTER] to exit"
read word
