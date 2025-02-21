package org.apache.commons.imaging.roundtrip;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

public class ExtendedColorRoundtripTest extends RoundtripBase {

    @Test
    public void testExtendedColorImage_2Colors() throws Exception {
        BufferedImage testImage = TestImages.createExtendedColorImage(10, 10, 2);
        FormatInfo formatInfo = FormatInfo.READ_WRITE_FORMATS[0];
        boolean imageExact = formatInfo.colorSupport != FormatInfo.COLOR_BITMAP
                && formatInfo.colorSupport != FormatInfo.COLOR_GRAYSCALE;

        roundtrip(formatInfo, testImage, "indexable_2", imageExact);
    }

    @Test
    public void testExtendedColorImage_4Colors() throws Exception {
        BufferedImage testImage = TestImages.createExtendedColorImage(10, 10, 4);
        FormatInfo formatInfo = FormatInfo.READ_WRITE_FORMATS[0];

        boolean imageExact = formatInfo.colorSupport != FormatInfo.COLOR_BITMAP
                && formatInfo.colorSupport != FormatInfo.COLOR_GRAYSCALE;

        roundtrip(formatInfo, testImage, "indexable_4", imageExact);
    }

    @Test
    public void testExtendedColorImage_8Colors() throws Exception {
        BufferedImage testImage = TestImages.createExtendedColorImage(10, 10, 8);
        FormatInfo formatInfo = FormatInfo.READ_WRITE_FORMATS[0];

        boolean imageExact = formatInfo.colorSupport != FormatInfo.COLOR_BITMAP
                && formatInfo.colorSupport != FormatInfo.COLOR_GRAYSCALE;

        roundtrip(formatInfo, testImage, "indexable_8", imageExact);
    }

    @Test
    public void testExtendedColorImage_16Colors() throws Exception {
        BufferedImage testImage = TestImages.createExtendedColorImage(20, 20, 16);
        FormatInfo formatInfo = FormatInfo.READ_WRITE_FORMATS[0];

        boolean imageExact = formatInfo.colorSupport != FormatInfo.COLOR_BITMAP
                && formatInfo.colorSupport != FormatInfo.COLOR_GRAYSCALE;

        roundtrip(formatInfo, testImage, "indexable_16", imageExact);
    }

    @Test
    public void testExtendedColorImage_32Colors() throws Exception {
        BufferedImage testImage = TestImages.createExtendedColorImage(20, 20, 32);
        FormatInfo formatInfo = FormatInfo.READ_WRITE_FORMATS[0];

        boolean imageExact = formatInfo.colorSupport != FormatInfo.COLOR_BITMAP
                && formatInfo.colorSupport != FormatInfo.COLOR_GRAYSCALE;

        roundtrip(formatInfo, testImage, "indexable_32", imageExact);
    }

    @Test
    public void testExtendedColorImage_64Colors() throws Exception {
        BufferedImage testImage = TestImages.createExtendedColorImage(30, 30, 64);
        FormatInfo formatInfo = FormatInfo.READ_WRITE_FORMATS[0];

        boolean imageExact = formatInfo.colorSupport != FormatInfo.COLOR_BITMAP
                && formatInfo.colorSupport != FormatInfo.COLOR_GRAYSCALE;

        roundtrip(formatInfo, testImage, "indexable_64", imageExact);
    }

    @Test
    public void testExtendedColorImage_128Colors() throws Exception {
        BufferedImage testImage = TestImages.createExtendedColorImage(50, 50, 128);
        FormatInfo formatInfo = FormatInfo.READ_WRITE_FORMATS[0];

        boolean imageExact = formatInfo.colorSupport != FormatInfo.COLOR_BITMAP
                && formatInfo.colorSupport != FormatInfo.COLOR_GRAYSCALE;

        roundtrip(formatInfo, testImage, "indexable_128", imageExact);
    }

    @Test
    public void testExtendedColorImage_200Colors() throws Exception {
        BufferedImage testImage = TestImages.createExtendedColorImage(100, 100, 200);
        FormatInfo formatInfo = FormatInfo.READ_WRITE_FORMATS[0];

        boolean imageExact = formatInfo.colorSupport != FormatInfo.COLOR_BITMAP
                && formatInfo.colorSupport != FormatInfo.COLOR_GRAYSCALE;

        roundtrip(formatInfo, testImage, "indexable_200", imageExact);
    }
}
