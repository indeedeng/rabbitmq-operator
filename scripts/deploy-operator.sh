#!/usr/bin/env bash

kubectl delete -f ./examples/rabbitmq_instance.yaml
kubectl delete -f ./examples/rabbitmq_operator.yaml
kubectl delete pvc -n rabbitmqs --all # PVCs are not deleted when pods are deleted, so clean them up when we destroy the cluster
kubectl delete pv -n rabbitmqs --all

gradle pushLocalImage && kubectl apply -f ./examples/rabbitmq_operator.yaml
