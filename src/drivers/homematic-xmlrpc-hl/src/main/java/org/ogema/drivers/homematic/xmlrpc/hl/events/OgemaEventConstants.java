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

import java.util.HashMap;
import java.util.Map;
import org.ogema.core.application.ApplicationManager;
import org.osgi.service.event.EventConstants;

/**
 *
 * @author jlapp
 */
public abstract class OgemaEventConstants {
    
    private OgemaEventConstants() {}
    
    /**
     * The OGEMA execution time when the event occurred.
     */
    public static final String OGEMA_TIMESTAMP = "timestamp_ogema";
    
    /**
     * The function used to create a message for a selected locale, type is
     * {@link LocalizableEventMessage}
     */
    public static final String TITLE_L10N = "title_l10n";
    
    /**
     * The function used to create a message for a selected locale, type is
     * {@link LocalizableEventMessage}
     */
    public static final String MESSAGE_L10N = "message_l10n";

    /**
     * Initializes a new event properties map with the OGEMA properties plus
     * the standard bundle, message and timestamp properties.
     * 
     * @param appman Application manager of the OGEMA application.
     * @param message (optional) event message.
     * @param title (optional) localizable event title for use in front ends.
     * @param messageL10N (optional) localizable event message for use in front ends.
     * @return new Event
     */
    public static Map<String, ?> createEventProperties(ApplicationManager appman, String message, LocalizableEventMessage title, LocalizableEventMessage messageL10N) {
        Map<String, Object> eventProps = new HashMap<>();
        eventProps.put(EventConstants.BUNDLE, appman.getAppID().getBundle());
        eventProps.put(EventConstants.TIMESTAMP, System.currentTimeMillis());
        if (message != null) {
            eventProps.put(EventConstants.MESSAGE, System.currentTimeMillis());
        }
        eventProps.put(OGEMA_TIMESTAMP, appman.getFrameworkTime());
        if (title != null) {
            eventProps.put(TITLE_L10N, title);
        }
        if (messageL10N != null) {
            eventProps.put(MESSAGE_L10N, messageL10N);
        }
        return eventProps;
    }
    
}

