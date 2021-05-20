package com.adobe.aem.commons.assetshare.content.renditions.download.async.impl;

import io.wcm.testing.mock.aem.junit.AemContext;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Rule;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class AsyncAssetRenditionsDownloadServletTest {
    @Rule
    public AemContext ctx = new AemContext();

    AsyncAssetRenditionsDownloadServlet servlet  = new AsyncAssetRenditionsDownloadServlet();

    @Test
    public void getLocalDateTime_EST() {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/London"));
        cal.set(2021, 05, 20, 7, 0);

        Calendar est = servlet.getLocalDateTime(cal, "America/New_York");

        assertTrue("America/New_York".equals(est.getTimeZone().getID()));
        assertTrue(2 == est.get(Calendar.HOUR));
        assertTrue(20 == est.get(Calendar.DATE));
    }

    @Test
    public void getLocalDateTime_PST() {

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/London"));
        cal.set(2021, 05, 20, 1, 0);

        Calendar pst = servlet.getLocalDateTime(cal, "America/Los_Angeles");

        assertTrue("America/Los_Angeles".equals(pst.getTimeZone().getID()));
        assertTrue(6 == pst.get(Calendar.HOUR));
        assertTrue(19 == pst.get(Calendar.DATE));
    }
}