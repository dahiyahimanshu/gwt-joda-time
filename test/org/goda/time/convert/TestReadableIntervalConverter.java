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
package org.goda.time.convert;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import junit.framework.TestSuite;

import org.goda.time.Chronology;
import org.goda.time.DateTimeZone;
import org.goda.time.Interval;
import org.goda.time.MutableInterval;
import org.goda.time.MutablePeriod;
import org.goda.time.PeriodType;
import org.goda.time.ReadableInterval;
import org.goda.time.chrono.BuddhistChronology;
import org.goda.time.chrono.CopticChronology;
import org.goda.time.chrono.GJChronology;
import org.goda.time.chrono.ISOChronology;
import org.goda.time.chrono.JulianChronology;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * This class is a JUnit test for ReadableIntervalConverter.
 * @author Stephen Colebourne
 */
public class TestReadableIntervalConverter extends GWTTestCase {
  @Override
  public String getModuleName() {
    return "org.goda.Goda";
  }

  private static final DateTimeZone UTC = DateTimeZone.UTC;
  private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
  private static final Chronology ISO_PARIS = ISOChronology.getInstance(PARIS);
  private static Chronology JULIAN;
  private static Chronology ISO;

  private DateTimeZone zone = null;

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static TestSuite suite() {
    return new TestSuite(TestReadableIntervalConverter.class);
  }


  @Override
  protected void gwtSetUp() throws Exception {
    JULIAN = JulianChronology.getInstance();
    ISO = ISOChronology.getInstance();
  }

  //-----------------------------------------------------------------------
  public void testSingleton() throws Exception {
    Class cls = ReadableIntervalConverter.class;
    assertEquals(false, Modifier.isPublic(cls.getModifiers()));
    assertEquals(false, Modifier.isProtected(cls.getModifiers()));
    assertEquals(false, Modifier.isPrivate(cls.getModifiers()));

    Constructor con = cls.getDeclaredConstructor((Class[]) null);
    assertEquals(1, cls.getDeclaredConstructors().length);
    assertEquals(true, Modifier.isProtected(con.getModifiers()));

    Field fld = cls.getDeclaredField("INSTANCE");
    assertEquals(false, Modifier.isPublic(fld.getModifiers()));
    assertEquals(false, Modifier.isProtected(fld.getModifiers()));
    assertEquals(false, Modifier.isPrivate(fld.getModifiers()));
  }

  //-----------------------------------------------------------------------
  public void testSupportedType() throws Exception {
    assertEquals(ReadableInterval.class, ReadableIntervalConverter.INSTANCE.getSupportedType());
  }

  //-----------------------------------------------------------------------
  public void testGetDurationMillis_Object() throws Exception {
    Interval i = new Interval(100L, 223L);
    assertEquals(123L, ReadableIntervalConverter.INSTANCE.getDurationMillis(i));
  }

  //-----------------------------------------------------------------------
  public void testGetPeriodType_Object() throws Exception {
    Interval i = new Interval(100L, 223L);
    assertEquals(PeriodType.standard(), ReadableIntervalConverter.INSTANCE.getPeriodType(i));
  }

  public void testSetIntoPeriod_Object1() throws Exception {
    Interval i = new Interval(100L, 223L);
    MutablePeriod m = new MutablePeriod(PeriodType.millis());
    ReadableIntervalConverter.INSTANCE.setInto(m, i, null);
    assertEquals(0, m.getYears());
    assertEquals(0, m.getMonths());
    assertEquals(0, m.getWeeks());
    assertEquals(0, m.getDays());
    assertEquals(0, m.getHours());
    assertEquals(0, m.getMinutes());
    assertEquals(0, m.getSeconds());
    assertEquals(123, m.getMillis());
  }

  public void testSetIntoPeriod_Object2() throws Exception {
    Interval i = new Interval(100L, 223L);
    MutablePeriod m = new MutablePeriod(PeriodType.millis());
    ReadableIntervalConverter.INSTANCE.setInto(m, i, CopticChronology.getInstance());
    assertEquals(0, m.getYears());
    assertEquals(0, m.getMonths());
    assertEquals(0, m.getWeeks());
    assertEquals(0, m.getDays());
    assertEquals(0, m.getHours());
    assertEquals(0, m.getMinutes());
    assertEquals(0, m.getSeconds());
    assertEquals(123, m.getMillis());
  }

  //-----------------------------------------------------------------------
  public void testIsReadableInterval_Object_Chronology() throws Exception {
    Interval i = new Interval(1234L, 5678L);
    assertEquals(true, ReadableIntervalConverter.INSTANCE.isReadableInterval(i, null));
  }

  public void testSetIntoInterval_Object1() throws Exception {
    Interval i = new Interval(0L, 123L, CopticChronology.getInstance());
    MutableInterval m = new MutableInterval(-1000L, 1000L, BuddhistChronology.getInstance());
    ReadableIntervalConverter.INSTANCE.setInto(m, i, null);
    assertEquals(0L, m.getStartMillis());
    assertEquals(123L, m.getEndMillis());
    assertEquals(CopticChronology.getInstance(), m.getChronology());
  }

  public void testSetIntoInterval_Object2() throws Exception {
    Interval i = new Interval(0L, 123L, CopticChronology.getInstance());
    MutableInterval m = new MutableInterval(-1000L, 1000L, BuddhistChronology.getInstance());
    ReadableIntervalConverter.INSTANCE.setInto(m, i, GJChronology.getInstance());
    assertEquals(0L, m.getStartMillis());
    assertEquals(123L, m.getEndMillis());
    assertEquals(GJChronology.getInstance(), m.getChronology());
  }

  public void testSetIntoInterval_Object3() throws Exception {
    MutableInterval i = new MutableInterval(0L, 123L) {
      public Chronology getChronology() {
        return null; // bad
      }
    };
    MutableInterval m = new MutableInterval(-1000L, 1000L, BuddhistChronology.getInstance());
    ReadableIntervalConverter.INSTANCE.setInto(m, i, GJChronology.getInstance());
    assertEquals(0L, m.getStartMillis());
    assertEquals(123L, m.getEndMillis());
    assertEquals(GJChronology.getInstance(), m.getChronology());
  }

  public void testSetIntoInterval_Object4() throws Exception {
    MutableInterval i = new MutableInterval(0L, 123L) {
      public Chronology getChronology() {
        return null; // bad
      }
    };
    MutableInterval m = new MutableInterval(-1000L, 1000L, BuddhistChronology.getInstance());
    ReadableIntervalConverter.INSTANCE.setInto(m, i, null);
    assertEquals(0L, m.getStartMillis());
    assertEquals(123L, m.getEndMillis());
    assertEquals(ISOChronology.getInstance(), m.getChronology());
  }

  //-----------------------------------------------------------------------
  public void testToString() {
    assertEquals("Converter[org.joda.time.ReadableInterval]", ReadableIntervalConverter.INSTANCE.toString());
  }

}
