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
package com.corevance.infrastructure.contentstore.processor;

import static java.util.Objects.requireNonNullElse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64InputStream;
import com.corevance.infrastructure.contentstore.util.ContentPipe;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class Base64DecoderContentProcessor implements ContentProcessor {

    private static final String BASE64_DECODE_PREFIX = "base64.decode.";

    public static final String BASE64_DECODE_PARAM_BUFFER_SIZE = BASE64_DECODE_PREFIX + "buffer-size";

    private final ContentPipe pipe;
    private final CorevanceProperties properties;

    @Override
    public ContentProcessorContext process(final ContentProcessorContext ctx) {
        final Integer bufferSize = ctx.getParameter(BASE64_DECODE_PARAM_BUFFER_SIZE, Integer.class,
                requireNonNullElse(properties.getContent().getDefaultBufferSize(), 8192));

        final var pipedInputStream = pipe.pipe(ctx.getInputStream(), (in, out) -> {
            pipe.write(new Base64InputStream(in), out, new byte[bufferSize]);
        });

        return ctx.clone(pipedInputStream);
    }
}
