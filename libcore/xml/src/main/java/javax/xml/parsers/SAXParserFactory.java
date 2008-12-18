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
 * Provides a factory for {@link SAXParser} instances. The class first needs to
 * be instantiated using the {@link #newInstance()} method. The instance can be
 * configured as desired. A call to its {@link #newSAXParser()} then provides a
 * {@code SAXParser} instance matching this configuration, if possible.
 * 
 * @since Android 1.0
 */
public abstract class SAXParserFactory {

    private boolean namespaceAware;

    private boolean validating;

    private boolean xincludeAware;

    /**
     * Do-nothing constructor. Prevents instantiation. To be overridden by
     * concrete subclasses.
     * 
     * @since Android 1.0
     */
    protected SAXParserFactory() {
        // Does nothing.
    }

    /**
     * Queries a feature from the underlying implementation.
     * 
     * @param name The name of the feature. The default Android implementation
     *             of {@link SAXParser} supports only the following three
     *             features:
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
     * @return the status of the feature.
     * 
     * @throws ParserConfigurationException if no {@code SAXParser} matching the
     *         given criteria is available.
     * @throws SAXNotRecognizedException if the given feature is not known to
     *         the underlying implementation.
     * @throws SAXNotSupportedException if the given features is known, but not
     *         supported by the underlying implementation.
     *         
     * @since Android 1.0
     */
    public abstract boolean getFeature(String name)
            throws ParserConfigurationException, SAXNotRecognizedException,
            SAXNotSupportedException;

// TODO No XSchema support in Android 1.0. Maybe later.
//    /**
//     * Queries the desired XML Schema object.
//     * 
//     * @return The XML Schema object, if it has been set by a call to setSchema,
//     *         or null otherwise.
//     */
//    public javax.xml.validation.Schema getSchema() {
//        return schema;
//    }
    
    /**
     * Queries whether the factory is configured to deliver parsers that are
     * namespace-aware.
     * 
     * @return {@code true} if namespace-awareness is desired, {@code false}
     *         otherwise.
     * 
     * @since Android 1.0
     */
    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    /**
     * Queries whether the factory is configured to deliver parsers that are
     * validating.
     * 
     * @return {@code true} if validating is desired, {@code false} otherwise.
     * 
     * @since Android 1.0
     */
    public boolean isValidating() {
        return validating;
    }

    /**
     * Queries whether the factory is configured to deliver parsers that are
     * XInclude-aware.
     * 
     * @return {@code true} if XInclude-awareness is desired, {@code false}
     *         otherwise.
     * 
     * @since Android 1.0
     */
    public boolean isXIncludeAware() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new {@code SAXParserFactory} that can be configured and then be
     * used for creating {@link SAXParser} objects. The method first checks the
     * value of the {@code SAXParserFactory} property. If this
     * is non-{@code null}, it is assumed to be the name of a class that serves
     * as the factory. The class is instantiated, and the instance is returned.
     * If the property value is {@code null}, the system's default factory
     * implementation is returned. 
     * 
     * @return the {@code SAXParserFactory}.
     * 
     * @throws FactoryConfigurationError if no {@code SAXParserFactory} can be
     *         created.
     *         
     * @since Android 1.0
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
     * Creates a new {@link SAXParser} that matches the current configuration of
     * the factory.
     * 
     * @return the {@code SAXParser}.
     * 
     * @throws ParserConfigurationException if no matching {@code SAXParser}
     *         could be found.
     * @throws SAXException if creating the {@code SAXParser} failed due to some
     *         other reason.
     * 
     * @since Android 1.0
     */
    public abstract SAXParser newSAXParser()
            throws ParserConfigurationException, SAXException;

    /**
     * Sets a feature in the underlying implementation.
     * 
     * @param name the name of the feature. The default Android implementation
     *             of {@link SAXParser} supports only the following three
     *             features:
     *             
     *             <dl>
     *               <dt>{@code http://xml.org/sax/features/namespaces}</dt>
     *               <dd>Sets the state of namespace-awareness.</dd>
     *
     *               <dt>
     *                 {@code http://xml.org/sax/features/namespace-prefixes}
     *               </dt>
     *               <dd>Sets the state of namespace prefix processing</dd>
     *
     *               <dt>{@code http://xml.org/sax/features/validation}</dt>
     *               <dd>Sets the state of validation.</dd>
     *             </dl>
     *             
     *             Note that despite the ability to query the validation
     *             feature, there is currently no validating parser available.
     *             Also note that currently either namespaces or 
     *             namespace prefixes can be enabled, but not both at the same 
     *             time.
     *             
     * @param value the status of the feature.
     * 
     * @throws ParserConfigurationException if no {@code SAXParser} matching
     *         the given criteria is available.
     * @throws SAXNotRecognizedException if the given feature is not known to
     *         the underlying implementation.
     * @throws SAXNotSupportedException if the given features is known, but not
     *         supported by the underlying implementation.
     *         
     * @since Android 1.0
     */
    public abstract void setFeature(String name, boolean value)
            throws ParserConfigurationException, SAXNotRecognizedException,
            SAXNotSupportedException;

    /**
     * Determines whether the factory is configured to deliver parsers that are
     * namespace-aware.
     * 
     * @param value turns namespace-awareness on or off.
     * 
     * @since Android 1.0
     */
    public void setNamespaceAware(boolean value) {
        namespaceAware = value;
    }

// TODO No XSchema support in Android 1.0. Maybe later.
//    /**
//     * Sets the desired XML Schema object.
//     * 
//     * @param schema The XML Schema object.
//     */
//    public void setSchema(Schema schema) {
//       this.schema = schema;
//    }

    /**
     * Determines whether the factory is configured to deliver parsers that are
     * validating.
     * 
     * @param value turns validation on or off.
     * 
     * @since Android 1.0
     */
    public void setValidating(boolean value) {
        validating = value;
    }

    /**
     * Determines whether the factory is configured to deliver parsers that are
     * XInclude-aware.
     * 
     * @param value turns XInclude-awareness on or off.
     * 
     * @since Android 1.0
     */
    public void setXIncludeAware(boolean value) {
        throw new UnsupportedOperationException();
    }

}

