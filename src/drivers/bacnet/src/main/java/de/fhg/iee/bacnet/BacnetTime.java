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
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.UnsupportedTemporalTypeException;

/**
 *
 * @author jan.lapp@iee.fraunhofer.de
 */
public class BacnetTime {
    
    public static final int ANY = 255;

    private final byte hour;
    private final byte minute;
    private final byte second;
    private final byte hundredth;
    
    /**
     * Initialize object from a tag's content.
     * @param contentBuffer
     */
    public BacnetTime(ByteBuffer contentBuffer) {
        hour = contentBuffer.get();
        minute = contentBuffer.get();
        second = contentBuffer.get();
        hundredth = contentBuffer.get();
    }

    public BacnetTime(int hour, int minute, int second, int hundredth) {
        this.hour = (byte) hour;
        this.minute = (byte) minute;
        this.second = (byte) second;
        this.hundredth = (byte) hundredth;
    }

    /**
     * @param time
     * @throws UnsupportedTemporalTypeException if not all fields are available
     * @throws DateTimeException if field value cannot be obtained
     */
    public BacnetTime(TemporalAccessor time) {
        hour = (byte) time.get(ChronoField.HOUR_OF_DAY);
        minute = (byte) time.get(ChronoField.MINUTE_OF_HOUR);
        second = (byte) time.get(ChronoField.SECOND_OF_MINUTE);
        hundredth = (byte) (time.get(ChronoField.MILLI_OF_SECOND) / 10);
    }

    public byte getHour() {
        return hour;
    }

    public byte getMinute() {
        return minute;
    }

    public byte getSecond() {
        return second;
    }

    public byte getHundredth() {
        return hundredth;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.hour;
        hash = 79 * hash + this.minute;
        hash = 79 * hash + this.second;
        hash = 79 * hash + this.hundredth;
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
        final BacnetTime other = (BacnetTime) obj;
        if (this.hour != other.hour) {
            return false;
        }
        if (this.minute != other.minute) {
            return false;
        }
        if (this.second != other.second) {
            return false;
        }
        return (this.hundredth == other.hundredth);
    }
    
    public LocalTime toTime() {
        return LocalTime.of(hour, minute, second, hundredth * 10_000_000);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(hour == ANY ? "any" : hour);
        sb.append("-");
        switch (minute) {
            case (byte) ANY : sb.append("any"); break;
            default : sb.append(minute); break;
        }
        sb.append("-");
        switch (second) {
            case (byte) ANY : sb.append("any"); break;
            default : sb.append(second); break;
        }
        sb.append("-");
        sb.append(hundredth == (byte) ANY ? "any" : hundredth);
        return sb.toString();
    }
    
    public static BacnetTime parse(String date) {
        date = date.toLowerCase();
        if (date.equals("any") || date.equals("*")) {
            return new BacnetTime(ANY, (byte) ANY, (byte) ANY, (byte) ANY);
        }
        String[] a = date.split("-");
        if (a.length == 0) {
            throw new IllegalArgumentException(date);
        }
        int hour;
        if (a[0].equals("any") || a[0].equals("*")) {
            hour = ANY;
        } else {
            try {
                hour = Integer.parseInt(a[0]);
                if (hour > 23) {
                    throw new IllegalArgumentException("hour out of range in " + date);
                }
             } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("no hour in " + date);
            }
        }
        if (a.length == 1) {
            return new BacnetTime(hour, (byte) ANY, (byte) ANY, (byte) ANY);
        }
        
        byte minute;
        switch (a[1]) {
            case "any":
            case "*":
                minute = (byte) ANY;
                break;
             default:
                try {
                    minute = (byte) Integer.parseInt(a[1]);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("no minutes in " + date);
                }   if (minute > 59) {
                    throw new IllegalArgumentException("minutesth out of range in " + date);
                }   break;
        }
        if (a.length == 3) {
            return new BacnetTime(hour, minute, (byte) ANY, (byte) ANY);
        }
        
        byte second;
        switch (a[2]) {
            case "any":
            case "*":
                second = (byte) ANY;
                break;
            default:
                try {
                    second = (byte) Integer.parseInt(a[2]);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("no seconds in " + date);
                }   if (second > 59) {
                    throw new IllegalArgumentException("seconds out of range in " + date);
                }   break;
        }
        if (a.length == 3) {
            return new BacnetTime(hour, minute, second, (byte) ANY);
        }
        
        byte hundredth;
        if (a[3].equals("any") || a[3].equals("*")) {
            hundredth = (byte) ANY;
        } else {
            try {
                hundredth = (byte) Integer.parseInt(a[3]);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("no hundredth in " + date);
            }
            if (hundredth > 99) {
                throw new IllegalArgumentException("hundredth out of range in " + date);
            }
        }
        return new BacnetTime(hour, minute, second, hundredth);
    }

}
