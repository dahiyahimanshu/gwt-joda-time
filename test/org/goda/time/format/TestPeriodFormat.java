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
package org.goda.time.format;

import java.util.Locale;
import java.util.TimeZone;

import junit.framework.TestSuite;

import org.goda.time.DateTimeConstants;
import org.goda.time.DateTimeUtils;
import org.goda.time.DateTimeZone;
import org.goda.time.Period;
import org.goda.time.PeriodType;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * This class is a Junit unit test for PeriodFormat.
 * @author Stephen Colebourne
 */
public class TestPeriodFormat extends GWTTestCase {
  @Override
  public String getModuleName() {
    return "org.goda.Goda";
  }

  private static final Period PERIOD = new Period(1, 2, 3, 4, 5, 6, 7, 8);
  private static final Period EMPTY_PERIOD = new Period(0, 0, 0, 0, 0, 0, 0, 0);
  private static final Period YEAR_DAY_PERIOD = new Period(1, 0, 0, 4, 5, 6, 7, 8, PeriodType.yearDayTime());
  private static final Period EMPTY_YEAR_DAY_PERIOD = new Period(0, 0, 0, 0, 0, 0, 0, 0, PeriodType.yearDayTime());
  private static final Period TIME_PERIOD = new Period(0, 0, 0, 0, 5, 6, 7, 8);
  private static final Period DATE_PERIOD = new Period(1, 2, 3, 4, 0, 0, 0, 0);

  private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
  private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");
  private static final DateTimeZone TOKYO = DateTimeZone.forID("Asia/Tokyo");

  long y2002days = 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365
      + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365;
  // 2002-06-09
  private long TEST_TIME_NOW = (y2002days + 31L + 28L + 31L + 30L + 31L + 9L - 1L) * DateTimeConstants.MILLIS_PER_DAY;

  private DateTimeZone originalDateTimeZone = null;
  private TimeZone originalTimeZone = null;
  private Locale originalLocale = null;

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static TestSuite suite() {
    return new TestSuite(TestPeriodFormat.class);
  }

  @Override
  protected void gwtSetUp() throws Exception {
    DateTimeUtils.setCurrentMillisFixed(TEST_TIME_NOW);
    originalDateTimeZone = DateTimeZone.getDefault();
    originalTimeZone = TimeZone.getDefault();
    originalLocale = Locale.getDefault();
    DateTimeZone.setDefault(LONDON);
    TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
    Locale.setDefault(Locale.UK);
  }

  @Override
  protected void gwtTearDown() throws Exception {
    DateTimeUtils.setCurrentMillisSystem();
    DateTimeZone.setDefault(originalDateTimeZone);
    TimeZone.setDefault(originalTimeZone);
    Locale.setDefault(originalLocale);
    originalDateTimeZone = null;
    originalTimeZone = null;
    originalLocale = null;
  }

  //-----------------------------------------------------------------------
  public void testSubclassableConstructor() {
    PeriodFormat f = new PeriodFormat() {
      // test constructor is protected
    };
    assertNotNull(f);
  }

  //-----------------------------------------------------------------------
  public void testFormatStandard() {
    Period p = new Period(0, 0, 0, 1, 5, 6, 7, 8);
    assertEquals("1 day, 5 hours, 6 minutes, 7 seconds and 8 milliseconds", PeriodFormat.getDefault().print(p));
  }

  //-----------------------------------------------------------------------
  public void testFormatOneField() {
    Period p = Period.days(2);
    assertEquals("2 days", PeriodFormat.getDefault().print(p));
  }

  //-----------------------------------------------------------------------
  public void testFormatTwoFields() {
    Period p = Period.days(2).withHours(5);
    assertEquals("2 days and 5 hours", PeriodFormat.getDefault().print(p));
  }

  //-----------------------------------------------------------------------
  public void testParseOneField() {
    Period p = Period.days(2);
    assertEquals(p, PeriodFormat.getDefault().parsePeriod("2 days"));
  }

  //-----------------------------------------------------------------------
  public void testParseTwoFields() {
    Period p = Period.days(2).withHours(5);
    assertEquals(p, PeriodFormat.getDefault().parsePeriod("2 days and 5 hours"));
  }

}
