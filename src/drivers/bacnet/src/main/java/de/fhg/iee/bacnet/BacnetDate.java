/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fhg.iee.bacnet;

import java.nio.ByteBuffer;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;

/**
 *
 * @author jan.lapp@iee.fraunhofer.de
 */
public class BacnetDate {
    
    public static final int ANY = 255;
    public static final byte MONTH_ODD = 13;
    public static final byte MONTH_EVEN = 14;
    public static final byte DAY_OF_MONTH_LAST = 32;
    public static final byte DAY_OF_MONTH_ODD = 33;
    public static final byte DAY_OF_MONTH_EVEN = 34;
    
    final int year;
    final byte month;
    final byte dayOfMonth;
    final byte dayOfWeek;
    final boolean hasWildCards;
    
    /**
     * Initialize object from a tag's content.
     * @param contentBuffer
     */
    public BacnetDate(ByteBuffer contentBuffer) {
        int yearInt = contentBuffer.get();
        if (yearInt < 0) {
            year = yearInt + 256;
        } else {
            year = yearInt;
        }
        month = contentBuffer.get();
        dayOfMonth = contentBuffer.get();
        dayOfWeek = contentBuffer.get();
        hasWildCards = checkWildcards();
    }
    
    public BacnetDate(int relYear, int month, int dayOfMonth, int dayOfWeek) {
        this.year = relYear;
        this.month = (byte) month;
        this.dayOfMonth = (byte) dayOfMonth;
        this.dayOfWeek = (byte) dayOfWeek;
        hasWildCards = checkWildcards();
    }
    
    public BacnetDate(TemporalAccessor date) {
        int yearInt = chronoGetOrElse(date, ChronoField.YEAR, ANY);
        if (yearInt != ANY) {
            year = yearInt - 1900;
        } else {
            year = ANY;
        }
        this.month = (byte) chronoGetOrElse(date, ChronoField.MONTH_OF_YEAR, ANY);
        this.dayOfMonth = (byte) chronoGetOrElse(date, ChronoField.DAY_OF_MONTH, ANY);
        this.dayOfWeek = (byte) chronoGetOrElse(date, ChronoField.DAY_OF_WEEK, ANY);
        hasWildCards = checkWildcards();
    }
        
    private int chronoGetOrElse(TemporalAccessor d, TemporalField f, int e) {
        return d.isSupported(f) ? d.get(f) : e;
    }

    public int getYear() {
        return year;
    }

    public byte getMonth() {
        return month;
    }

    public byte getDayOfMonth() {
        return dayOfMonth;
    }

    public byte getDayOfWeek() {
        return dayOfWeek;
    }
    
    public boolean hasWildcards() {
        return hasWildCards;
    }
    
    /**
     * Converts this Bacnet date into a {@code LocalDate} if possible.
     * @return the date represented by this object.
     * @throws DateTimeException if this object does not represent a single date.
     */
    public LocalDate toDate() {
        if (year == ANY
                || (month == (byte) ANY)
                || (dayOfMonth == (byte) ANY)) {
            throw new DateTimeException("Bacnet date contains wildcards.");
        }
        if (month > 12) {
            throw new DateTimeException("Bacnet date does not define month.");
        }
        if (dayOfMonth > 31) {
            throw new DateTimeException("Bacnet date does not define day of month.");
        }
        return LocalDate.of(year + 1900, Month.of(month), dayOfMonth);
    }
    
    final boolean checkWildcards() {
        return year == ANY
                || (month == (byte) ANY)
                || (dayOfMonth == (byte) ANY)
                || (dayOfWeek == (byte) ANY);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(year == ANY ? "any" : year + 1900);
        sb.append("-");
        switch (month) {
            case (byte) ANY : sb.append("any"); break;
            case MONTH_ODD : sb.append("odd"); break;
            case MONTH_EVEN : sb.append("even"); break;
            default : sb.append(month); break;
        }
        sb.append("-");
        switch (dayOfMonth) {
            case (byte) ANY : sb.append("any"); break;
            case DAY_OF_MONTH_LAST : sb.append("last"); break;
            case DAY_OF_MONTH_ODD : sb.append("odd"); break;
            case DAY_OF_MONTH_EVEN : sb.append("even"); break;
            default : sb.append(dayOfMonth); break;
        }
        sb.append("-");
        sb.append(dayOfWeek == (byte) ANY ? "any" : dayOfWeek);
        return sb.toString();
    }
    
    public static BacnetDate parse(String date) {
        date = date.toLowerCase();
        if (date.equals("any") || date.equals("*")) {
            return new BacnetDate(ANY, (byte) ANY, (byte) ANY, (byte) ANY);
        }
        String[] a = date.split("-");
        if (a.length == 0) {
            throw new IllegalArgumentException(date);
        }
        int year;
        if (a[0].equals("any") || a[0].equals("*")) {
            year = ANY;
        } else {
            try {
                year = Integer.parseInt(a[0]);
                if (year > 1900 + 255) {
                    throw new IllegalArgumentException("year out of range in " + date);
                }
                if (year == 1900 + 255) {
                    year = ANY;
                } else {
                    year -= 1900;
                }
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("no year in " + date);
            }
        }
        if (a.length == 1) {
            return new BacnetDate(year, (byte) ANY, (byte) ANY, (byte) ANY);
        }
        
        byte month;
        switch (a[1]) {
            case "any":
            case "*":
                month = (byte) ANY;
                break;
            case "even":
                month = MONTH_EVEN;
                break;
            case "odd":
                month = MONTH_ODD;
                break;
            default:
                try {
                    month = (byte) Integer.parseInt(a[1]);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("no month in " + date);
                }   if (month > 14) {
                    throw new IllegalArgumentException("month out of range in " + date);
                }   break;
        }
        if (a.length == 3) {
            return new BacnetDate(year, month, (byte) ANY, (byte) ANY);
        }
        
        byte dayOfMonth;
        switch (a[2]) {
            case "any":
            case "*":
                dayOfMonth = (byte) ANY;
                break;
            case "even":
                dayOfMonth = DAY_OF_MONTH_EVEN;
                break;
            case "last":
                dayOfMonth = DAY_OF_MONTH_LAST;
                break;
            case "odd":
                dayOfMonth = DAY_OF_MONTH_ODD;
                break;
            default:
                try {
                    dayOfMonth = (byte) Integer.parseInt(a[2]);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("no day of month in " + date);
                }   if (dayOfMonth > 34) {
                    throw new IllegalArgumentException("day of month out of range in " + date);
                }   break;
        }
        if (a.length == 3) {
            return new BacnetDate(year, month, dayOfMonth, (byte) ANY);
        }
        
        byte dayOfWeek;
        if (a[3].equals("any") || a[3].equals("*")) {
            dayOfWeek = (byte) ANY;
        } else {
            try {
                dayOfWeek = (byte) Integer.parseInt(a[3]);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("no day of week in " + date);
            }
            if (dayOfWeek > 7) {
                throw new IllegalArgumentException("day of week out of range in " + date);
            }
        }
        return new BacnetDate(year, month, dayOfMonth, dayOfWeek);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.year;
        hash = 89 * hash + this.month;
        hash = 89 * hash + this.dayOfMonth;
        hash = 89 * hash + this.dayOfWeek;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BacnetDate other = (BacnetDate) obj;
        if (this.year != other.year) {
            return false;
        }
        if (this.month != other.month) {
            return false;
        }
        if (this.dayOfMonth != other.dayOfMonth) {
            return false;
        }
        return this.dayOfWeek == other.dayOfWeek;
    }
    
}
