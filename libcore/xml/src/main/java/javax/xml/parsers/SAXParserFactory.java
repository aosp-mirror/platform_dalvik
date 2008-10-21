/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.xml.parsers;

import org.apache.harmony.xml.parsers.SAXParserFactoryImpl;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXException;

/**
 * Provides a factory for SAXParser instances. The class first needs to be
 * instantiated using the newInstance() method. The instance can be
 * configured as desired. A call to newSAXParser() then provides a SAXParser
 * instance matching this configuration. 
 */
public abstract class SAXParserFactory {

    private boolean namespaceAware;

    private boolean validating;

    private boolean xincludeAware;

    /**
     * Do-nothing constructor. Prevents instantiation. To be overridden by
     * concrete subclasses.
     */
    protected SAXParserFactory() {
        // Does nothing.
    }

    /**
     * Queries a feature from the underlying implementation.
     * 
     * @param name The name of the feature. The default Android implementation
     *             of SAXParser supports only the following three features:
     *             
     *             <dl>
     *               <dt>{@code http://xml.org/sax/features/namespaces}</dt>
     *               <dd>Queries the state of namespace-awareness.</dd>
     *               
     *               <dt>
     *                 {@code http://xml.org/sax/features/namespace-prefixes}
     *               </dt>
     *               <dd>Queries the state of namespace prefix processing</dd>
     *
     *               <dt>{@code http://xml.org/sax/features/validation}</dt>
     *               <dd>Queries the state of validation.</dd>
     *             </dl>
     *             
     *             Note that despite the ability to query the validation
     *             feature, there is currently no validating parser available.
     *             Also note that currently either namespaces or 
     *             namespace prefixes can be enabled, but not both at the same 
     *             time.
     *             
     * @return The status of the feature.
     * 
     * @throws ParserConfigurationException if no SAXParser matching the given
     *         criteria is available.
     * @throws SAXNotRecognizedException If the given feature is not known to
     *         the underlying implementation.
     * @throws SAXNotSupportedException If the given features is known, but not
     *         supported by the underlying implementation.
     */
    public abstract boolean getFeature(String name)
            throws ParserConfigurationException, SAXNotRecognizedException,
            SAXNotSupportedException;

    /**
     * Queries the desired XML Schema object.
     * 
     * @return The XML Schema object, if it has been set by a call to setSchema,
     *         or null otherwise.
     */
    // TODO Do we want the validation package in?
    // public javax.xml.validation.Schema getSchema() {
    // return schema;
    // }
    
    /**
     * Queries whether the factory is configured to deliver parsers that are
     * namespace-aware.
     * 
     * @return true if namespace-awareness is desired, false otherwise.
     */
    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    /**
     * Queries whether the factory is configured to deliver parsers that are
     * validating.
     * 
     * @return true if validating is desired, false otherwise.
     */
    public boolean isValidating() {
        return validating;
    }

    /**
     * Queries whether the factory is configured to deliver parsers that are
     * XInclude-aware.
     * 
     * @return true if XInclude-awareness is desired, false otherwise.
     */
    public boolean isXIncludeAware() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new SAXParserFactory that can be configured and then be used
     * for creating SAXPArser objects.
     * 
     * @return The SAXParserFactory.
     * 
     * @throws FactoryConfigurationError If no SAXParserFactory can be created.
     */
    public static SAXParserFactory newInstance()
            throws FactoryConfigurationError {
        // TODO Properties file and META-INF case missing here. See spec.
        String factory = System
                .getProperty("javax.xml.parsers.SAXParserFactory");
        if (factory != null) {
            try {
                return (SAXParserFactory) Class.forName(factory).newInstance();
            } catch (Exception ex) {
                throw new FactoryConfigurationError(factory);
            }
        }

        try {
            return new SAXParserFactoryImpl();
        } catch (Exception ex) {
            // Ignore.
        }

        throw new FactoryConfigurationError("Cannot create SAXParserFactory");
    }

    /**
     * Creates a new SAXParser that matches the current configuration.
     * 
     * @return The SAXParser.
     * 
     * @throws ParserConfigurationException if no matching SAXParser could be
     *         found.
     * @throws SAXException If a problem occurs during SAX parsing.
     */
    public abstract SAXParser newSAXParser()
            throws ParserConfigurationException, SAXException;

    /**
     * Sets a feature in the underlying implementation.
     * 
     * @param name The name of the feature. The default Android implementation
     *             of SAXParser supports only the following three features:
     *             
     *             <dl>
     *               <dt>{@code http://xml.org/sax/features/namespaces}</dt>
     *               <dd>Queries the state of namespace-awareness.</dd>
     *
     *               <dt>
     *                 {@code http://xml.org/sax/features/namespace-prefixes}
     *               </dt>
     *               <dd>Queries the state of namespace prefix processing</dd>
     *
     *               <dt>{@code http://xml.org/sax/features/validation}</dt>
     *               <dd>Queries the state of validation.</dd>
     *             </dl>
     *             
     *             Note that despite the ability to query the validation
     *             feature, there is currently no validating parser available.
     *             Also note that currently either namespaces or 
     *             namespace prefixes can be enabled, but not both at the same 
     *             time.
     *             
     * @param value The status of the feature.
     * 
     * @throws ParserConfigurationException if no SAXParser matching the given
     *         criteria is available.
     * @throws SAXNotRecognizedException If the given feature is not known to
     *         the underlying implementation.
     * @throws SAXNotSupportedException If the given features is known, but not
     *         supported by the underlying implementation.
     */
    public abstract void setFeature(String name, boolean value)
            throws ParserConfigurationException, SAXNotRecognizedException,
            SAXNotSupportedException;

    /**
     * Determines whether the factory is configured to deliver parsers that are
     * namespace-aware.
     * 
     * @param value Turns namespace-awareness on or off.
     */
    public void setNamespaceAware(boolean value) {
        namespaceAware = value;
    }

    /**
     * Sets the desired XML Schema object.
     * 
     * @param schema The XML Schema object.
     */
    // TODO Do we want the validation package in?
    // public void setSchema(Schema schema) {
    // this.schema = schema;
    // }
    /**
     * Determines whether the factory is configured to deliver parsers that are
     * validating.
     * 
     * @param value Turns validation on or off.
     */
    public void setValidating(boolean value) {
        validating = value;
    }

    /**
     * Determines whether the factory is configured to deliver parsers that are
     * XInclude-aware.
     * 
     * @param value Turns XInclude-awareness on or off.
     */
    public void setXIncludeAware(boolean value) {
        throw new UnsupportedOperationException();
    }

}

