#!/bin/bash
cd scripts
mate-terminal --title="supplier-ws" --command "sh supplier-ws.sh"
sleep 3
mate-terminal --title="supplier-ws2" --command "sh supplier-ws2.sh"
sleep 3
mate-terminal --title="supplier-ws-cli" --command "sh supplier-ws-cli.sh"
sleep 3
mate-terminal --title="supplier-ws-cli2" --command "sh supplier-ws-cli.sh"
sleep 3
mate-terminal --title="mediator-ws_secondary" --command "sh mediator-ws_secondary.sh"
sleep 3
mate-terminal --title="mediator-ws_primary" --command "sh mediator-ws_primary.sh"
