/*
 * Copyright (C) 2011 Trilarion
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tools.io.Resource;

/**
 * New composition of the Properties class which extends the functionality. It
 * uses the very same properties files. Conversions to integer are done
 * automatically. We can set a parent which then is used as fallback or master
 * source (implementing part of functionality from ResourceBundle without all
 * the overhead).
 *
 * Comment: The Properties class itself has shortcomings, one is that it is
 * based on a Hashtable<Object, Object> but anyway stores only strings, the
 * other that the order in the file cannot be made alphabetically because of the
 * interplay of the store function of Properties and the Hashtable. However, for
 * keeping the compatibility, we leave it as it is.
 */
public class Property {

    private static final Logger LOG = Logger.getLogger(Property.class.getName());
    /**
     * We can have them in a chain
     */
    private Property parent;
    /**
     * Underlying Property object
     */
    private Properties prop;
    /**
     * The resource location for loading/saving.
     */
    private Resource location;

    /**
     * Creates new instance.
     */
    public Property() {
        prop = new Properties();
    }

    /**
     * Sets the parent.
     *
     * @param parent New parent.
     */
    public void setParent(Property parent) {
        this.parent = parent;
    }

    /**
     * Sets loading/saving location. The location is a Resource (file, archive).
     *
     * @param location New Location.
     */
    public void setLocation(Resource location) {
        this.location = location;
    }

    // start of methods extending the Properties functionality
    /**
     * Convenience function. Relays to containsKey(String, boolean).
     *
     * @param key The key.
     * @return True if existing in the Property.
     */
    public boolean containsKey(String key) {
        return containsKey(key, true);
    }

    /**
     * Does this key exist either in the Property attached to this class or in
     * any of the Properties attached to this class and all parents?
     *
     * @param key The key.
     * @param recursive If true than also parents are tested recursively.
     * @return True if existing.
     */
    public boolean containsKey(String key, boolean recursive) {
        if (recursive == true) {
            return prop.containsKey(key) || (parent != null && parent.containsKey(key, true));
        } else {
            return prop.containsKey(key);
        }

    }

    /**
     * Returns a content String for a given key. If the key is not existing
     * relays to the parent recursively.
     *
     * @param key The key.
     * @return The content String or null if key does not exist.
     */
    public String get(String key) {
        if (!prop.containsKey(key) && parent != null) {
            return parent.get(key);
        }
        // returns null if property not found
        return prop.getProperty(key);
    }

    /**
     * Stores a new content String under a given key, overwriting the existing
     * content if the key existed. If either key or value is null, nothing is
     * done.
     *
     * @param key The key.
     * @param value The new value.
     */
    public void put(String key, String value) {
        if (key != null && value != null) {
            prop.put(key, value);
        }
    }

    /**
     * Convenience function. Converts the content String to Integer before
     * return.
     *
     * Will throw an exception if the content cannot be converted or the key is
     * not existing.
     *
     * @param key The key.
     * @return The number represented by the content of the key.
     */
    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    /**
     * Convenience function. Allows to specify numbers as contents. They will be
     * converted to String before storing them.
     *
     * @param key The key.
     * @param value The number to store.
     */
    public void putInt(String key, int value) {
        put(key, Integer.toString(value));
    }

    /**
     * Removes a certain key and the associated content.
     *
     * @param key The key.
     * @return True if the key was existing.
     */
    public boolean removeKey(String key) {
        return prop.remove(key) != null;
    }

    /**
     * Remove all keys and their content at once.
     */
    public void removeKeys() {
        prop.clear();
    }

    /**
     * Renames a key, i.e. removing the old key and creating a new key with the
     * same content.
     *
     * If the old key was not existing or the new key is already contained
     * nothing is done.
     *
     * @param oldKey The old key.
     * @param newKey The new key.
     */
    public void renameKey(String oldKey, String newKey) {
        if (prop.containsKey(oldKey) && !prop.containsKey(newKey)) {
            String value = get(oldKey);
            removeKey(oldKey);
            put(newKey, value);
        }
    }

    /**
     * Convenience function. Relays to getKeysAsSet(true).
     *
     * @return A set of keys for this class and all parents.
     */
    public Set<String> getKeysAsSet() {
        return getKeysAsSet(true);
    }

    /**
     * Returns a set of keys (Iterable). If the recursive flag is set, also adds
     * the keys of all parents.
     *
     * @param recursive If true all parents keys are included.
     * @return A set of keys.
     */
    public Set<String> getKeysAsSet(boolean recursive) {
        Set<String> set = new HashSet<String>(prop.size());
        for (Object key : prop.keySet()) {
            set.add((String) key);
        }
        // Should we include also from the parent?
        if (recursive == true && parent != null) {
            for (String s : parent.getKeysAsSet(true)) {
                set.add(s);
            }
        }
        return set;
    }

    // end of methods extending the Properties functionality
    // start of methods for loading/saving
    /**
     * Loads from the specified location. Uses the load method of Properties,
     * therefore fully compatible with ".properties" files.
     *
     * Does not load any parent. Use method chainLoad for this.
     *
     * @return True if location was set, was existing, an InputStream could be
     * obtained and loading
     */
    public boolean load() {
        if (location == null || !location.exists()) {
            return false;
        }
        try {
            prop.load(location.getInputStream());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    /**
     * Saves to the specified location. Uses the store method of Properties,
     * therefore fully compatible with ".properties" files.
     *
     * Does not save any parent. A description can be set but it's useless since
     * it is not read upon load. Just use a key with the description as content
     * if you need one.
     *
     * @return True if the location was set and an OutputStream could be
     * obtained and the storing of the Property was okay.
     */
    public boolean save() {
        if (location == null) {
            return false;
        }
        try {
            /**
             * We would like to save them in alphabetical order in the output
             * file, so the file is editable by humans more easily, but it turns
             * out that Properties store method can't do it, since it uses a
             * Hashtable, i.e. an Enumeration from a Hashtable, to loop over the
             * keys in it's store function. Therefore sorting the keys is not
             * possible without overwriting the store function which could break
             * compatibility with future releases of Property.
             */
            // prop.store(location.getOutputStream(), "saved by org.tools.i18n.Property");
            // to have this feature we will just copy from the source of java.util.Properties
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(location.getOutputStream(), "8859_1"));

            bw.write("#" + new Date().toString());
            bw.newLine();

            // because Properties works with Objects which are not comparable, we first explicitly convert to a List of Strings
            List<String> keys = new ArrayList<String>(prop.size());
            for (Object key : prop.keySet()) {
                keys.add((String) key);
            }
            // then we sort
            Collections.sort(keys);
            // now walk through and save
            for (String key : keys) {
                String val = (String) prop.get(key);
                key = saveConvertFromProperties(key, true);
                val = saveConvertFromProperties(val, false);
                bw.write(key + "=" + val);
                bw.newLine();
            }
            bw.flush();

        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    /**
     * Private helper function needed for alphabetical writing of keys. Copied
     * from the java source: java.util.Properties.
     *
     * @param key
     * @param b
     * @return
     */
    private static String saveConvertFromProperties(String text, boolean escapeSpace) {
        int len = text.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuilder outBuffer = new StringBuilder(bufLen);

        for (int x = 0; x < len; x++) {
            char aChar = text.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch (aChar) {
                case ' ':
                    if (x == 0 || escapeSpace) {
                        outBuffer.append('\\');
                    }
                    outBuffer.append(' ');
                    break;
                case '\t':
                    outBuffer.append('\\');
                    outBuffer.append('t');
                    break;
                case '\n':
                    outBuffer.append('\\');
                    outBuffer.append('n');
                    break;
                case '\r':
                    outBuffer.append('\\');
                    outBuffer.append('r');
                    break;
                case '\f':
                    outBuffer.append('\\');
                    outBuffer.append('f');
                    break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\');
                    outBuffer.append(aChar);
                    break;
                default:
                    if (((aChar < 0x0020) || (aChar > 0x007e))) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >> 8) & 0xF));
                        outBuffer.append(toHex((aChar >> 4) & 0xF));
                        outBuffer.append(toHex(aChar & 0xF));
                    } else {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }
    /**
     * A table of hex digits
     */
    private static final char[] hexDigit = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    /**
     * Convert a nibble to a hex character
     *
     * @param	nibble	the nibble to convert.
     */
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    /**
     * Convenience function. Relays to the generic variant of chainLoad and uses
     * directly two locations - one for a Property object and another one for
     * the parent object. Loads them all and sets the parent.
     *
     * @param locationA Location of content for the created object.
     * @param locationB Location of content for the parent of the created
     * object.
     * @return A newly created Property object which is already loaded and has
     * its parent set.
     */
    public static Property chainLoad(Resource locationA, Resource locationB) {
        return Property.chainLoad(Arrays.asList(locationA, locationB), Property.class);
    }

    /**
     * Generic method to load several Property (or derived) objects and chain
     * them as parents so that only the last object is returned. The returned
     * object will correspond to the first location in the list, the oldest
     * parent to the last location.
     *
     * After creating the objects, the locations are set and the load method is
     * called.
     *
     * @param <T> A type that extends this class.
     * @param locations A list of locations defining the resources for the
     * content of the created object and its parents.
     * @param type We need to directly define what T is by a class, otherwise we
     * cannot obtain a new instance.
     * @return A newly created object of Type T which is already loaded and has
     * all parents set.
     */
    public static <T extends Property> T chainLoad(List<Resource> locations, Class<T> type) {
        if (locations.isEmpty()) {
            return null;
        }
        try {
            T a = null, b = null;
            Resource location;
            for (int i = locations.size() - 1; i >= 0; i--) {
                location = locations.get(i);
                a = type.newInstance();
                a.setLocation(location);
                a.load();
                if (b != null) {
                    a.setParent(b);
                    b = a;
                }
            }
            return a;
        } // end of methods for loading/saving
        catch ( IllegalAccessException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        catch(InstantiationException ex){
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;
    }
    // end of methods for loading/saving
}
