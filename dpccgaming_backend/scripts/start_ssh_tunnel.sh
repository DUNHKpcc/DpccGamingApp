#!/usr/bin/env bash
set -e

if lsof -nP -iTCP:13306 -sTCP:LISTEN | grep -q ssh; then
  exit 0
fi

nohup ssh -N -L 13306:127.0.0.1:3306 root@39.96.164.116 \
  > /tmp/dpcc_ssh_tunnel.log 2>&1 &
