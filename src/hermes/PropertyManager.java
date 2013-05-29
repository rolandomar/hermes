/**
 * $RCSfile: PropertyManager.java,v $ $Revision: 1.9.2.6 $ $Date: 2001/04/26
 * 06:55:09 $
 *
 * Copyright (C) 2000 CoolServlets.com. All rights reserved.
 *
 * =================================================================== The
 * Apache Software License, Version 1.1
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 * include the following acknowledgment: "This product includes software
 * developed by CoolServlets.com (http://www.coolservlets.com)." Alternately,
 * this acknowledgment may appear in the software itself, if and wherever such
 * third-party acknowledgments normally appear.
 *
 * 4. The names "Jive" and "CoolServlets.com" must not be used to endorse or
 * promote products derived from this software without prior written permission.
 * For written permission, please contact webmaster@coolservlets.com.
 *
 * 5. Products derived from this software may not be called "Jive", nor may
 * "Jive" appear in their name, without prior written permission of
 * CoolServlets.com.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * COOLSERVLETS.COM OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals on
 * behalf of CoolServlets.com. For more information on CoolServlets.com, please
 * see <http://www.coolservlets.com>.
 *
 * Modified by Soila Pertet for Hadoop fault injector Modified Date: April 10,
 * 2008
 */
package hermes;

import java.util.*;
import java.io.*;

/**
 * Manages properties for the entire system. Properties are merely pieces of
 * information that need to be saved in between server restarts. <p> This class
 * is implemented as a singleton since many classloaders seem to take issue with
 * doing classpath resource loading from a static context.
 */
public class PropertyManager {

    private String propsName = "/hadoop-bug.properties";

    /**
     * Set path to property file.
     *
     * @param propsName path to property file
     */
    public void setPropsName(String props) {
        propsName = props;
    }

    /**
     * Returns path to property file.
     *
     * @return path to property file
     */
    public String getPropsName() {
        return (propsName);
    }
    private Properties properties = null;
    final private Object propertiesLock = new Object();
    private String resourceURI;

    /**
     * Creates a new PropertyManager. Singleton access only.
     */
    public PropertyManager(String resourceURI) {
        this.resourceURI = resourceURI;
    }

    /**
     * Gets a property. Properties are stored in properties file. The properties
     * file should be accesible from the classpath. Additionally, it should have
     * a path field that gives the full path to where the file is located.
     * Getting properties is a fast operation.
     *
     * @param name the name of the property to get.
     * @return the property specified by name.
     */
    protected String getProperty(String name) {
        // If properties aren't loaded yet. We also need to make this thread
        // safe, so synchronize...
        if (properties == null) {
            synchronized (propertiesLock) {
                // Need an additional check
                if (properties == null) {
                    loadProperties();
                }
            }
        }
        String property = properties.getProperty(name);
        if (property == null) {
            return null;
        } else {
            return property.trim();
        }
    }

    /**
     * Sets a property. Because the properties must be saved to disk every time
     * a property is set, property setting is relatively slow.
     */
    protected void setProperty(String name, String value) {
        // Only one thread should be writing to the file system at once.
        synchronized (propertiesLock) {
            // Create the properties object if necessary.
            if (properties == null) {
                loadProperties();
            }
            properties.setProperty(name, value);
            saveProperties();
        }
    }

    /**
     * Gets a property of type long. Properties are stored in properties file.
     *
     * @param name
     */
    protected long getPropertyLong(String name) {
        String property = getProperty(name);
        long propertyLong = 0;
        if (property != null) {
            try {
                propertyLong = Long.parseLong(property);
            } catch (Exception e) {
                System.err
                        .println("WARN: Unable to convert property from string to long: "
                        + name + "=" + property);
                e.printStackTrace();
            }
        }
        return (propertyLong);
    }

    /**
     * Sets a property of type long. Properties are stored in properties file.
     *
     * @param name
     */
    protected void setPropertyLong(String name, long value) {
        setProperty(name, Long.toString(value));
    }

    /**
     * Gets a property of type float. Properties are stored in properties file.
     *
     * @param name
     */
    protected float getPropertyFloat(String name) {
        String property = getProperty(name);
        float propertyFloat = 0;
        if (property != null) {
            try {
                propertyFloat = Float.parseFloat(property);
            } catch (Exception e) {
                System.err
                        .println("WARN: Unable to convert property from string to float: "
                        + name + "=" + property);
                e.printStackTrace();
            }
        }
        return (propertyFloat);
    }

    /**
     * Sets a property of type float. Properties are stored in properties file.
     *
     * @param name
     */
    protected void setPropertyFloat(String name, float value) {
        setProperty(name, Float.toString(value));
    }

    protected void deleteProperty(String name) {
        // Only one thread should be writing to the file system at once.
        synchronized (propertiesLock) {
            // Create the properties object if necessary.
            if (properties == null) {
                loadProperties();
            }
            properties.remove(name);
            saveProperties();
        }
    }

    protected Enumeration propertyNames() {
        // If properties aren't loaded yet. We also need to make this thread
        // safe, so synchronize...
        if (properties == null) {
            synchronized (propertiesLock) {
                // Need an additional check
                if (properties == null) {
                    loadProperties();
                }
            }
        }
        return properties.propertyNames();
    }

    public String toString() {

        StringBuffer buf = new StringBuffer();

        for (Enumeration propNames = propertyNames(); propNames
                .hasMoreElements();) {
            String key = (String) propNames.nextElement();
            String value = getProperty(key);
            buf.append(key);
            buf.append(": ");
            buf.append(value);
            buf.append(", ");
        }

        return (buf.toString());

    }

    /**
     * Loads properties from the disk.
     */
    private void loadProperties() {
        properties = new Properties();
        //InputStream in = null;
        FileInputStream in = null;
        try {
            //in = getClass().getResourceAsStream(resourceURI);
            in = new FileInputStream(resourceURI);
            properties.load(in);
            
        } catch (Exception e) {
            System.err
                    .println("Error reading properties in PropertyManager.loadProps() "
                    + e);
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Saves properties to disk.
     */
    private void saveProperties() {
        // Now, save the properties to disk. In order for this to work, the user
        // needs to have set the path field in the properties file. Trim
        // the String to make sure there are no extra spaces.
        String path = properties.getProperty("path").trim();
        OutputStream out = null;
        try {
            out = new FileOutputStream(path);
            properties.store(out, propsName + " -- " + (new java.util.Date()));
        } catch (Exception ioe) {
            System.err
                    .println("There was an error writing "
                    + propsName
                    + " to "
                    + path
                    + ". "
                    + "Ensure that the path exists and that the process has permission "
                    + "to write to it -- " + ioe);
            ioe.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Returns true if the properties are readable. This method is mainly
     * valuable at setup time to ensure that the properties file is setup
     * correctly.
     */
    public boolean propertyFileIsReadable() {
        try {
            InputStream in = getClass().getResourceAsStream(resourceURI);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if the properties file exists where the path property
     * purports that it does.
     */
    public boolean propertyFileExists() {
        String path = getProperty("path");
        if (path == null) {
            return false;
        }
        File file = new File(path);
        if (file.isFile()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if the properties are writable. This method is mainly
     * valuable at setup time to ensure that the properties file is setup
     * correctly.
     */
    public boolean propFileIsWritable() {
        String path = getProperty("path");
        File file = new File(path);
        if (file.isFile()) {
            // See if we can write to the file
            if (file.canWrite()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
