/*******************************************************************************
 * Copyright (c) 2010, 2016 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.core.api.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.gui.util.AppProperties;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.util.FileUtil;

public class BundleTools {
    private static final Logger LOGGER = LoggerFactory.getLogger(BundleTools.class);

    public static final Map<String, String> SESSION_TAGS_MANIFEST = new HashMap<>(3);
    public static final Map<String, String> SESSION_TAGS_FILE = new HashMap<>(3);

    static {
        for (Iterator<Entry<Object, Object>> iter = System.getProperties().entrySet().iterator(); iter.hasNext();) {
            Entry<Object, Object> element = iter.next();
            String tag = element.getKey().toString();
            if (tag.startsWith("TGM-")) { //$NON-NLS-1$
                SESSION_TAGS_MANIFEST.put(tag.substring(4), element.getValue().toString());
            } else if (tag.startsWith("TGF-")) { //$NON-NLS-1$
                SESSION_TAGS_FILE.put(tag.substring(4), element.getValue().toString());
            }
        }
    }

    public static final String CONFIRM_CLOSE = "weasis.confirm.closing"; //$NON-NLS-1$
    public static final List<Codec> CODEC_PLUGINS = Collections.synchronizedList(new ArrayList<Codec>());
    private static final File propsFile;

    static {
        StringBuilder bufDir = new StringBuilder(AppProperties.WEASIS_PATH);
        if (!AppProperties.WEASIS_PATH.endsWith(File.separator)) {
            bufDir.append(File.separator);
        }
        bufDir.append("preferences"); //$NON-NLS-1$
        bufDir.append(File.separator);
        bufDir.append(AppProperties.WEASIS_USER);
        bufDir.append(File.separator);
        bufDir.append(AppProperties.WEASIS_PROFILE);

        File dir = new File(bufDir.toString());
        try {
            dir.mkdirs();
        } catch (Exception e) {
            dir = new File(AppProperties.WEASIS_PATH);
            LOGGER.error("Init weasis path dir", e); //$NON-NLS-1$
        }
        propsFile = new File(dir, "weasis.properties"); //$NON-NLS-1$
    }

    public static final WProperties SYSTEM_PREFERENCES = new WProperties();
    public static final WProperties LOCAL_PERSISTENCE = new WProperties();

    static {
        FileUtil.readProperties(propsFile, SYSTEM_PREFERENCES);
        if (!propsFile.canRead()) {
            try {
                propsFile.createNewFile();
            } catch (IOException e) {
                LOGGER.error("", e); //$NON-NLS-1$
            }
        }
    }

    private BundleTools() {
    }

    public static Codec getCodec(String mimeType, String preferredCodec) {
        Codec codec = null;
        synchronized (BundleTools.CODEC_PLUGINS) {
            for (Codec c : BundleTools.CODEC_PLUGINS) {
                if (c.isMimeTypeSupported(mimeType)) {
                    if (c.getCodecName().equals(preferredCodec)) {
                        codec = c;
                        break;
                    }
                    // If the preferred codec cannot be find, the first-found codec is retained
                    if (codec == null) {
                        codec = c;
                    }
                }
            }
            return codec;
        }
    }

    public static void saveSystemPreferences() {
        FileUtil.storeProperties(propsFile, SYSTEM_PREFERENCES, null);
    }

}
