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
import java.util.Arrays;

import junit.framework.TestSuite;

import org.goda.time.Chronology;
import org.goda.time.DateTimeField;
import org.goda.time.DateTimeZone;
import org.goda.time.ReadablePartial;
import org.goda.time.TimeOfDay;
import org.goda.time.YearMonthDay;
import org.goda.time.base.BasePartial;
import org.goda.time.chrono.BuddhistChronology;
import org.goda.time.chrono.ISOChronology;
import org.goda.time.chrono.JulianChronology;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * This class is a Junit unit test for ReadablePartialConverter.
 * @author Stephen Colebourne
 */
public class TestReadablePartialConverter extends GWTTestCase {
  @Override
  public String getModuleName() {
    return "org.goda.Goda";
  }

  private static final DateTimeZone UTC = DateTimeZone.UTC;
  private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
  private static final Chronology ISO_PARIS = ISOChronology.getInstance(PARIS);
  private static Chronology JULIAN;
  private static Chronology ISO;
  private static Chronology BUDDHIST;

  private DateTimeZone zone = null;

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static TestSuite suite() {
    return new TestSuite(TestReadablePartialConverter.class);
  }

  @Override
  protected void gwtSetUp() throws Exception {
    JULIAN = JulianChronology.getInstance();
    ISO = ISOChronology.getInstance();
    BUDDHIST = BuddhistChronology.getInstance();
  }

  //-----------------------------------------------------------------------
  public void testSingleton() throws Exception {
    Class cls = ReadablePartialConverter.class;
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
    assertEquals(ReadablePartial.class, ReadablePartialConverter.INSTANCE.getSupportedType());
  }

  //-----------------------------------------------------------------------
  public void testGetChronology_Object_Zone() throws Exception {
    assertEquals(ISO_PARIS, ReadablePartialConverter.INSTANCE.getChronology(new TimeOfDay(123L), PARIS));
    assertEquals(ISO, ReadablePartialConverter.INSTANCE.getChronology(new TimeOfDay(123L), DateTimeZone.getDefault()));
    assertEquals(ISO, ReadablePartialConverter.INSTANCE.getChronology(new TimeOfDay(123L), (DateTimeZone) null));
  }

  public void testGetChronology_Object_Chronology() throws Exception {
    assertEquals(JULIAN, ReadablePartialConverter.INSTANCE.getChronology(new TimeOfDay(123L, BUDDHIST), JULIAN));
    assertEquals(JULIAN, ReadablePartialConverter.INSTANCE.getChronology(new TimeOfDay(123L), JULIAN));
    assertEquals(BUDDHIST.withUTC(), ReadablePartialConverter.INSTANCE.getChronology(new TimeOfDay(123L, BUDDHIST), (Chronology) null));
  }

  //-----------------------------------------------------------------------
  public void testGetPartialValues() throws Exception {
    TimeOfDay tod = new TimeOfDay();
    int[] expected = new int[] {1, 2, 3, 4};
    int[] actual = ReadablePartialConverter.INSTANCE.getPartialValues(tod, new TimeOfDay(1, 2, 3, 4), ISOChronology.getInstance(PARIS));
    assertEquals(true, Arrays.equals(expected, actual));

    try {
      ReadablePartialConverter.INSTANCE.getPartialValues(tod, new YearMonthDay(2005, 6, 9), JULIAN);
      fail();
    } catch (IllegalArgumentException ex) {
    }
    try {
      ReadablePartialConverter.INSTANCE.getPartialValues(tod, new MockTOD(), JULIAN);
      fail();
    } catch (IllegalArgumentException ex) {
    }
  }

  static class MockTOD extends BasePartial {
    protected DateTimeField getField(int index, Chronology chrono) {
      switch (index) {
        case 0:
          return chrono.hourOfDay();
        case 1:
          return chrono.minuteOfHour();
        case 2:
          return chrono.year();
        case 3:
          return chrono.era();
      }
      return null;
    }

    public int size() {
      return 4;
    }
  }

  //-----------------------------------------------------------------------
  public void testToString() {
    assertEquals("Converter[org.joda.time.ReadablePartial]", ReadablePartialConverter.INSTANCE.toString());
  }

}
