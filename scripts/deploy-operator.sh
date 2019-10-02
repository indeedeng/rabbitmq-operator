#!/usr/bin/env bash
gradle pushLocalImage && kubectl apply -f ./examples/rabbitmq_operator.yaml
