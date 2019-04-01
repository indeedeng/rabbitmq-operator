#!/usr/bin/env bash
set -e

docker build -t indeedeng/rabbitmq-operator:latest .

if [ "$TRAVIS_PULL_REQUEST" != "false" ] ; then
    echo "built pull request, nothing left to do"
elif [[ ! -z "$TRAVIS_TAG" ]]; then
    echo "tag and push docker"
    echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
    docker tag indeedeng/rabbitmq-operator:latest indeedeng/rabbitmq-operator:$TRAVIS_TAG
    docker push indeedeng/rabbitmq-operator:$TRAVIS_TAG
else
    echo "nothing to do"
fi

docker images