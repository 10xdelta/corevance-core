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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import com.corevance.infrastructure.core.service.MDCWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CorrelationHeaderFilterTest {

    private static final String CORRELATION_ID_HEADER_NAME = "X-CORR-ID";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private MDCWrapper mdcWrapper;

    private CorevanceProperties corevanceProperties;

    private CorrelationHeaderFilter underTest;

    @BeforeEach
    void setUp() {
        corevanceProperties = new CorevanceProperties();
        CorevanceProperties.CorevanceCorrelationProperties correlationProps = new CorevanceProperties.CorevanceCorrelationProperties();
        correlationProps.setHeaderName(CORRELATION_ID_HEADER_NAME);
        correlationProps.setEnabled(true);
        corevanceProperties.setCorrelation(correlationProps);
        underTest = new CorrelationHeaderFilter(corevanceProperties, mdcWrapper);
    }

    @Test
    public void testDoFilterInternalShouldPutCorrelationIdIntoMDCIfHeaderIsPresentAndEnabled() throws Exception {
        // given
        String correlationId = UUID.randomUUID().toString();
        given(request.getHeader(corevanceProperties.getCorrelation().getHeaderName())).willReturn(correlationId);

        // when
        underTest.doFilterInternal(request, response, filterChain);

        // then
        verify(mdcWrapper).put(CorrelationHeaderFilter.CORRELATION_ID_KEY, correlationId);
        verify(mdcWrapper).remove(CorrelationHeaderFilter.CORRELATION_ID_KEY);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testDoFilterInternalShouldNotPutCorrelationIdIntoMDCIfHeaderIsNotPresentAndEnabled() throws Exception {
        // given
        given(request.getHeader(corevanceProperties.getCorrelation().getHeaderName())).willReturn(null);

        // when
        underTest.doFilterInternal(request, response, filterChain);

        // then
        verify(mdcWrapper, never()).put(anyString(), anyString());
        verify(mdcWrapper).remove(CorrelationHeaderFilter.CORRELATION_ID_KEY);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testDoFilterInternalShouldNotPutCorrelationIdIntoMDCIfHeaderIsPresentButWhitespacesAndEnabled() throws Exception {
        // given
        given(request.getHeader(corevanceProperties.getCorrelation().getHeaderName())).willReturn("    ");

        // when
        underTest.doFilterInternal(request, response, filterChain);

        // then
        verify(mdcWrapper, never()).put(anyString(), anyString());
        verify(mdcWrapper).remove(CorrelationHeaderFilter.CORRELATION_ID_KEY);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testDoFilterInternalShouldNotPutCorrelationIdIntoMDCIfHeaderIsPresentAndDisabled() throws Exception {
        // given
        corevanceProperties.getCorrelation().setEnabled(false);
        String correlationId = UUID.randomUUID().toString();
        given(request.getHeader(corevanceProperties.getCorrelation().getHeaderName())).willReturn(correlationId);

        // when
        underTest.doFilterInternal(request, response, filterChain);

        // then
        verifyNoInteractions(mdcWrapper);
        verify(filterChain).doFilter(request, response);
    }
}
