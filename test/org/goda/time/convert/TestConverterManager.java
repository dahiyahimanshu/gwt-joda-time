/*
 * Copyright 2001-2006 Stephen Colebourne Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.goda.time.convert;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestSuite;

import org.goda.time.Chronology;
import org.goda.time.DateTime;
import org.goda.time.DateTimeZone;
import org.goda.time.Duration;
import org.goda.time.Interval;
import org.goda.time.Period;
import org.goda.time.PeriodType;
import org.goda.time.ReadWritableInterval;
import org.goda.time.ReadWritablePeriod;
import org.goda.time.ReadableDateTime;
import org.goda.time.ReadableDuration;
import org.goda.time.ReadableInstant;
import org.goda.time.ReadableInterval;
import org.goda.time.ReadablePartial;
import org.goda.time.ReadablePeriod;
import org.goda.time.TimeOfDay;
import org.goda.time.format.DateTimeFormatter;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * This class is a JUnit test for ConverterManager.
 * @author Stephen Colebourne
 */
public class TestConverterManager extends GWTTestCase {
  @Override
  public String getModuleName() {
    return "org.goda.Goda";
  }

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

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static TestSuite suite() {
    return new TestSuite(TestConverterManager.class);
  }

  //-----------------------------------------------------------------------
  public void testSingleton() throws Exception {
    Class cls = ConverterManager.class;
    assertEquals(true, Modifier.isPublic(cls.getModifiers()));

    Constructor con = cls.getDeclaredConstructor((Class[]) null);
    assertEquals(1, cls.getDeclaredConstructors().length);
    assertEquals(true, Modifier.isProtected(con.getModifiers()));

    Field fld = cls.getDeclaredField("INSTANCE");
    assertEquals(true, Modifier.isPrivate(fld.getModifiers()));
  }

  //-----------------------------------------------------------------------
  public void testGetInstantConverter() {
    InstantConverter c = ConverterManager.getInstance().getInstantConverter(new Long(0L));
    assertEquals(Long.class, c.getSupportedType());

    c = ConverterManager.getInstance().getInstantConverter(new DateTime());
    assertEquals(ReadableInstant.class, c.getSupportedType());

    c = ConverterManager.getInstance().getInstantConverter("");
    assertEquals(String.class, c.getSupportedType());

    c = ConverterManager.getInstance().getInstantConverter(new Date());
    assertEquals(Date.class, c.getSupportedType());

    c = ConverterManager.getInstance().getInstantConverter(new GregorianCalendar());
    assertEquals(Calendar.class, c.getSupportedType());

    c = ConverterManager.getInstance().getInstantConverter(null);
    assertEquals(null, c.getSupportedType());

    try {
      ConverterManager.getInstance().getInstantConverter(Boolean.TRUE);
      fail();
    } catch (IllegalArgumentException ex) {
    }
  }

  public void testGetInstantConverterRemovedNull() {
    try {
      ConverterManager.getInstance().removeInstantConverter(NullConverter.INSTANCE);
      try {
        ConverterManager.getInstance().getInstantConverter(null);
        fail();
      } catch (IllegalArgumentException ex) {
      }
    } finally {
      ConverterManager.getInstance().addInstantConverter(NullConverter.INSTANCE);
    }
    assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
  }

  public void testGetInstantConverterOKMultipleMatches() {
    InstantConverter c = new InstantConverter() {
      public long getInstantMillis(Object object, Chronology chrono) {
        return 0;
      }

      public Chronology getChronology(Object object, DateTimeZone zone) {
        return null;
      }

      public Chronology getChronology(Object object, Chronology chrono) {
        return null;
      }

      public Class getSupportedType() {
        return ReadableDateTime.class;
      }
    };
    try {
      ConverterManager.getInstance().addInstantConverter(c);
      InstantConverter ok = ConverterManager.getInstance().getInstantConverter(new DateTime());
      // ReadableDateTime and ReadableInstant both match, but RI discarded as less specific
      assertEquals(ReadableDateTime.class, ok.getSupportedType());
    } finally {
      ConverterManager.getInstance().removeInstantConverter(c);
    }
    assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
  }

  public void testGetInstantConverterBadMultipleMatches() {
    InstantConverter c = new InstantConverter() {
      public long getInstantMillis(Object object, Chronology chrono) {
        return 0;
      }

      public Chronology getChronology(Object object, DateTimeZone zone) {
        return null;
      }

      public Chronology getChronology(Object object, Chronology chrono) {
        return null;
      }

      public Class getSupportedType() {
        return Serializable.class;
      }
    };
    try {
      ConverterManager.getInstance().addInstantConverter(c);
      try {
        ConverterManager.getInstance().getInstantConverter(new DateTime());
        fail();
      } catch (IllegalStateException ex) {
        // Serializable and ReadableInstant both match, so cannot pick
      }
    } finally {
      ConverterManager.getInstance().removeInstantConverter(c);
    }
    assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
  }

  //-----------------------------------------------------------------------
  public void testGetInstantConverters() {
    InstantConverter[] array = ConverterManager.getInstance().getInstantConverters();
    assertEquals(6, array.length);
  }

  //-----------------------------------------------------------------------
  public void testAddInstantConverter1() {
    InstantConverter c = new InstantConverter() {
      public long getInstantMillis(Object object, Chronology chrono) {
        return 0;
      }

      public Chronology getChronology(Object object, DateTimeZone zone) {
        return null;
      }

      public Chronology getChronology(Object object, Chronology chrono) {
        return null;
      }

      public Class getSupportedType() {
        return Boolean.class;
      }
    };
    try {
      InstantConverter removed = ConverterManager.getInstance().addInstantConverter(c);
      assertEquals(null, removed);
      assertEquals(Boolean.class, ConverterManager.getInstance().getInstantConverter(Boolean.TRUE).getSupportedType());
      assertEquals(7, ConverterManager.getInstance().getInstantConverters().length);
    } finally {
      ConverterManager.getInstance().removeInstantConverter(c);
    }
    assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
  }

  public void testAddInstantConverter2() {
    InstantConverter c = new InstantConverter() {
      public long getInstantMillis(Object object, Chronology chrono) {
        return 0;
      }

      public Chronology getChronology(Object object, DateTimeZone zone) {
        return null;
      }

      public Chronology getChronology(Object object, Chronology chrono) {
        return null;
      }

      public Class getSupportedType() {
        return String.class;
      }
    };
    try {
      InstantConverter removed = ConverterManager.getInstance().addInstantConverter(c);
      assertEquals(StringConverter.INSTANCE, removed);
      assertEquals(String.class, ConverterManager.getInstance().getInstantConverter("").getSupportedType());
      assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
    } finally {
      ConverterManager.getInstance().addInstantConverter(StringConverter.INSTANCE);
    }
    assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
  }

  public void testAddInstantConverter3() {
    InstantConverter removed = ConverterManager.getInstance().addInstantConverter(StringConverter.INSTANCE);
    assertEquals(null, removed);
    assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
  }

  public void testAddInstantConverter4() {
    InstantConverter removed = ConverterManager.getInstance().addInstantConverter(null);
    assertEquals(null, removed);
    assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
  }


  //-----------------------------------------------------------------------
  public void testRemoveInstantConverter1() {
    try {
      InstantConverter removed = ConverterManager.getInstance().removeInstantConverter(StringConverter.INSTANCE);
      assertEquals(StringConverter.INSTANCE, removed);
      assertEquals(5, ConverterManager.getInstance().getInstantConverters().length);
    } finally {
      ConverterManager.getInstance().addInstantConverter(StringConverter.INSTANCE);
    }
    assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
  }

  public void testRemoveInstantConverter2() {
    InstantConverter c = new InstantConverter() {
      public long getInstantMillis(Object object, Chronology chrono) {
        return 0;
      }

      public Chronology getChronology(Object object, DateTimeZone zone) {
        return null;
      }

      public Chronology getChronology(Object object, Chronology chrono) {
        return null;
      }

      public Class getSupportedType() {
        return Boolean.class;
      }
    };
    InstantConverter removed = ConverterManager.getInstance().removeInstantConverter(c);
    assertEquals(null, removed);
    assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
  }

  public void testRemoveInstantConverter3() {
    InstantConverter removed = ConverterManager.getInstance().removeInstantConverter(null);
    assertEquals(null, removed);
    assertEquals(6, ConverterManager.getInstance().getInstantConverters().length);
  }


  //-----------------------------------------------------------------------
  //-----------------------------------------------------------------------
  private static final int PARTIAL_SIZE = 7;

  public void testGetPartialConverter() {
    PartialConverter c = ConverterManager.getInstance().getPartialConverter(new Long(0L));
    assertEquals(Long.class, c.getSupportedType());

    c = ConverterManager.getInstance().getPartialConverter(new TimeOfDay());
    assertEquals(ReadablePartial.class, c.getSupportedType());

    c = ConverterManager.getInstance().getPartialConverter(new DateTime());
    assertEquals(ReadableInstant.class, c.getSupportedType());

    c = ConverterManager.getInstance().getPartialConverter("");
    assertEquals(String.class, c.getSupportedType());

    c = ConverterManager.getInstance().getPartialConverter(new Date());
    assertEquals(Date.class, c.getSupportedType());

    c = ConverterManager.getInstance().getPartialConverter(new GregorianCalendar());
    assertEquals(Calendar.class, c.getSupportedType());

    c = ConverterManager.getInstance().getPartialConverter(null);
    assertEquals(null, c.getSupportedType());

    try {
      ConverterManager.getInstance().getPartialConverter(Boolean.TRUE);
      fail();
    } catch (IllegalArgumentException ex) {
    }
  }

  public void testGetPartialConverterRemovedNull() {
    try {
      ConverterManager.getInstance().removePartialConverter(NullConverter.INSTANCE);
      try {
        ConverterManager.getInstance().getPartialConverter(null);
        fail();
      } catch (IllegalArgumentException ex) {
      }
    } finally {
      ConverterManager.getInstance().addPartialConverter(NullConverter.INSTANCE);
    }
    assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
  }

  public void testGetPartialConverterOKMultipleMatches() {
    PartialConverter c = new PartialConverter() {
      public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono) {
        return null;
      }

      public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono, DateTimeFormatter parser) {
        return null;
      }

      public Chronology getChronology(Object object, DateTimeZone zone) {
        return null;
      }

      public Chronology getChronology(Object object, Chronology chrono) {
        return null;
      }

      public Class getSupportedType() {
        return ReadableDateTime.class;
      }
    };
    try {
      ConverterManager.getInstance().addPartialConverter(c);
      PartialConverter ok = ConverterManager.getInstance().getPartialConverter(new DateTime());
      // ReadableDateTime and ReadablePartial both match, but RI discarded as less specific
      assertEquals(ReadableDateTime.class, ok.getSupportedType());
    } finally {
      ConverterManager.getInstance().removePartialConverter(c);
    }
    assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
  }

  public void testGetPartialConverterBadMultipleMatches() {
    PartialConverter c = new PartialConverter() {
      public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono) {
        return null;
      }

      public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono, DateTimeFormatter parser) {
        return null;
      }

      public Chronology getChronology(Object object, DateTimeZone zone) {
        return null;
      }

      public Chronology getChronology(Object object, Chronology chrono) {
        return null;
      }

      public Class getSupportedType() {
        return Serializable.class;
      }
    };
    try {
      ConverterManager.getInstance().addPartialConverter(c);
      try {
        ConverterManager.getInstance().getPartialConverter(new DateTime());
        fail();
      } catch (IllegalStateException ex) {
        // Serializable and ReadablePartial both match, so cannot pick
      }
    } finally {
      ConverterManager.getInstance().removePartialConverter(c);
    }
    assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
  }

  //-----------------------------------------------------------------------
  public void testGetPartialConverters() {
    PartialConverter[] array = ConverterManager.getInstance().getPartialConverters();
    assertEquals(PARTIAL_SIZE, array.length);
  }

  //-----------------------------------------------------------------------
  public void testAddPartialConverter1() {
    PartialConverter c = new PartialConverter() {
      public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono) {
        return null;
      }

      public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono, DateTimeFormatter parser) {
        return null;
      }

      public Chronology getChronology(Object object, DateTimeZone zone) {
        return null;
      }

      public Chronology getChronology(Object object, Chronology chrono) {
        return null;
      }

      public Class getSupportedType() {
        return Boolean.class;
      }
    };
    try {
      PartialConverter removed = ConverterManager.getInstance().addPartialConverter(c);
      assertEquals(null, removed);
      assertEquals(Boolean.class, ConverterManager.getInstance().getPartialConverter(Boolean.TRUE).getSupportedType());
      assertEquals(PARTIAL_SIZE + 1, ConverterManager.getInstance().getPartialConverters().length);
    } finally {
      ConverterManager.getInstance().removePartialConverter(c);
    }
    assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
  }

  public void testAddPartialConverter2() {
    PartialConverter c = new PartialConverter() {
      public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono) {
        return null;
      }

      public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono, DateTimeFormatter parser) {
        return null;
      }

      public Chronology getChronology(Object object, DateTimeZone zone) {
        return null;
      }

      public Chronology getChronology(Object object, Chronology chrono) {
        return null;
      }

      public Class getSupportedType() {
        return String.class;
      }
    };
    try {
      PartialConverter removed = ConverterManager.getInstance().addPartialConverter(c);
      assertEquals(StringConverter.INSTANCE, removed);
      assertEquals(String.class, ConverterManager.getInstance().getPartialConverter("").getSupportedType());
      assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
    } finally {
      ConverterManager.getInstance().addPartialConverter(StringConverter.INSTANCE);
    }
    assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
  }

  public void testAddPartialConverter3() {
    PartialConverter removed = ConverterManager.getInstance().addPartialConverter(StringConverter.INSTANCE);
    assertEquals(null, removed);
    assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
  }

  public void testAddPartialConverter4() {
    PartialConverter removed = ConverterManager.getInstance().addPartialConverter(null);
    assertEquals(null, removed);
    assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
  }

  //-----------------------------------------------------------------------
  public void testRemovePartialConverter1() {
    try {
      PartialConverter removed = ConverterManager.getInstance().removePartialConverter(StringConverter.INSTANCE);
      assertEquals(StringConverter.INSTANCE, removed);
      assertEquals(PARTIAL_SIZE - 1, ConverterManager.getInstance().getPartialConverters().length);
    } finally {
      ConverterManager.getInstance().addPartialConverter(StringConverter.INSTANCE);
    }
    assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
  }

  public void testRemovePartialConverter2() {
    PartialConverter c = new PartialConverter() {
      public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono) {
        return null;
      }

      public int[] getPartialValues(ReadablePartial partial, Object object, Chronology chrono, DateTimeFormatter parser) {
        return null;
      }

      public Chronology getChronology(Object object, DateTimeZone zone) {
        return null;
      }

      public Chronology getChronology(Object object, Chronology chrono) {
        return null;
      }

      public Class getSupportedType() {
        return Boolean.class;
      }
    };
    PartialConverter removed = ConverterManager.getInstance().removePartialConverter(c);
    assertEquals(null, removed);
    assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
  }

  public void testRemovePartialConverter3() {
    PartialConverter removed = ConverterManager.getInstance().removePartialConverter(null);
    assertEquals(null, removed);
    assertEquals(PARTIAL_SIZE, ConverterManager.getInstance().getPartialConverters().length);
  }


  //-----------------------------------------------------------------------
  //-----------------------------------------------------------------------
  private static int DURATION_SIZE = 5;

  public void testGetDurationConverter() {
    DurationConverter c = ConverterManager.getInstance().getDurationConverter(new Long(0L));
    assertEquals(Long.class, c.getSupportedType());

    c = ConverterManager.getInstance().getDurationConverter(new Duration(123L));
    assertEquals(ReadableDuration.class, c.getSupportedType());

    c = ConverterManager.getInstance().getDurationConverter(new Interval(0L, 1000L));
    assertEquals(ReadableInterval.class, c.getSupportedType());

    c = ConverterManager.getInstance().getDurationConverter("");
    assertEquals(String.class, c.getSupportedType());

    c = ConverterManager.getInstance().getDurationConverter(null);
    assertEquals(null, c.getSupportedType());

    try {
      ConverterManager.getInstance().getDurationConverter(Boolean.TRUE);
      fail();
    } catch (IllegalArgumentException ex) {
    }
  }

  public void testGetDurationConverterRemovedNull() {
    try {
      ConverterManager.getInstance().removeDurationConverter(NullConverter.INSTANCE);
      try {
        ConverterManager.getInstance().getDurationConverter(null);
        fail();
      } catch (IllegalArgumentException ex) {
      }
    } finally {
      ConverterManager.getInstance().addDurationConverter(NullConverter.INSTANCE);
    }
    assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
  }

  //-----------------------------------------------------------------------
  public void testGetDurationConverters() {
    DurationConverter[] array = ConverterManager.getInstance().getDurationConverters();
    assertEquals(DURATION_SIZE, array.length);
  }

  //-----------------------------------------------------------------------
  public void testAddDurationConverter1() {
    DurationConverter c = new DurationConverter() {
      public long getDurationMillis(Object object) {
        return 0;
      }

      public Class getSupportedType() {
        return Boolean.class;
      }
    };
    try {
      DurationConverter removed = ConverterManager.getInstance().addDurationConverter(c);
      assertEquals(null, removed);
      assertEquals(Boolean.class, ConverterManager.getInstance().getDurationConverter(Boolean.TRUE).getSupportedType());
      assertEquals(DURATION_SIZE + 1, ConverterManager.getInstance().getDurationConverters().length);
    } finally {
      ConverterManager.getInstance().removeDurationConverter(c);
    }
    assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
  }

  public void testAddDurationConverter2() {
    DurationConverter c = new DurationConverter() {
      public long getDurationMillis(Object object) {
        return 0;
      }

      public Class getSupportedType() {
        return String.class;
      }
    };
    try {
      DurationConverter removed = ConverterManager.getInstance().addDurationConverter(c);
      assertEquals(StringConverter.INSTANCE, removed);
      assertEquals(String.class, ConverterManager.getInstance().getDurationConverter("").getSupportedType());
      assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
    } finally {
      ConverterManager.getInstance().addDurationConverter(StringConverter.INSTANCE);
    }
    assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
  }

  public void testAddDurationConverter3() {
    DurationConverter removed = ConverterManager.getInstance().addDurationConverter(null);
    assertEquals(null, removed);
    assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
  }

  //-----------------------------------------------------------------------
  public void testRemoveDurationConverter1() {
    try {
      DurationConverter removed = ConverterManager.getInstance().removeDurationConverter(StringConverter.INSTANCE);
      assertEquals(StringConverter.INSTANCE, removed);
      assertEquals(DURATION_SIZE - 1, ConverterManager.getInstance().getDurationConverters().length);
    } finally {
      ConverterManager.getInstance().addDurationConverter(StringConverter.INSTANCE);
    }
    assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
  }

  public void testRemoveDurationConverter2() {
    DurationConverter c = new DurationConverter() {
      public long getDurationMillis(Object object) {
        return 0;
      }

      public Class getSupportedType() {
        return Boolean.class;
      }
    };
    DurationConverter removed = ConverterManager.getInstance().removeDurationConverter(c);
    assertEquals(null, removed);
    assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
  }

  public void testRemoveDurationConverter3() {
    DurationConverter removed = ConverterManager.getInstance().removeDurationConverter(null);
    assertEquals(null, removed);
    assertEquals(DURATION_SIZE, ConverterManager.getInstance().getDurationConverters().length);
  }

  //-----------------------------------------------------------------------
  //-----------------------------------------------------------------------
  private static int PERIOD_SIZE = 5;

  public void testGetPeriodConverter() {
    PeriodConverter c = ConverterManager.getInstance().getPeriodConverter(new Period(1, 2, 3, 4, 5, 6, 7, 8));
    assertEquals(ReadablePeriod.class, c.getSupportedType());

    c = ConverterManager.getInstance().getPeriodConverter(new Duration(123L));
    assertEquals(ReadableDuration.class, c.getSupportedType());

    c = ConverterManager.getInstance().getPeriodConverter(new Interval(0L, 1000L));
    assertEquals(ReadableInterval.class, c.getSupportedType());

    c = ConverterManager.getInstance().getPeriodConverter("");
    assertEquals(String.class, c.getSupportedType());

    c = ConverterManager.getInstance().getPeriodConverter(null);
    assertEquals(null, c.getSupportedType());

    try {
      ConverterManager.getInstance().getPeriodConverter(Boolean.TRUE);
      fail();
    } catch (IllegalArgumentException ex) {
    }
  }

  public void testGetPeriodConverterRemovedNull() {
    try {
      ConverterManager.getInstance().removePeriodConverter(NullConverter.INSTANCE);
      try {
        ConverterManager.getInstance().getPeriodConverter(null);
        fail();
      } catch (IllegalArgumentException ex) {
      }
    } finally {
      ConverterManager.getInstance().addPeriodConverter(NullConverter.INSTANCE);
    }
    assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
  }

  //-----------------------------------------------------------------------
  public void testGetPeriodConverters() {
    PeriodConverter[] array = ConverterManager.getInstance().getPeriodConverters();
    assertEquals(PERIOD_SIZE, array.length);
  }

  //-----------------------------------------------------------------------
  public void testAddPeriodConverter1() {
    PeriodConverter c = new PeriodConverter() {
      public void setInto(ReadWritablePeriod duration, Object object, Chronology c) {
      }

      public PeriodType getPeriodType(Object object) {
        return null;
      }

      public Class getSupportedType() {
        return Boolean.class;
      }
    };
    try {
      PeriodConverter removed = ConverterManager.getInstance().addPeriodConverter(c);
      assertEquals(null, removed);
      assertEquals(Boolean.class, ConverterManager.getInstance().getPeriodConverter(Boolean.TRUE).getSupportedType());
      assertEquals(PERIOD_SIZE + 1, ConverterManager.getInstance().getPeriodConverters().length);
    } finally {
      ConverterManager.getInstance().removePeriodConverter(c);
    }
    assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
  }

  public void testAddPeriodConverter2() {
    PeriodConverter c = new PeriodConverter() {
      public void setInto(ReadWritablePeriod duration, Object object, Chronology c) {
      }

      public PeriodType getPeriodType(Object object) {
        return null;
      }

      public Class getSupportedType() {
        return String.class;
      }
    };
    try {
      PeriodConverter removed = ConverterManager.getInstance().addPeriodConverter(c);
      assertEquals(StringConverter.INSTANCE, removed);
      assertEquals(String.class, ConverterManager.getInstance().getPeriodConverter("").getSupportedType());
      assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
    } finally {
      ConverterManager.getInstance().addPeriodConverter(StringConverter.INSTANCE);
    }
    assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
  }

  public void testAddPeriodConverter3() {
    PeriodConverter removed = ConverterManager.getInstance().addPeriodConverter(null);
    assertEquals(null, removed);
    assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
  }

  //-----------------------------------------------------------------------
  public void testRemovePeriodConverter1() {
    try {
      PeriodConverter removed = ConverterManager.getInstance().removePeriodConverter(StringConverter.INSTANCE);
      assertEquals(StringConverter.INSTANCE, removed);
      assertEquals(PERIOD_SIZE - 1, ConverterManager.getInstance().getPeriodConverters().length);
    } finally {
      ConverterManager.getInstance().addPeriodConverter(StringConverter.INSTANCE);
    }
    assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
  }

  public void testRemovePeriodConverter2() {
    PeriodConverter c = new PeriodConverter() {
      public void setInto(ReadWritablePeriod duration, Object object, Chronology c) {
      }

      public PeriodType getPeriodType(Object object) {
        return null;
      }

      public Class getSupportedType() {
        return Boolean.class;
      }
    };
    PeriodConverter removed = ConverterManager.getInstance().removePeriodConverter(c);
    assertEquals(null, removed);
    assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
  }

  public void testRemovePeriodConverter3() {
    PeriodConverter removed = ConverterManager.getInstance().removePeriodConverter(null);
    assertEquals(null, removed);
    assertEquals(PERIOD_SIZE, ConverterManager.getInstance().getPeriodConverters().length);
  }


  //-----------------------------------------------------------------------
  //-----------------------------------------------------------------------
  private static int INTERVAL_SIZE = 3;

  public void testGetIntervalConverter() {
    IntervalConverter c = ConverterManager.getInstance().getIntervalConverter(new Interval(0L, 1000L));
    assertEquals(ReadableInterval.class, c.getSupportedType());

    c = ConverterManager.getInstance().getIntervalConverter("");
    assertEquals(String.class, c.getSupportedType());

    c = ConverterManager.getInstance().getIntervalConverter(null);
    assertEquals(null, c.getSupportedType());

    try {
      ConverterManager.getInstance().getIntervalConverter(Boolean.TRUE);
      fail();
    } catch (IllegalArgumentException ex) {
    }
    try {
      ConverterManager.getInstance().getIntervalConverter(new Long(0));
      fail();
    } catch (IllegalArgumentException ex) {
    }
  }

  public void testGetIntervalConverterRemovedNull() {
    try {
      ConverterManager.getInstance().removeIntervalConverter(NullConverter.INSTANCE);
      try {
        ConverterManager.getInstance().getIntervalConverter(null);
        fail();
      } catch (IllegalArgumentException ex) {
      }
    } finally {
      ConverterManager.getInstance().addIntervalConverter(NullConverter.INSTANCE);
    }
    assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
  }

  //-----------------------------------------------------------------------
  public void testGetIntervalConverters() {
    IntervalConverter[] array = ConverterManager.getInstance().getIntervalConverters();
    assertEquals(INTERVAL_SIZE, array.length);
  }

  //-----------------------------------------------------------------------
  public void testAddIntervalConverter1() {
    IntervalConverter c = new IntervalConverter() {
      public boolean isReadableInterval(Object object, Chronology chrono) {
        return false;
      }

      public void setInto(ReadWritableInterval interval, Object object, Chronology chrono) {
      }

      public Class getSupportedType() {
        return Boolean.class;
      }
    };
    try {
      IntervalConverter removed = ConverterManager.getInstance().addIntervalConverter(c);
      assertEquals(null, removed);
      assertEquals(Boolean.class, ConverterManager.getInstance().getIntervalConverter(Boolean.TRUE).getSupportedType());
      assertEquals(INTERVAL_SIZE + 1, ConverterManager.getInstance().getIntervalConverters().length);
    } finally {
      ConverterManager.getInstance().removeIntervalConverter(c);
    }
    assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
  }

  public void testAddIntervalConverter2() {
    IntervalConverter c = new IntervalConverter() {
      public boolean isReadableInterval(Object object, Chronology chrono) {
        return false;
      }

      public void setInto(ReadWritableInterval interval, Object object, Chronology chrono) {
      }

      public Class getSupportedType() {
        return String.class;
      }
    };
    try {
      IntervalConverter removed = ConverterManager.getInstance().addIntervalConverter(c);
      assertEquals(StringConverter.INSTANCE, removed);
      assertEquals(String.class, ConverterManager.getInstance().getIntervalConverter("").getSupportedType());
      assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
    } finally {
      ConverterManager.getInstance().addIntervalConverter(StringConverter.INSTANCE);
    }
    assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
  }

  public void testAddIntervalConverter3() {
    IntervalConverter removed = ConverterManager.getInstance().addIntervalConverter(null);
    assertEquals(null, removed);
    assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
  }

  //-----------------------------------------------------------------------
  public void testRemoveIntervalConverter1() {
    try {
      IntervalConverter removed = ConverterManager.getInstance().removeIntervalConverter(StringConverter.INSTANCE);
      assertEquals(StringConverter.INSTANCE, removed);
      assertEquals(INTERVAL_SIZE - 1, ConverterManager.getInstance().getIntervalConverters().length);
    } finally {
      ConverterManager.getInstance().addIntervalConverter(StringConverter.INSTANCE);
    }
    assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
  }

  public void testRemoveIntervalConverter2() {
    IntervalConverter c = new IntervalConverter() {
      public boolean isReadableInterval(Object object, Chronology chrono) {
        return false;
      }

      public void setInto(ReadWritableInterval interval, Object object, Chronology chrono) {
      }

      public Class getSupportedType() {
        return Boolean.class;
      }
    };
    IntervalConverter removed = ConverterManager.getInstance().removeIntervalConverter(c);
    assertEquals(null, removed);
    assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
  }

  public void testRemoveIntervalConverter3() {
    IntervalConverter removed = ConverterManager.getInstance().removeIntervalConverter(null);
    assertEquals(null, removed);
    assertEquals(INTERVAL_SIZE, ConverterManager.getInstance().getIntervalConverters().length);
  }

  //-----------------------------------------------------------------------
  public void testToString() {
    assertEquals("ConverterManager[6 instant,7 partial,5 duration,5 period,3 interval]", ConverterManager.getInstance().toString());
  }

}
