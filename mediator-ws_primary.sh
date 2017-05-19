#!/bin/bash
cd mediator-ws
mvn compile exec:java
echo ""
echo "mediator-ws_primary"
echo "[ENTER] to exit"
read word
