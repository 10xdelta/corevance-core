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
package com.corevance.infrastructure.instancemode.filter;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import com.corevance.infrastructure.instancemode.InstanceModeMock;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpMethod;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CorevanceInstanceModeApiFilterTest {

    @Mock
    private CorevanceProperties corevanceProperties;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter outputWriter;

    @InjectMocks
    private CorevanceInstanceModeApiFilter underTest;

    @BeforeEach
    void setUp() throws IOException {
        given(response.getWriter()).willReturn(outputWriter);
    }

    @Test
    void testDoFilterInternal_ShouldLetReadApisThrough_WhenCorevanceIsInAllModeAndIsGetApi() throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(true, true, true, true);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getPathInfo()).willReturn("/v1/loans");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetReadApisThrough_WhenCorevanceIsInReadOnlyModeAndIsGetApi() throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(true, false, false, false);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getPathInfo()).willReturn("/v1/loans");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetActuatorApisThrough_WhenCorevanceIsInReadOnlyModeAndIsHealthApi()
            throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(true, false, false, false);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getServletPath()).willReturn("/actuator/health");
        given(request.getPathInfo()).willReturn(null);
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldNotLetWriteApisThrough_WhenCorevanceIsInReadOnlyModeAndIsPostApi() throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(true, false, false, false);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/loans");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verifyNoInteractions(filterChain);
        verify(response).setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void testDoFilterInternal_ShouldNotLetBatchApisThrough_WhenCorevanceIsInReadOnlyModeAndIsJobsApi() throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(true, false, false, false);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/jobs/1");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verifyNoInteractions(filterChain);
        verify(response).setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void testDoFilterInternal_ShouldLetReadApisThrough_WhenCorevanceIsInWriteModeAndIsGetApi() throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(false, true, false, false);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getPathInfo()).willReturn("/v1/loans");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetWriteApisThrough_WhenCorevanceIsInWriteModeAndIsPostApi() throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(false, true, false, false);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/loans");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetWriteApisThrough_WhenCorevanceIsInWriteModeAndIsPutApi() throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(false, true, false, false);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.PUT.name());
        given(request.getPathInfo()).willReturn("/v1/loans/1");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetActuatorApisThrough_WhenCorevanceIsInWriteModeAndIsHelathApi() throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(false, true, false, false);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getServletPath()).willReturn("/actuator/health");
        given(request.getPathInfo()).willReturn(null);
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldNotLetBatchApisThrough_WhenCorevanceIsInWriteModeAndIsJobsApi() throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(false, true, false, false);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/jobs/1");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verifyNoInteractions(filterChain);
        verify(response).setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void testDoFilterInternal_ShouldLetBatchApisThrough_WhenCorevanceIsInBatchModeAndIsJobsApi() throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(false, false, true, true);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/jobs/1");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetBatchApisThrough_WhenCorevanceIsInBatchModeAndIsListingJobsApi()
            throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(false, false, true, true);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getPathInfo()).willReturn("/v1/jobs");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetActuatorApisThrough_WhenCorevanceIsInBatchModeAndIsHealthApi() throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(false, false, true, true);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getServletPath()).willReturn("/actuator/health");
        given(request.getPathInfo()).willReturn(null);
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetBatchesApisThrough_WhenCorevanceIsInReadMode() throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(true, false, false, false);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/batches");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetBatchesApisThrough_WhenCorevanceIsInWriteMode() throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(false, true, false, false);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/batches");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetBatchesApisThrough_WhenCorevanceIsInBatchMode() throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(false, false, true, true);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/batches");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetLoanCOBCatchUpApiThrough_WhenCorevanceIsInBatchManagerMode() throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(false, false, false, true);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/loans/catch-up");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldNotLetLoanCOBCatchUpApiThrough_WhenCorevanceIsNotInBatchManagerMode()
            throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(false, false, true, false);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/loans/catch-up");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verifyNoInteractions(filterChain);
        verify(response).setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void testDoFilterInternal_ShouldLetLoanCOBCatchUpStatusApiThrough_WhenCorevanceIsInBatchManagerMode()
            throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(false, false, false, true);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/loans/is-catch-up-running");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldNotLetLoanCOBCatchUpStatusApiThrough_WhenCorevanceIsNotInBatchManagerMode()
            throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(false, false, true, false);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/loans/is-catch-up-running");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verifyNoInteractions(filterChain);
        verify(response).setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void testDoFilterInternal_ShouldLetOtherLoanCatchUpApisThrough_WhenCorevanceIsInBatchManagerAndReadModeAndIsGetApi()
            throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(true, false, false, true);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getPathInfo()).willReturn("/v1/loans/oldest-cob-closed");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetOtherLoanCatchUpApisThrough_WhenCorevanceIsInReadModeAndIsGetApi()
            throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(true, false, false, false);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getPathInfo()).willReturn("/v1/loans/oldest-cob-closed");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetSchedulerApiThrough_WhenCorevanceIsInBatchManagerMode() throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(false, false, false, true);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/scheduler?command=start");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldNotLetSchedulerApiThrough_WhenCorevanceIsNotInBatchManagerMode() throws ServletException, IOException {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = InstanceModeMock.createModeProps(true, true, true, false);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/scheduler?command=start");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verifyNoInteractions(filterChain);
        verify(response).setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }
}
