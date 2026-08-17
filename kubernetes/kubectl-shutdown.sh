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

echo "Shutting down Corevance Kubernetes deployment..."

echo "Deleting mifos-community..."
kubectl delete -f corevance-mifoscommunity-deployment.yml --ignore-not-found=true

echo "Deleting corevance-server..."
kubectl delete -f corevance-server-deployment.yml --ignore-not-found=true

echo "Deleting corevancemysql..."
kubectl delete -f corevancemysql-deployment.yml --ignore-not-found=true
kubectl delete -f corevancemysql-configmap.yml --ignore-not-found=true

echo "Deleting secrets..."
kubectl delete secret corevance-tenants-db-secret --ignore-not-found=true

echo
echo "Corevance Kubernetes deployment has been shut down."
