#!/usr/bin/env bash
kubectl get secret $1-runtime-secret -o json | jq -r '.data["password"]' | base64 --decode
echo
