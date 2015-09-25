/*
 * Copyright (C) 2012 Trilarion 2012
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.tools.i18n;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tools.common.CommonUtils;
import org.tools.io.Resource;
import org.tools.io.ResourceUtils;

/**
 * Helper utilities for i18n package.
 */
public class I18nUtils {

    private static final Logger LOG = Logger.getLogger(I18nUtils.class.getName());
    private static final String EXT_PROPERTY_BUNDLE = ".properties";

    /**
     * No instantiation.
     */
    private I18nUtils() {
    }

    /**
     *
     * @param <T>
     * @param codes
     * @param directory
     * @param base
     * @param type
     * @return
     */
    public static <T extends Property> T loadPropertyBundle(String[] codes, String directory, String base, Class<T> type) {
        if (!isValidLanguageCode(codes)) {
            return null;
        }
        List<Resource> locations = new LinkedList<>();
        StringBuilder sb = new StringBuilder(100);
        for (int i = 0; i < codes.length; i++) {
            sb.append(directory);
            sb.append(ResourceUtils.Delimiter);
            sb.append(base);
            for (int j = 0; j < codes.length - i; j++) {
                sb.append('_');
                sb.append(codes[j]);
            }
            sb.append(EXT_PROPERTY_BUNDLE);
            String path = sb.toString();
            sb.setLength(0);
            Resource location = null;
            try {
                location = ResourceUtils.asResource(path);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            locations.add(location);
        }
        return Property.chainLoad(locations, type);
    }

    /**
     * Checks if a language, country, variant code fulfills certain requirements
     * for a valid Locale code. - Only 3 parts are allowed. - Language and
     * country part must contain exactly two letters - Language must be lower
     * case, country must be upper case.
     *
     * @param code The extracted language code.
     * @return True if the requirements are fulfilled.
     */
    public static boolean isValidLanguageCode(String[] code) {
        if (code.length > 3) {
            return false;
        }
        if (code.length > 0 && (code[0].length() != 2 || !CommonUtils.isLower(code[0]))) {
            return false;
        }
        if (code.length > 1 && (code[1].length() != 2 || !CommonUtils.isUpper(code[1]))) {
            return false;
        }
        /*
         * java.sun.com/docs/books/tutorial/i18n/locale/create.html
         * The variant codes conform to no standard. They are arbitrary and
         * specific to your application. If you create Locale objects with
         * variant codes only your application will know how to deal with them.
         */
        return true;
    }

    /**
     * Locale has three constructors, whether a language, country or variant
     * code are available or not. Often we have parts of file names
     * de_DE_science which specify these codes. This methods automatically
     * extracts the code from part of the file name and calls the right
     * constructor from Locale.
     *
     * @param name The part of the file name specifying the Locale codes
     * (language, country, variant).
     * @return An instance of Locale corresponding to the codes in the input
     * string.
     */
    public static Locale createLocaleFromFileName(String name) {
        String[] code = name.split("_");
        if (!isValidLanguageCode(code)) {
            return null;
        }
        switch (code.length) {
            case 1:
                return new Locale(code[0]);
            case 2:
                return new Locale(code[0], code[1]);
            case 3:
                return new Locale(code[0], code[1], code[2]);
            default:
                return null;
        }
    }
}