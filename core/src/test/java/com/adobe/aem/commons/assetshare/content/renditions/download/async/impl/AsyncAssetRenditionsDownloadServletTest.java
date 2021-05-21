package com.adobe.aem.commons.assetshare.content.renditions.download.async.impl;

import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Rule;
import org.junit.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;

public class AsyncAssetRenditionsDownloadServletTest {
    @Rule
    public AemContext ctx = new AemContext();

    AsyncAssetRenditionsDownloadServlet servlet  = new AsyncAssetRenditionsDownloadServlet();

    @Test
    public void getLocalDateTime_EST() {
        ZonedDateTime now = ZonedDateTime.of(2021, 5, 6, 7, 0, 0, 0, ZoneId.of("UTC"));

        ZonedDateTime est = servlet.getZonedNowDateTime(now, "America/New_York");

        assertEquals(3, est.getHour());
        assertEquals(6, est.getDayOfMonth());
    }

    @Test
    public void getLocalDateTime_PST() {

        ZonedDateTime now = ZonedDateTime.of(2021, 5, 6, 6, 0, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime pst = servlet.getZonedNowDateTime(now, "America/Los_Angeles");

        assertEquals(23, pst.getHour());
        assertEquals(5, pst.getDayOfMonth());
    }
}