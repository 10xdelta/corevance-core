/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.corevance.integrationtests.client.feign.helpers;

import static com.corevance.client.feign.util.FeignCalls.ok;

import java.util.List;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.DeleteDelinquencyBucketResponse;
import com.corevance.client.models.DeleteDelinquencyRangeResponse;
import com.corevance.client.models.DelinquencyBucketRequest;
import com.corevance.client.models.DelinquencyBucketResponse;
import com.corevance.client.models.DelinquencyRangeRequest;
import com.corevance.client.models.DelinquencyRangeResponse;
import com.corevance.client.models.PostDelinquencyBucketResponse;
import com.corevance.client.models.PostDelinquencyRangeResponse;
import com.corevance.client.models.PutDelinquencyBucketResponse;
import com.corevance.client.models.PutDelinquencyRangeResponse;

public class FeignDelinquencyHelper {

    private final CorevanceFeignClient corevanceClient;

    public FeignDelinquencyHelper(CorevanceFeignClient corevanceClient) {
        this.corevanceClient = corevanceClient;
    }

    public PostDelinquencyRangeResponse createRange(DelinquencyRangeRequest request) {
        return ok(() -> corevanceClient.delinquencyRangeAndBucketsManagement().createRange(request));
    }

    public DelinquencyRangeResponse getRange(Long rangeId) {
        return ok(() -> corevanceClient.delinquencyRangeAndBucketsManagement().getRange(rangeId));
    }

    public List<DelinquencyRangeResponse> getRanges() {
        return ok(() -> corevanceClient.delinquencyRangeAndBucketsManagement().getRanges());
    }

    public PutDelinquencyRangeResponse updateRange(Long rangeId, DelinquencyRangeRequest request) {
        return ok(() -> corevanceClient.delinquencyRangeAndBucketsManagement().updateRange(rangeId, request));
    }

    public DeleteDelinquencyRangeResponse deleteRange(Long rangeId) {
        return ok(() -> corevanceClient.delinquencyRangeAndBucketsManagement().deleteRange(rangeId));
    }

    public PostDelinquencyBucketResponse createBucket(DelinquencyBucketRequest request) {
        return ok(() -> corevanceClient.delinquencyRangeAndBucketsManagement().createBucket(request));
    }

    public DelinquencyBucketResponse getBucket(Long bucketId) {
        return ok(() -> corevanceClient.delinquencyRangeAndBucketsManagement().getBucket(bucketId));
    }

    public List<DelinquencyBucketResponse> getBuckets() {
        return ok(() -> corevanceClient.delinquencyRangeAndBucketsManagement().getBuckets());
    }

    public PutDelinquencyBucketResponse updateBucket(Long bucketId, DelinquencyBucketRequest request) {
        return ok(() -> corevanceClient.delinquencyRangeAndBucketsManagement().updateBucket(bucketId, request));
    }

    public DeleteDelinquencyBucketResponse deleteBucket(Long bucketId) {
        return ok(() -> corevanceClient.delinquencyRangeAndBucketsManagement().deleteBucket(bucketId));
    }
}
