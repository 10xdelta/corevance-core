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
package com.corevance.test.data.delinquency;

import static com.corevance.client.feign.util.FeignCalls.ok;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.DelinquencyBucketResponse;
import com.corevance.test.data.DelinquencyBucket;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DelinquencyBucketResolver {

    private final CorevanceFeignClient corevanceClient;

    @Cacheable(key = "#delinquencyBucket.name()", value = "delinquencyBucketsByName")
    public long resolve(DelinquencyBucket delinquencyBucket) {
        String delinquencyBucketName = delinquencyBucket.name();
        log.debug("Resolving account type by name [{}]", delinquencyBucketName);
        List<DelinquencyBucketResponse> delinquencyBucketResponses = ok(
                () -> corevanceClient.delinquencyRangeAndBucketsManagement().getBuckets());
        DelinquencyBucketResponse foundAtr = delinquencyBucketResponses.stream()//
                .filter(atr -> delinquencyBucketName.equals(atr.getName()))//
                .findAny()//
                .orElseThrow(() -> new IllegalArgumentException("Delinquency bucket [%s] not found".formatted(delinquencyBucketName)));//

        return foundAtr.getId();
    }
}
