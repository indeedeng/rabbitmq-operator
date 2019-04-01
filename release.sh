#!/usr/bin/env bash
set -e

docker build -t indeedoss/rabbitmq-operator:latest .

if [ "$TRAVIS_PULL_REQUEST" != "false" ] ; then
    echo "built pull request, nothing left to do"
elif [[ ! -z "$TRAVIS_TAG" ]]; then
    echo "tag and push docker"
    echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
    docker tag indeedoss/rabbitmq-operator:latest indeedoss/rabbitmq-operator:$TRAVIS_TAG
    docker push indeedoss/rabbitmq-operator:$TRAVIS_TAG
else
    echo "nothing to do"
fi

docker images