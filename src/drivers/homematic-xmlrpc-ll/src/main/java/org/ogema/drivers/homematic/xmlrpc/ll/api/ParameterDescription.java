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
package org.ogema.drivers.homematic.xmlrpc.ll.api;

import java.util.Comparator;
import java.util.Map;

/**
 * Represents a {@code ParameterDescription} as defined in the HomeMatic
 * XML-RPC specification. {@code Paramsets} are respresented as
 * {@code Map<String, ParameterDescription>}.
 * 
 * @param <T> data type of the described parameter.
 * 
 * @author jlapp
 */
public interface ParameterDescription<T extends Object> extends XmlRpcStruct {
    
    public static final Comparator<ParameterDescription<?>> TAB_ORDER_COMPARATOR = new Comparator<ParameterDescription<?>>() {
        @Override
        public int compare(ParameterDescription<?> o1, ParameterDescription<?> o2) {
            return Integer.compare(o1.getTabOrder(), o2.getTabOrder());
        }
    };
    
    /**
     * Paramset types for HomeMatic XML-RPC. Note that Paramsets of type {@link LINK}
     * will actually use the device address of the link target as name, see
     * {@link DeviceDescription#getParamsets() }.
     */
    public enum SET_TYPES {
        MASTER,
        LINK,
        VALUES
    }
    
    /**
     * Parameter types used by HomeMatic XML-RPC.
     */
    public enum TYPES {
        FLOAT,
        INTEGER,
        BOOL,
        ENUM,
        STRING,
        ACTION
    }
    
    /**
     * Keys used by the XML-RPC structs in a ParameterDescription's
     * {@link KEYS#SPECIAL } member.
     */
    public enum SPECIAL_KEYS {
        ID,
        VALUE
    }

    /**
     * Keys used by the ParameterDescription XML-RPC struct.
     */
    public enum KEYS {

        /** FLOAT, INTEGER, BOOL, ENUM */
        TYPE(String.class),
        
        /** flags: 1=Read, 2=Write, 4=Event */
        OPERATIONS(int.class),
        
        /**
         * 0x01: Visible <br>
         * 0x02: Internal  <br>
         * 0x04: Transform  <br>
         * 0x08: Service <br>
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
    
    int getOperations();

    boolean isReadable();
    
    boolean isWritable();
    
    boolean isEvent();
    
    String getUnit();
    
}
