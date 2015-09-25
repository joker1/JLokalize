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
package org.tools.ui;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Sets Look and Feel.
 */
public class LookAndFeel {

    private static final Logger LOG = Logger.getLogger(LookAndFeel.class.getName());

    /**
     * Private constructor to avoid instantiation.
     */
    private LookAndFeel() {
    }

    /**
     * We want the GUI to mimic the natural system application look and feel.
     */
    public static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch ( UnsupportedLookAndFeelException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }catch (ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, null, ex);
		}catch (InstantiationException ex) {
            LOG.log(Level.SEVERE, null, ex);
		}catch (IllegalAccessException ex) {
            LOG.log(Level.SEVERE, null, ex);
		}
    }

    /**
     * Cross-platform look and feel. Metal.
     */
    public static void setMetalLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException e) {
            LOG.log(Level.SEVERE, null, e);
			e.printStackTrace();
		} catch (InstantiationException e) {
            LOG.log(Level.SEVERE, null, e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
            LOG.log(Level.SEVERE, null, e);
			e.printStackTrace();
		}
    }

    /**
     * Cross-platform look and feel. Nimbus.
     */
    public static void setNimbusLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (InstantiationException e) {
            LOG.log(Level.SEVERE, null, e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
            LOG.log(Level.SEVERE, null, e);
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
            LOG.log(Level.SEVERE, null, e);
			e.printStackTrace();
		}
    }
}
