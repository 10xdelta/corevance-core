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
package com.corevance.infrastructure.businessdate.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.corevance.infrastructure.businessdate.domain.BusinessDateType;
import com.corevance.validation.constraints.DateFormat;
import com.corevance.validation.constraints.EnumValue;
import com.corevance.validation.constraints.LocalDate;
import com.corevance.validation.constraints.Locale;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@LocalDate(dateField = "date", formatField = "dateFormat", localeField = "locale")
public class BusinessDateUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{com.corevance.businessdate.date-format.not-blank}")
    @DateFormat
    private String dateFormat;
    @Schema(description = "Type of business date", example = "BUSINESS_DATE", allowableValues = { "BUSINESS_DATE", "COB_DATE" })
    @EnumValue(enumClass = BusinessDateType.class, message = "{com.corevance.businessdate.type.invalid}")
    @NotNull(message = "{com.corevance.businessdate.type.not-blank}")
    private String type;
    @NotBlank(message = "{com.corevance.businessdate.date.not-blank}")
    private String date;
    @NotBlank(message = "{com.corevance.businessdate.locale.not-blank}")
    @Locale
    private String locale;
}
