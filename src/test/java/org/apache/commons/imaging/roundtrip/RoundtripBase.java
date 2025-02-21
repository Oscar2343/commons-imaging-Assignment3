/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.imaging.roundtrip;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Stream;

import org.apache.commons.imaging.AbstractImageParser;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.ImagingParameters;
import org.apache.commons.imaging.common.RgbBufferedImageFactory;
import org.apache.commons.imaging.formats.gif.GifImagingParameters;
import org.apache.commons.imaging.internal.ImageParserFactory;
import org.junit.jupiter.params.provider.Arguments;

public class RoundtripBase {
    private static boolean hasXmpData = false;

    public static Stream<Arguments> createRoundtripArguments(final BufferedImage[] images) {
        return Stream.of(images).flatMap(i -> Stream.of(FormatInfo.READ_WRITE_FORMATS).map(f -> Arguments.of(i, f)));
    }

    protected void roundtrip(final FormatInfo formatInfo, final BufferedImage testImage, final String tempPrefix, final boolean imageExact)
            throws IOException, ImagingException, ImagingException {

        final AbstractImageParser abstractImageParser = ImageParserFactory.getImageParser(formatInfo.format);

        final ImagingParameters params = ImageParserFactory.getImageParser(formatInfo.format).getDefaultParameters();

        if (!hasXmpData && params instanceof GifImagingParameters) {
            hasXmpData = true;
            String xmpData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n" +
                    "   <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                    "       <rdf:Description rdf:about=\"\"\n" +
                    "           xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\">\n" +
                    "           <xmp:CreatorTool>My GIF Creator</xmp:CreatorTool>\n" +
                    "       </rdf:Description>\n" +
                    "   </rdf:RDF>\n" +
                    "</x:xmpmeta>";
            ((GifImagingParameters) params).setXmpXml(xmpData);
        }

        final byte[] temp1;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            abstractImageParser.writeImage(testImage, bos, params);
            temp1 = bos.toByteArray();
        }

        final ImagingParameters readParams = ImageParserFactory.getImageParser(formatInfo.format).getDefaultParameters();
        readParams.setBufferedImageFactory(new RgbBufferedImageFactory());
        final BufferedImage image2 = abstractImageParser.getBufferedImage(temp1, readParams);
        assertNotNull(image2);

        if (imageExact) {
            // note tolerance when comparing grayscale images
            // BufferedImages of
            ImageAsserts.assertImageEquals(testImage, image2);
        }

        if (formatInfo.identicalSecondWrite) {
            final byte[] temp2;
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                abstractImageParser.writeImage(image2, bos, params);
                temp2 = bos.toByteArray();
            }

            assertArrayEquals(temp1, temp2);
        }
    }
}
