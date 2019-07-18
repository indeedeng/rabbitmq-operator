# Operator for RabbitMQ

## Overview
### Status: **Pre-alpha! Not for production use! Breaking changes can appear at anytime without notice!**

[![Build Status](https://travis-ci.org/indeedeng/rabbitmq-operator.svg?branch=master)](https://travis-ci.org/indeedeng/rabbitmq-operator) 
![OSS Lifecycle](https://img.shields.io/osslifecycle/indeedeng/rabbitmq-operator.svg)


Provision and manage RabbitMQ clusters on Kubernetes! This operator currently has the following features:
* Deploy N-node RabbitMQ clusters, utilizing auto-discovery for automatic clustering
* Scale cluster replicas, storage, and CPU
* Specify persistent volume storage class
* Expose clusters to external clients using a LoadBalancer
* Datadog auto-discovery annotations
* Safely resolve network partitions without dropping messages (experimental, requires manual custom resource creation)

# Getting Started
## Prerequisites
You must have a Kubernetes cluster. Standard Pod and Service networking must work.

You must also have a Docker registry that both your development environment and the Kubernetes cluster can access via the CNAME `registry.local.tld`

The example assumes you have Rook-managed storage deployed. You can read about Rook at https://rook.io/.

## Deploying the operator
Use the script `deploy-operator.sh` to build and push the operator image. At the end you should see a `rabbitmq-operator` pod spin up in the `rabbitmqs` namespace.
```
LOCAL_DOCKER_REGISTRY=registry.local.tld ./scripts/deploy-operator.sh
```

## Deploying a cluster
Apply the [example RabbitMQCustomResource](examples/rabbitmq_instance.yaml). By default, this deploys a cluster with 3 instances in the `rabbitmqs` namespace.
```
kubectl apply -f examples/rabbitmq_instance.yaml
```

## Connecting to the cluster
For each cluster, a service called `<cluster name>-svc` will be created. This is a standard (non-headless) service. Nodes will be added to the relevant Endpoints as soon as their healthcheck returns ok. A cluster named `myrabbitmq` in namespace `rabbitmqs` can be internally accessed at `myrabbitmq.rabbitmqs.svc.cluster.local`. Standard RabbitMQ ports are exposed.

To access a RabbitMQ cluster from outside the Kubernetes cluster, you need to either expose the Rabbit cluster using a NodePort or set `createLoadBalancer` to `true`. This will provision a LoadBalancer service with name `<cluster-name>-svc-lb` (assuming your environment supports it). You can then access your cluster using the LoadBalancer IP and standard RabbitMQ ports.

For more information on Service DNS and routing, see https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/.

# Custom Resource Schema
## RabbitMQCustomResource spec - [example](examples/rabbitmq_instance.yaml)
| Field                | Type      | Description |
|----------------------|-----------|-------------|
| `rabbitMQImage`      | string    | Name of RabbitMQ image |
| `initContainerImage` | string    | Name of initContainer image|
| `createLoadBalancer` | boolean   | Whether to create a LoadBalancer service |
| `preserveOrphanPVCs` | boolean   | When scaling down a cluster, whether to preserve "orphaned" PVCs; this field is optional and defaults to false |
| `replicas`           | number    | Number of cluster nodes |
| `compute.cpuRequest` | string    | CPU request per node, ex: "500m" |
| `compute.memory`     | string    | Memory request per node, ex: "512Mi" |
| `storage.storageClassName`      | string    | Storage class to use for persistent storage (immutable) |
| `storage.limit`      | string    | PersistentVolume size per cluster node (immutable) |
| `clusterSpec.highWatermarkFraction`      | string    | RabbitMQ high watermark, ex: 0.4 |

**Note:** Scaling replicas down is a dangerous operation. The operator does not currently make any safety guarantees when scaling down replicas.

# Roadmap
This operator is very much a work-in-progress. Features that we want to implement in the near future include:
* Shovel configuration
* Policy configuration
* Improvements to user management

# Code of Conduct
Operator for RabbitMQ is governed by the [Contributor Covenant v 1.4.1](CODE_OF_CONDUCT.md).

# License
Operator for RabbitMQ is licensed under the [Apache 2 License](LICENSE).

# Creating a Release
Check the GitHub [releases page](https://github.com/indeedeng/rabbitmq-operator/releases) for the latest version and from that determine the next version to release. On master, create a tag (`git tag <the new version>`) and push it (`git push --tags`). [release.sh](release.sh) will see the tag is on master and push the new version to DockerHub automatically (via Travis).

[Draft a new release](https://github.com/indeedeng/rabbitmq-operator/releases/new), put the tag you just created in the "tag version" box, and copy everything from [CHANGELOG.md](CHANGELOG.md) into the release description.

Finally, add a `Bugs`, `Improvements`, and `New Features` section for the next version.