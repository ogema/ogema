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

/**
 * Represents a {@code DeviceDescription} as defined in the HomeMatic
 * XML-RPC specification.
 * @author jlapp
 */
public interface DeviceDescription extends XmlRpcStruct {

    /**
     * Keys used by the DeviceDescription XML-RPC struct.
     */
    public static enum KEYS {

        TYPE(String.class),
        ADDRESS(String.class),
        RF_ADDRESS(String.class),
        /**
         * String[]: Adressen der untergeordneten Kanäle.
         */
        CHILDREN(String[].class),
        /**
         * Bei Geräten vorhanden aber leer
         */
        PARENT(String.class),
        /**
         * Nur bei Kanälen: Typ (Kurzbezeichnung) des übergeordneten Gerätes
         */
        PARENT_TYPE(String.class),
        /**
         * Nur bei Kanälen: Integer: Kanalnummer
         */
        INDEX(int.class),
        /**
         * Boolean: gesicherte Übertragung aktiviert?
         */
        AES_ACTIVE(boolean.class),
        /**
         * String[]: Namen der vorhanden Parameter-Sets..
         */
        PARAMSETS(String[].class),
        /**
         * String: Firmwareversion des Geräts.
         */
        FIRMWARE(String.class),
        /**
         * String: Für Upgrade verfügbare Firmwareversion.
         */
        AVAILABLE_FIRMWARE(String.class),
        /**
         * String: Version der Geräte- oder Kanalbeschreibung.
         */
        VERSION(int.class),
        /**
         * Integer: Flags: 0x01 Visible, 0x02 Internal, 0x08 Dontdelete
         */
        FLAGS(int.class),
        /**
         * String: Durch Leerzeichen getrennte Liste von Rollen, die der Kanal
         * in einer Verknüpfung als Sender annehmen kann. Eine Rolle ist z.B.
         * „SWITCH“ für einen Kanal, der Schaltbefehle senden kann.
         */
        LINK_SOURCE_ROLES(String.class),
        /**
         * (nur bei Kanälen) Datentyp String. Durch Leerzeichen getrennte Liste
         * von Rollen, die der Kanal in einer Verknüpfung als Empfänger annehmen
         * kann. Eine Rolle ist z.B. „SWITCH“ für einen Kanal, der auf
         * empfangene Schaltbefehle reagieren kann.
         */
        LINK_TARGET_ROLES(String.class),
        /**
         * (nur bei Kanälen) Datentyp Integer. Gibt die Richtung (Senden oder
         * Empfangen) dieses Kanals in einer direkten Verknüpfung an. 0 =
         * DIRECTION_NONE (Kanal unterstützt keine direkte Verknüpfung) 1 =
         * DIRECTION_SENDER 2 = DIRECTION_RECEIVER
         */
        DIRECTION(int.class),
        /**
         * (optional, nur bei Kanälen) Datentyp String. Nur bei gruppierten
         * Kanälen (Tastenpaaren) vorhanden. Hier wird die Adresse des anderen
         * zur Gruppe gehörenden Kanals angegeben.
         */
        GROUP(String.class),
        /**
         * (optional, nur bei Kanälen mit Team) Datentyp String. Nur bei Kanälen
         * mit Team (z.B. Rauchmelder) vorhanden. Gibt die Adresse des
         * virtuellen Teamkanals an.
         */
        TEAM(String.class),
        /**
         * (optional, nur bei Kanälen und Teams) Datentyp String. Nur bei
         * Kanälen mit Team (z.B. Rauchmelder) und bei virtuellen Teamkanälen
         * vorhanden. Für die Auswahl eines Teams an der Oberfläche. Ein Kanal,
         * der einem Team zugeordnet werden soll und der virtuelle Teamkanal
         * (das Team) müssen hier denselben Wert haben.
         */
        TEAM_TAG(String.class),
        /**
         * (optional, nur bei Kanälen, die ein Team darstellen) Datentyp
         * String[]. Adressen der dem Team zugeordneten Kanäle.
         */
        TEAM_CHANNELS(String[].class),
        /**
         * (optional, nur bei BidCos-RF) Datentyp String. Seriennummer des dem
         * Gerät zugeordneten Interfaces.
         */
        INTERFACE(String.class),
        /**
         * (optional, nur bei BidCos-RF) Datentyp Boolean. Ist true, wenn die
         * Interfacezuordnung des Geräts automatisch den Empfangsverhältnissen
         * angepasst wird.
         */
        ROAMING(boolean.class),
        /**
         * (nur bei Geräten, nur bei BidCos-RF) Datentyp Integer.
         * Oder-Verknüpfung von Flags die den Empfangsmodes des Gerätes
         * pezifikation repräsentieren. Folgende Werte haben eine Bedeutung:
         * 0x01 = RX_ALWAYS; Das Gerät ist dauerhaft auf Empfang 0x02 =
         * RX_BURST; Das Gerät arbeitet im wake on radio Modus 0x04 = RX_CONFIG;
         * Das Gerät kann nach drücken der Konfigurationstaste erreicht werden
         * 0x08 = RX_WAKEUP; Das Gerät kann nach einer direkter Kommunikation
         * mit der Zentrale geweckt werden 0x10 = RX_LAZY_CONFIG; Das Gerät
         * unterstützt LazyConfig. Das Gerät kann nach einer normalen Bedienung
         * (z.B. Tastendruck einer Fernbedienung) konfiguriert werden.
         */
        RX_MODE(int.class),
        /**
         * ??? boolean ???
         */
        UPDATABLE(boolean.class);

        final Class<?> type;

        private KEYS(Class<?> type) {
            this.type = type;
        }

    }

    /**
     * Bit flags used in the {@link KEYS#RX_MODE } member.
     */
    public static enum RX_FLAGS {

        RX_ALWAYS(0x01),
        RX_BURST(0x02),
        RX_CONFIG(0x04),
        RX_WAKEUP(0x08),
        RX_LAZY_CONFIG(0x10);

        private final int flag;

        private RX_FLAGS(int flag) {
            this.flag = flag;
        }

        public int getFlag() {
            return flag;
        }

        public boolean isSet(int flags) {
            return (flag & flags) != 0;
        }
    }

    /**
     * @return logical device address.
     */
    String getAddress();

    /**
     * List of available {@code Paramsets}. Usually {@code 'MASTER', 'VALUES'}
     * plus the names of LINK sets, which are logical device address strings.
     * Use {@link HomeMatic#getParamsetDescription } and {@link HomeMatic#getParamset }
     * to retrieve description and actual values.
     * 
     * @return available {@code Paramsets}
     */
    String[] getParamsets();

    /**
     * Address of the parent logical device.
     * @return parent address, or null if this describes a top level device.
     */
    String getParent();
    
    /**
     * Parent type, if parent is available.
     * @return parent type, or null if this describes a top level device.
     */
    String getParentType();

    /**
     * @return device type
     */
    String getType();
    
    /**
     * @return true iff this is not a channel.
     */
    boolean isDevice();
    
    int getVersion();
    
    /**
     * @return addresses of sub devices.
     */
    String[] getChildren();

}
