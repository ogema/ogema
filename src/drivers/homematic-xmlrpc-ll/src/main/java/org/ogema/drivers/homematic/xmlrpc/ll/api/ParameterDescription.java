/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.drivers.homematic.xmlrpc.ll.api;

import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author jlapp
 */
public interface ParameterDescription<T extends Object> extends XmlRpcStruct {
    
    public static final Comparator<ParameterDescription<?>> TAB_ORDER_COMPARATOR =
            (ParameterDescription<?> o1, ParameterDescription<?> o2) -> Integer.compare(o1.getTabOrder(), o2.getTabOrder());
    
    public static enum SET_TYPES {
        MASTER,
        LINK,
        VALUES
    }
    
    public static enum TYPES {
        FLOAT,
        INTEGER,
        BOOL,
        ENUM,
        STRING,
        ACTION
    }
    
    public static enum SPECIAL {
        ID,
        VALUE
    }

    public static enum KEYS {

        /** FLOAT, INTEGER, BOOL, ENUM */
        TYPE(String.class),
        
        /** flags: 1=Read, 2=Write, 4=Event */
        OPERATIONS(int.class),
        
        /**
         * 0x01: Visible <br />
         * 0x02: Internal  <br />
         * 0x04: Transform  <br />
         * 0x08: Service <br />
         * 0x10: Sticky
         */
        FLAGS(int.class),
        
        DEFAULT(Object.class),
        
        MAX(Object.class),
        
        MIN(Object.class),
        
        UNIT(String.class),
        
        TAB_ORDER(int.class),
        
        CONTROL(String.class),
        
        /** 
         * optional for type FLOAT or INTEGER:
         * 
         * array of special values as structs with members ID and VALUE
         */
        SPECIAL(Map[].class),
        
        /** Values for ENUM */
        VALUE_LIST(String[].class)
        ;

        final Class<?> type;

        private KEYS(Class<?> type) {
            this.type = type;
        }
        
    }
    
    TYPES getType();
    
    int getTabOrder();
    
    T getDefault();
    
    Map<String, T> getSpecial();
    
    default int getOperations() {
        return getInt(KEYS.OPERATIONS.name());
    }

    default boolean isReadable() {
        return (getOperations() & 0x01) != 0;
    }
    
    default boolean isWritable() {
        return (getOperations() & 0x02) != 0;
    }
    
    default boolean isEvent() {
        return (getOperations() & 0x04) != 0;
    }
    
    default String getUnit() {
        return getString(KEYS.UNIT.name());
    }
    
}
