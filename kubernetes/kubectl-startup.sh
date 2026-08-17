#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#

set -e

echo "Setting up Corevance service configuration..."
kubectl create secret generic corevance-tenants-db-secret --from-literal=username=root --from-literal=password=$(head /dev/urandom | LC_CTYPE=C tr -dc A-Za-z0-9 | head -c 16) 2>/dev/null || echo "Secret already exists, skipping..."
kubectl apply -f corevancemysql-configmap.yml

echo
echo "Starting corevancemysql..."
kubectl apply -f corevancemysql-deployment.yml

echo "Waiting for corevancemysql to be ready..."
kubectl wait --for=condition=ready pod -l tier=corevancemysql --timeout=300s

echo
echo "Starting corevance server..."
kubectl apply -f corevance-server-deployment.yml

echo "Waiting for corevance-server to be ready..."
kubectl wait --for=condition=ready pod -l app=corevance-server --timeout=300s

echo "Corevance server is up and running"

echo
echo "Starting Mifos Community UI..."
kubectl apply -f corevance-mifoscommunity-deployment.yml

echo "Waiting for mifos-community to be ready..."
kubectl wait --for=condition=ready pod -l app=mifos-community --timeout=300s

echo "Mifos Community UI is up and running"

echo
echo "============================================"
echo "Corevance Kubernetes deployment is ready!"
echo "============================================"
echo
echo "To access the Mifos web application:"
echo "  minikube service mifos-community"
echo
echo "To access the Corevance API directly:"
echo "  minikube service corevance-server --url --https"
echo
echo "Default credentials:"
echo "  Username: mifos"
echo "  Password: password"
echo
echo "To check pod status:"
echo "  kubectl get pods"
echo
echo "To view logs:"
echo "  kubectl logs deployment/corevance-server"
echo "  kubectl logs deployment/mifos-community"
echo
