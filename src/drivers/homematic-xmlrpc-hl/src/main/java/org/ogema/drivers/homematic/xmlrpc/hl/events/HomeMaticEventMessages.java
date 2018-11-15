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
package org.ogema.drivers.homematic.xmlrpc.hl.events;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import org.ogema.core.application.ApplicationManager;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;

/**
 *
 * @author jlapp
 */
public class HomeMaticEventMessages {

    public static final String TOPIC = "ogema/drivers/homematic";

    public static final String WRITE_TARGET = "write.address";

    final static String BUNDLE_BASE_NAME = HomeMaticEventMessages.class.getPackage().getName() + ".Messages";

    static final LocalizableEventMessage TITLE = new LocalizableEventMessage() {
        @Override
        public String getMessage(Locale l, Event e) {
            return MessageFormat.format(loadBundle(l).getString("WriteFailedTitle"), (Object) null);
        }

    };

    static final LocalizableEventMessage MESSAGE = new LocalizableEventMessage() {
        @Override
        public String getMessage(Locale t, Event u) {
            String s = String.valueOf(u.getProperty(WRITE_TARGET));
            String msg = loadBundle(t).getString("WriteFailed");
            return MessageFormat.format(msg, s);
        }
    };

    static ResourceBundle loadBundle(Locale l) {
        return ResourceBundle.getBundle(BUNDLE_BASE_NAME, l);
    }

    public static Event createWriteFailedEvent(ApplicationManager appman, String target) {
        Map<String, ?> eventProps = OgemaEventConstants.createEventProperties(appman, "write failed: " + target, TITLE, MESSAGE);
        return new Event(TOPIC, eventProps);
    }

}
