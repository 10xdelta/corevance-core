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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import com.corevance.infrastructure.core.service.MDCWrapper;
import com.corevance.infrastructure.security.utils.LogParameterEscapeUtil;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class CorrelationHeaderFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_KEY = "correlationId";

    private final CorevanceProperties corevanceProperties;
    private final MDCWrapper mdcWrapper;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException {
        CorevanceProperties.CorevanceCorrelationProperties correlationProperties = corevanceProperties.getCorrelation();
        if (correlationProperties.isEnabled()) {
            handleCorrelations(request, response, filterChain, correlationProperties);
        } else {
            filterChain.doFilter(request, response);
        }

    }

    private void handleCorrelations(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain,
            CorevanceProperties.CorevanceCorrelationProperties correlationProperties) throws IOException, ServletException {
        try {
            String correlationHeaderName = correlationProperties.getHeaderName();
            String correlationId = request.getHeader(correlationHeaderName);
            if (StringUtils.isNotBlank(correlationId)) {
                String escapedCorrelationId = LogParameterEscapeUtil.escapeLogMDCParameter(correlationId);
                log.debug("Found correlationId in header : {}", escapedCorrelationId);
                mdcWrapper.put(CORRELATION_ID_KEY, escapedCorrelationId);
            }
            filterChain.doFilter(request, response);
        } finally {
            mdcWrapper.remove(CORRELATION_ID_KEY);
        }
    }

    @Override
    protected boolean isAsyncDispatch(final HttpServletRequest request) {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

}
