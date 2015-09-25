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

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tools.io.Resource;
import org.tools.io.ResourceUtils;

/**
 * Extends the Property class by the ability to collect usage statistics and to
 * store and load it.
 *
 * A statistics is simply a list itself that stores for each key of the super
 * class how often it has been accessed. Even keys that are stored in parents
 * are counted.
 *
 * The default extension of statistics files is ".statistics". The location will
 * be derived from the location of the super class - just the extension changes.
 *
 * Mostly we overwrite some methods of Property. The specific things are: - We
 * allow to no specify that statistics are not loaded, for example if you want
 * to load a Property file - We allow to save the statistics only, for example
 * if the content is read in read only mode.
 */
public class PropertyWithStats extends Property {

    private static final Logger LOG = Logger.getLogger(PropertyWithStats.class.getName());
    /**
     * Standard file extension.
     */
    private static final String EXT = ".statistics";
    /**
     * A Properties object holding the statistics.
     */
    private Properties prop;
    /**
     * The resource location for loading/saving of the statistics.
     */
    private Resource location;

    /**
     * Creates new instance.
     */
    public PropertyWithStats() {
        prop = new Properties();
    }

    /**
     * {@inheritDoc}
     *
     * The location of the statistics file is automatically set also by the rule
     * that the extension of location is removed and the standard extension is
     * added.
     *
     * If such a location is not existing, it will not be loaded from later on.
     *
     * @param location {@inheritDoc}
     */
    @Override
    public void setLocation(Resource location) {
        super.setLocation(location);


        String path = location.getPath();
        path = path.substring(0, path.lastIndexOf('.')) + EXT;
        try {
            this.location = ResourceUtils.asResource(path);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Additionally the statistics is increased if the key was existing.
     *
     * @param key {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String get(String key) {
        String result = super.get(key);
        if (result != null && containsKey(key, false)) {
            if (prop.containsKey(key)) {
                // Entry with key already existing, increment it
                Integer counts = Integer.parseInt((String) prop.get(key));
                counts += 1;
                prop.put(key, counts.toString());
            } else {
                // First time this entry is recalled, create new entry in the statistics
                prop.put(key, "1");
            }
        }
        return result;
    }

    /**
     * Clears the statistics.
     */
    public void clear() {
        prop.clear();
    }

    /**
     * Delete all entries for keys in the statistics that do not have an entry
     * in the Property anymore.
     */
    public void prune() {
        // automatically delete all that are not present
        for (Object key : prop.keySet()) {
            if (!containsKey((String) key, false)) {
                prop.remove(key);
            }
        }
    }

    // start of methods for loading/saving
    /**
     * Convenience function. Relays to load(true).
     *
     * @return True if successful.
     */
    @Override
    public boolean load() {
        return load(true);
    }

    /**
     * {@inheritDoc}
     *
     * Additionally loads the statistics if the loadStats flag is set and the
     * resource at the location for the statistics is existing.
     *
     * @param loadStats If true, the statistics is loaded.
     * @return {@inheritDoc}
     */
    public boolean load(boolean loadStats) {
        boolean b = super.load();
        if (b == true && loadStats == true) {
            if (location != null && location.exists()) {
                try {
                    prop.load(location.getInputStream());
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                    return false;
                }
            }
        }
        return b;
    }

    /**
     * {@inheritDoc}
     *
     * Also saves the statistics.
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean save() {
        boolean b = super.save();
        if (b == true) {
            b = saveStatsOnly();
        }
        return b;
    }

    /**
     * Only saves the statistics.
     *
     * @return True if the location was set, an OutputStream could be obtained
     * and the storing of the statistics was successful.
     */
    public boolean saveStatsOnly() {
        if (location == null) {
            return false;
        }
        try {
            prop.store(location.getOutputStream(), "statistics");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return true;
    }

    /**
     * Convenience function. Relays to the generic variant of chainLoad and uses
     * directly two locations - one for a PropertyWithStats object and another
     * one for the parent object. Loads them all and sets the parent.
     *
     * @param locationA Location of content for the created object.
     * @param locationB Location of content for the parent of the created
     * object.
     * @return A newly created PropertyWithStatistics object which is already
     * loaded and has its parent set.
     */
    public static PropertyWithStats chainLoad(Resource locationA, Resource locationB) {
        return Property.chainLoad(Arrays.asList(locationA, locationB), PropertyWithStats.class);
    }
    // end of methods for loading/saving
}
