#!/usr/bin/env bash

# This assumes that there's only one operator instance
POD=`kubectl get pods --namespace rabbitmqs --no-headers | grep rabbitmq-operator | cut -d ' ' -f 1`
echo "Tailing pod $POD"
kubectl logs -f --namespace rabbitmqs $POD
