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

package org.apache.commons.imaging.formats.tiff;

import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.ImagingFormatException;
import org.apache.commons.imaging.bytesource.ByteSource;
import java.nio.ByteOrder;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
/**
 * Tests Google oss-fuzz issue 53669.
 */
public class TiffImageParserTest {

    @Test
    public void testOssFuzzIssue53669() {
        assertThrows(ImagingFormatException.class,
                () -> new TiffImageParser().getBufferedImage(
                        ByteSource.file(new File(
                                "src/test/resources/images/tiff/oss-fuzz-53669/clusterfuzz-testcase-minimized-ImagingTiffFuzzer-5965016805539840.tiff")),
                        null));
    }
    private TiffDirectory directory;
    private ByteOrder byteOrder;
    private TiffImagingParameters params;

    @BeforeEach
    public void setUp() {
        directory = mock(TiffDirectory.class);
        byteOrder = ByteOrder.BIG_ENDIAN;
        params = mock(TiffImagingParameters.class);
    }

    @Test
    public void testMissingSampleFormatThrowsException() {
        // Setup directory to return null for sample format
        try {
            when(directory.getFieldValue(eq(TiffTagConstants.TIFF_TAG_SAMPLE_FORMAT), eq(true))).thenReturn(null);
        } catch (ImagingException e) {
            throw new RuntimeException(e);
        }

        // Verify exception is thrown with correct message
        ImagingException exception = assertThrows(ImagingException.class, () -> {
            final TiffImageParser parser = new TiffImageParser();
            parser.getRasterData(directory, byteOrder, params);
        });

        assertEquals("Directory does not specify numeric raster data", exception.getMessage());
    }

    @Test
    public void testUnsupportedFloatingPointBitsPerSampleThrowsException() throws ImagingException {
        // Setup directory with IEEE floating point format but invalid bits per sample
        setupBasicDirectory();
        when(directory.getFieldValue(eq(TiffTagConstants.TIFF_TAG_SAMPLE_FORMAT), eq(true)))
                .thenReturn(new short[]{TiffTagConstants.SAMPLE_FORMAT_VALUE_IEEE_FLOATING_POINT});

        TiffField bitsPerSampleField = mock(TiffField.class);
        when(bitsPerSampleField.getIntArrayValue()).thenReturn(new int[]{16}); // Invalid for float (not 32 or 64)
        when(directory.findField(TiffTagConstants.TIFF_TAG_BITS_PER_SAMPLE)).thenReturn(bitsPerSampleField);

        ImagingException exception = assertThrows(ImagingException.class, () -> {
            final TiffImageParser parser = new TiffImageParser();
            parser.getRasterData(directory, byteOrder, params);
        });

        assertEquals("TIFF floating-point data uses unsupported bits-per-sample: 16", exception.getMessage());
    }

    @Test
    public void testUnsupportedIntegerSamplesPerPixelThrowsException() throws ImagingException {
        // Setup directory with signed integer format but invalid samples per pixel
        setupBasicDirectory();
        when(directory.getFieldValue(eq(TiffTagConstants.TIFF_TAG_SAMPLE_FORMAT), eq(true)))
                .thenReturn(new short[]{TiffTagConstants.SAMPLE_FORMAT_VALUE_TWOS_COMPLEMENT_SIGNED_INTEGER});

        // Set samples per pixel to 2 (unsupported for integer data)
        TiffField samplesPerPixelField = mock(TiffField.class);
        when(samplesPerPixelField.getIntValue()).thenReturn(2);
        when(directory.findField(TiffTagConstants.TIFF_TAG_SAMPLES_PER_PIXEL)).thenReturn(samplesPerPixelField);

        ImagingException exception = assertThrows(ImagingException.class, () -> {
            final TiffImageParser parser = new TiffImageParser();
            parser.getRasterData(directory, byteOrder, params);
        });

        assertEquals("TIFF integer data uses unsupported samples per pixel: 2", exception.getMessage());
    }

    @Test
    public void testUnsupportedIntegerBitsPerPixelThrowsException() throws ImagingException {
        // Setup directory with signed integer format but invalid bits per pixel
        setupBasicDirectory();
        when(directory.getFieldValue(eq(TiffTagConstants.TIFF_TAG_SAMPLE_FORMAT), eq(true)))
                .thenReturn(new short[]{TiffTagConstants.SAMPLE_FORMAT_VALUE_TWOS_COMPLEMENT_SIGNED_INTEGER});

        TiffField bitsPerSampleField = mock(TiffField.class);
        when(bitsPerSampleField.getIntArrayValue()).thenReturn(new int[]{8}); // Invalid for integer (not 16 or 32)
        when(bitsPerSampleField.getIntValueOrArraySum()).thenReturn(8);
        when(directory.findField(TiffTagConstants.TIFF_TAG_BITS_PER_SAMPLE)).thenReturn(bitsPerSampleField);

        ImagingException exception = assertThrows(ImagingException.class, () -> {
            final TiffImageParser parser = new TiffImageParser();
            parser.getRasterData(directory, byteOrder, params);
        });

        assertEquals("TIFF integer data uses unsupported bits-per-pixel: 8", exception.getMessage());
    }

    @Test
    public void testUnsupportedIntegerPredictorThrowsException() throws ImagingException {
        // Setup directory with signed integer format but invalid predictor
        setupBasicDirectory();
        when(directory.getFieldValue(eq(TiffTagConstants.TIFF_TAG_SAMPLE_FORMAT), eq(true)))
                .thenReturn(new short[]{TiffTagConstants.SAMPLE_FORMAT_VALUE_TWOS_COMPLEMENT_SIGNED_INTEGER});

        TiffField bitsPerSampleField = mock(TiffField.class);
        when(bitsPerSampleField.getIntArrayValue()).thenReturn(new int[]{16}); // Valid for integer
        when(bitsPerSampleField.getIntValueOrArraySum()).thenReturn(16);
        when(directory.findField(TiffTagConstants.TIFF_TAG_BITS_PER_SAMPLE)).thenReturn(bitsPerSampleField);

        // Set an unsupported predictor value
        TiffField predictorField = mock(TiffField.class);
        when(predictorField.getIntValueOrArraySum()).thenReturn(3); // Unsupported value
        when(directory.findField(TiffTagConstants.TIFF_TAG_PREDICTOR)).thenReturn(predictorField);

        ImagingException exception = assertThrows(ImagingException.class, () -> {
            final TiffImageParser parser = new TiffImageParser();
            parser.getRasterData(directory, byteOrder, params);
        });

        assertEquals("TIFF integer data uses unsupported horizontal-differencing predictor", exception.getMessage());
    }

    @Test
    public void testUnsupportedSampleFormatThrowsException() throws ImagingException {
        // Setup directory with unsupported sample format
        setupBasicDirectory();
        when(directory.getFieldValue(eq(TiffTagConstants.TIFF_TAG_SAMPLE_FORMAT), eq(true)))
                .thenReturn(new short[]{99}); // Invalid sample format

        ImagingException exception = assertThrows(ImagingException.class, () -> {
            final TiffImageParser parser = new TiffImageParser();
            parser.getRasterData(directory, byteOrder, params);
        });

        assertEquals("TIFF does not provide a supported raster-data format", exception.getMessage());
    }

    // Helper method to setup basic directory structure
    private void setupBasicDirectory() throws ImagingException {
        // Setup minimal valid directory structure
        when(directory.getSingleFieldValue(TiffTagConstants.TIFF_TAG_IMAGE_WIDTH)).thenReturn(100);
        when(directory.getSingleFieldValue(TiffTagConstants.TIFF_TAG_IMAGE_LENGTH)).thenReturn(100);
    }

}
