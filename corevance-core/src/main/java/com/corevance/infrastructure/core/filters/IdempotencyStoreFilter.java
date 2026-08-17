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
package com.corevance.infrastructure.core.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import com.corevance.commands.service.SynchronousCommandProcessingService;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import com.corevance.infrastructure.core.domain.CorevanceRequestContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

@RequiredArgsConstructor
@Slf4j
public class IdempotencyStoreFilter extends OncePerRequestFilter {

    private final CorevanceRequestContextHolder corevanceRequestContextHolder;
    private final IdempotencyStoreHelper helper;
    private final CorevanceProperties corevanceProperties;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        Mutable<ContentCachingResponseWrapper> wrapper = new MutableObject<>();
        if (helper.isAllowedContentTypeRequest(request)) {
            wrapper.setValue(new ContentCachingResponseWrapper(response));
        }
        extractIdempotentKeyFromHttpServletRequest(request).ifPresent(idempotentKey -> corevanceRequestContextHolder
                .setAttribute(SynchronousCommandProcessingService.IDEMPOTENCY_KEY_ATTRIBUTE, idempotentKey, request));

        filterChain.doFilter(request, wrapper.get() != null ? wrapper.get() : response);
        Optional<Long> commandId = helper.getCommandId(request);
        boolean isSuccessWithoutStored = commandId.isPresent() && wrapper.get() != null && helper.isStoreIdempotencyKey(request)
                && helper.isAllowedContentTypeResponse(response);
        if (isSuccessWithoutStored) {
            helper.storeCommandResult(response.getStatus(), Optional.ofNullable(wrapper.get())
                    .map(ContentCachingResponseWrapper::getContentAsByteArray).map(s -> new String(s, StandardCharsets.UTF_8)).orElse(null),
                    commandId.get());
        }
        if (wrapper.get() != null) {
            wrapper.get().copyBodyToResponse();
        }
    }

    private Optional<String> extractIdempotentKeyFromHttpServletRequest(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(corevanceProperties.getIdempotencyKeyHeaderName()));
    }
}
