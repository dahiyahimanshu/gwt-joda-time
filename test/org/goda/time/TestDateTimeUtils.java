/*
 * Copyright 2001-2005 Stephen Colebourne Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.goda.time;

import java.lang.reflect.Modifier;

import junit.framework.TestSuite;

import org.goda.time.base.AbstractInstant;
import org.goda.time.chrono.BuddhistChronology;
import org.goda.time.chrono.CopticChronology;
import org.goda.time.chrono.GJChronology;
import org.goda.time.chrono.ISOChronology;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * This class is a Junit unit test for Instant.
 * @author Stephen Colebourne
 */
public class TestDateTimeUtils extends GWTTestCase {
  @Override
  public String getModuleName() {
    return "org.goda.Goda";
  }

  private static final GJChronology GJ = GJChronology.getInstance();
  private static final boolean OLD_JDK;
  static {
    String str = System.getProperty("java.version");
    boolean old = true;
    if (str.length() > 3 && str.charAt(0) == '1' && str.charAt(1) == '.'
        && (str.charAt(2) == '4' || str.charAt(2) == '5' || str.charAt(2) == '6')) {
      old = false;
    }
    OLD_JDK = old;
  }

  // Test in 2002/03 as time zones are more well known
  // (before the late 90's they were all over the place)

  private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
  private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");

  long y2002days = 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365
      + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365;
  long y2003days = 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365
      + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365;

  // 2002-06-09
  private long TEST_TIME_NOW = (y2002days + 31L + 28L + 31L + 30L + 31L + 9L - 1L) * DateTimeConstants.MILLIS_PER_DAY;

  // 2002-04-05
  private long TEST_TIME1 = (y2002days + 31L + 28L + 31L + 5L - 1L) * DateTimeConstants.MILLIS_PER_DAY + 12L
      * DateTimeConstants.MILLIS_PER_HOUR + 24L * DateTimeConstants.MILLIS_PER_MINUTE;

  // 2003-05-06
  private long TEST_TIME2 = (y2003days + 31L + 28L + 31L + 30L + 6L - 1L) * DateTimeConstants.MILLIS_PER_DAY + 14L
      * DateTimeConstants.MILLIS_PER_HOUR + 28L * DateTimeConstants.MILLIS_PER_MINUTE;

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static TestSuite suite() {
    return new TestSuite(TestDateTimeUtils.class);
  }

  @Override
  protected void gwtSetUp() throws Exception {
  }

  @Override
  protected void gwtTearDown() throws Exception {
  }

  //-----------------------------------------------------------------------
  public void testTest() {
    assertEquals("2002-06-09T00:00:00.000Z", new Instant(TEST_TIME_NOW).toString());
    assertEquals("2002-04-05T12:24:00.000Z", new Instant(TEST_TIME1).toString());
    assertEquals("2003-05-06T14:28:00.000Z", new Instant(TEST_TIME2).toString());
  }

  //-----------------------------------------------------------------------
  public void testClass() {
    Class cls = DateTimeUtils.class;
    assertEquals(true, Modifier.isPublic(cls.getModifiers()));
    assertEquals(false, Modifier.isFinal(cls.getModifiers()));

    assertEquals(1, cls.getDeclaredConstructors().length);
    assertEquals(true, Modifier.isProtected(cls.getDeclaredConstructors()[0].getModifiers()));

    DateTimeUtils utils = new DateTimeUtils() {
    };
  }

  //-----------------------------------------------------------------------
  public void testSystemMillis() {
    long nowSystem = System.currentTimeMillis();
    long now = DateTimeUtils.currentTimeMillis();
    assertTrue((now >= nowSystem));
    assertTrue((now - nowSystem) < 10000L);
  }

  //-----------------------------------------------------------------------
  public void testFixedMillis() {
    try {
      DateTimeUtils.setCurrentMillisFixed(0L);
      assertEquals(0L, DateTimeUtils.currentTimeMillis());
      assertEquals(0L, DateTimeUtils.currentTimeMillis());
      assertEquals(0L, DateTimeUtils.currentTimeMillis());
    } finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
    long nowSystem = System.currentTimeMillis();
    long now = DateTimeUtils.currentTimeMillis();
    assertTrue((now >= nowSystem));
    assertTrue((now - nowSystem) < 10000L);
  }

  //-----------------------------------------------------------------------
  public void testOffsetMillis() {
    try {
      // set time to one day ago
      DateTimeUtils.setCurrentMillisOffset(-24 * 60 * 60 * 1000);
      long nowSystem = System.currentTimeMillis();
      long now = DateTimeUtils.currentTimeMillis();
      long nowAdjustDay = now + (24 * 60 * 60 * 1000);
      assertTrue((now < nowSystem));
      assertTrue((nowAdjustDay >= nowSystem));
      assertTrue((nowAdjustDay - nowSystem) < 10000L);
    } finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
    long nowSystem = System.currentTimeMillis();
    long now = DateTimeUtils.currentTimeMillis();
    assertTrue((now >= nowSystem));
    assertTrue((now - nowSystem) < 10000L);
  }

  //-----------------------------------------------------------------------
  public void testOffsetMillisToZero() {
    long now1 = 0L;
    try {
      // set time to one day ago
      DateTimeUtils.setCurrentMillisOffset(0);
      now1 = DateTimeUtils.currentTimeMillis();
    } finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
    long now2 = DateTimeUtils.currentTimeMillis();
    assertEquals(now1, now2);
  }

  //-----------------------------------------------------------------------
  public void testGetInstantMillis_RI() {
    Instant i = new Instant(123L);
    assertEquals(123L, DateTimeUtils.getInstantMillis(i));
    try {
      DateTimeUtils.setCurrentMillisFixed(TEST_TIME_NOW);
      assertEquals(TEST_TIME_NOW, DateTimeUtils.getInstantMillis(null));
    } finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
  }

  //-----------------------------------------------------------------------
  public void testGetInstantChronology_RI() {
    DateTime dt = new DateTime(123L, BuddhistChronology.getInstance());
    assertEquals(BuddhistChronology.getInstance(), DateTimeUtils.getInstantChronology(dt));

    Instant i = new Instant(123L);
    assertEquals(ISOChronology.getInstanceUTC(), DateTimeUtils.getInstantChronology(i));

    AbstractInstant ai = new AbstractInstant() {
      public long getMillis() {
        return 0L;
      }

      public Chronology getChronology() {
        return null; // testing for this
      }
    };
    assertEquals(ISOChronology.getInstance(), DateTimeUtils.getInstantChronology(ai));

    assertEquals(ISOChronology.getInstance(), DateTimeUtils.getInstantChronology(null));
  }

  //-----------------------------------------------------------------------
  public void testGetIntervalChronology_RInterval() {
    Interval dt = new Interval(123L, 456L, BuddhistChronology.getInstance());
    assertEquals(BuddhistChronology.getInstance(), DateTimeUtils.getIntervalChronology(dt));

    assertEquals(ISOChronology.getInstance(), DateTimeUtils.getIntervalChronology(null));

    MutableInterval ai = new MutableInterval() {
      public Chronology getChronology() {
        return null; // testing for this
      }
    };
    assertEquals(ISOChronology.getInstance(), DateTimeUtils.getIntervalChronology(ai));
  }

  //-----------------------------------------------------------------------
  public void testGetIntervalChronology_RI_RI() {
    DateTime dt1 = new DateTime(123L, BuddhistChronology.getInstance());
    DateTime dt2 = new DateTime(123L, CopticChronology.getInstance());
    assertEquals(BuddhistChronology.getInstance(), DateTimeUtils.getIntervalChronology(dt1, dt2));
    assertEquals(BuddhistChronology.getInstance(), DateTimeUtils.getIntervalChronology(dt1, null));
    assertEquals(CopticChronology.getInstance(), DateTimeUtils.getIntervalChronology(null, dt2));
    assertEquals(ISOChronology.getInstance(), DateTimeUtils.getIntervalChronology(null, null));
  }

  //-----------------------------------------------------------------------
  public void testGetReadableInterval_ReadableInterval() {
    ReadableInterval input = new Interval(0, 100L);
    assertEquals(input, DateTimeUtils.getReadableInterval(input));

    try {
      DateTimeUtils.setCurrentMillisFixed(TEST_TIME_NOW);
      assertEquals(new Interval(TEST_TIME_NOW, TEST_TIME_NOW), DateTimeUtils.getReadableInterval(null));
    } finally {
      DateTimeUtils.setCurrentMillisSystem();
    }
  }

  //-----------------------------------------------------------------------
  public void testGetChronology_Chronology() {
    assertEquals(BuddhistChronology.getInstance(), DateTimeUtils.getChronology(BuddhistChronology.getInstance()));
    assertEquals(ISOChronology.getInstance(), DateTimeUtils.getChronology(null));
  }

  //-----------------------------------------------------------------------
  public void testGetZone_Zone() {
    assertEquals(PARIS, DateTimeUtils.getZone(PARIS));
    assertEquals(DateTimeZone.getDefault(), DateTimeUtils.getZone(null));
  }

  //-----------------------------------------------------------------------
  public void testGetPeriodType_PeriodType() {
    assertEquals(PeriodType.dayTime(), DateTimeUtils.getPeriodType(PeriodType.dayTime()));
    assertEquals(PeriodType.standard(), DateTimeUtils.getPeriodType(null));
  }

  //-----------------------------------------------------------------------
  public void testGetDurationMillis_RI() {
    Duration dur = new Duration(123L);
    assertEquals(123L, DateTimeUtils.getDurationMillis(dur));
    assertEquals(0L, DateTimeUtils.getDurationMillis(null));
  }

  //-----------------------------------------------------------------------
  public void testIsContiguous_RP() {
    YearMonthDay ymd = new YearMonthDay(2005, 6, 9);
    assertEquals(true, DateTimeUtils.isContiguous(ymd));
    TimeOfDay tod = new TimeOfDay(12, 20, 30, 0);
    assertEquals(true, DateTimeUtils.isContiguous(tod));
    Partial year = new Partial(DateTimeFieldType.year(), 2005);
    assertEquals(true, DateTimeUtils.isContiguous(year));
    Partial hourOfDay = new Partial(DateTimeFieldType.hourOfDay(), 12);
    assertEquals(true, DateTimeUtils.isContiguous(hourOfDay));
    Partial yearHour = year.with(DateTimeFieldType.hourOfDay(), 12);
    assertEquals(false, DateTimeUtils.isContiguous(yearHour));
    Partial ymdd = new Partial(ymd).with(DateTimeFieldType.dayOfWeek(), 2);
    assertEquals(false, DateTimeUtils.isContiguous(ymdd));
    Partial dd = new Partial(DateTimeFieldType.dayOfMonth(), 13).with(DateTimeFieldType.dayOfWeek(), 5);
    assertEquals(false, DateTimeUtils.isContiguous(dd));

    try {
      DateTimeUtils.isContiguous((ReadablePartial) null);
      fail();
    } catch (IllegalArgumentException ex) {
    }
  }

  //-----------------------------------------------------------------------
  public void testIsContiguous_RP_GJChronology() {
    YearMonthDay ymd = new YearMonthDay(2005, 6, 9, GJ);
    assertEquals(true, DateTimeUtils.isContiguous(ymd));
    TimeOfDay tod = new TimeOfDay(12, 20, 30, 0, GJ);
    assertEquals(true, DateTimeUtils.isContiguous(tod));
    Partial year = new Partial(DateTimeFieldType.year(), 2005, GJ);
    assertEquals(true, DateTimeUtils.isContiguous(year));
    Partial hourOfDay = new Partial(DateTimeFieldType.hourOfDay(), 12, GJ);
    assertEquals(true, DateTimeUtils.isContiguous(hourOfDay));
    Partial yearHour = year.with(DateTimeFieldType.hourOfDay(), 12);
    assertEquals(false, DateTimeUtils.isContiguous(yearHour));
    Partial ymdd = new Partial(ymd).with(DateTimeFieldType.dayOfWeek(), 2);
    assertEquals(false, DateTimeUtils.isContiguous(ymdd));
    Partial dd = new Partial(DateTimeFieldType.dayOfMonth(), 13).with(DateTimeFieldType.dayOfWeek(), 5);
    assertEquals(false, DateTimeUtils.isContiguous(dd));

    try {
      DateTimeUtils.isContiguous((ReadablePartial) null);
      fail();
    } catch (IllegalArgumentException ex) {
    }
  }

}
