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
package org.ogema.frameworkadministration.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.osgi.framework.Bundle;

/**
 *
 * @author jlapp
 */
public class BundleIcon {

    private static final Map<Bundle, BundleIcon> icons = new HashMap<>();
    private static final BundleIcon NO_ICON = new BundleIcon(null, IconType.PNG);

    public static enum IconType {

        PNG("image/png"), JPG("image/jpg"), SVG("image/svg+xml");
        private final String contentType;

        IconType(String contentType) {
            this.contentType = contentType;
        }

    };

    private final URL url;
    private final IconType type;

    /* images that fit into buffer are stored on first read */
    static final int BUFFER_SIZE = 8192;
    private int size = -1;
    private byte[] storedBuffer;

    public BundleIcon(URL url, IconType type) {
        this.url = url;
        this.type = type;
    }

    public static BundleIcon forBundle(Bundle b, BundleIcon defaultIcon) {
        BundleIcon icon = icons.get(b);
        if (icon != null) {
            return icon == NO_ICON ? defaultIcon : icon;
        }

        URL url = b.getResource("/icon.svg");
        if (url != null) {
            icon = new BundleIcon(url, IconType.SVG);
        } else {
            url = b.getResource("/icon.png");
            if (url != null) {
                icon = new BundleIcon(url, IconType.PNG);
            } else {
                url = b.getResource("/icon.jpg");
                if (url != null) {
                    icon = new BundleIcon(url, IconType.JPG);
                } else {
                    icon = NO_ICON;
                }
            }
        }
        icons.put(b, icon);
        return icon == NO_ICON ? defaultIcon : icon;
    }

    public void writeIcon(HttpServletResponse resp) throws IOException {
        resp.setContentType(type.contentType);

        if (size != -1 && size < BUFFER_SIZE) {
            synchronized (this) {
                if (storedBuffer == null) {
                    storedBuffer = new byte[BUFFER_SIZE];
                    try (InputStream is = url.openStream()) {
                        int len, offset = 0;
                        while ((len = is.read(storedBuffer, offset, BUFFER_SIZE-offset)) != -1) {
                            offset += len;
                        }
                    }
                }
            }
            try (OutputStream out = resp.getOutputStream()) {
                out.write(storedBuffer, 0, size);
            }
            return;
        }

        byte[] buf = new byte[BUFFER_SIZE];
        int totalSize = 0;
        try (InputStream is = url.openStream(); OutputStream out = resp.getOutputStream()) {
            int len;
            while ((len = is.read(buf)) != -1) {
                out.write(buf, 0, len);
                totalSize += len;
            }
        }

        size = totalSize;
    }
}
