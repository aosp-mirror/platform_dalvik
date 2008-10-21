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

import org.apache.harmony.xml.parsers.DocumentBuilderFactoryImpl;

/**
 * Provides a factory for DocumentBuilder instances. The class first needs to be
 * instantiated using the newInstance() method. The instance can be configured
 * as desired. A call to newDocumentBuilder () then provides a DocumentBuilder
 * instance matching this configuration (if possible).
 */
public abstract class DocumentBuilderFactory extends java.lang.Object {

    private boolean coalesce;

    private boolean expandEntityReferences;

    private boolean ignoreComments;

    private boolean ignoreElementContentWhitespace;

    private boolean namespaceAware;

    private boolean validate;

    /**
     * Do-nothing constructor. To be overridden by concrete document builders.
     */
    protected DocumentBuilderFactory() {
        // Does nothing.
    }

    /**
     * Queries an attribute from the underlying implementation.
     * 
     * @param name The name of the attribute.
     * @return The value of the attribute.
     * 
     * @throws java.lang.IllegalArgumentException if the argument is unknown to
     *         the underlying implementation.
     */
    public abstract Object getAttribute(String name)
            throws IllegalArgumentException;

    /**
     * Queries a feature from the underlying implementation.
     * 
     * @param name The name of the feature. The default Android implementation
     *             of DocumentBuilder supports only the following three
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
     *               <dt>
     *                 {@code http://xml.org/sax/features/validation}
     *               </dt>
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
     * @throws java.lang.IllegalArgumentException if the feature is unknown to
     *         the underlying implementation.
     * @throws javax.xml.parsers.ParserConfigurationException if the feature is
     *         known, but not supported.
     */
    public abstract boolean getFeature(String name)
            throws javax.xml.parsers.ParserConfigurationException;

    /**
     * Queries the desired XML Schema object.
     * 
     * @return The XML Schema object, if it has been set by a call to setSchema,
     *         or null otherwise.
     */
    // TODO Do we want the validation package in?
    // public javax.xml.validation.Schema getSchema() {
    //     return schema;
    // }
    
    /**
     * Queries whether the factory is configured to deliver parsers that convert
     * CDATA nodes to text nodes and melt them with neighbouring nodes.
     * 
     * @return true if coalescing is desired, false otherwise.
     */
    public boolean isCoalescing() {
        return coalesce;
    }

    /**
     * Queries whether the factory is configured to deliver parsers that expand
     * entity references.
     * 
     * @return true if entity expansion is desired, false otherwise.
     */
    public boolean isExpandEntityReferences() {
        return expandEntityReferences;
    }

    /**
     * Queries whether the factory is configured to deliver parsers that ignore
     * comments.
     * 
     * @return true if comment ignorance is desired, false otherwise.
     */
    public boolean isIgnoringComments() {
        return ignoreComments;
    }

    /**
     * Queries whether the factory is configured to deliver parsers that ignore
     * whitespace in elements.
     * 
     * @return true if whitespace ignorance is desired, false otherwise.
     */
    public boolean isIgnoringElementContentWhitespace() {
        return ignoreElementContentWhitespace;
    }

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
        return validate;
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
     * Creates a new DocumentBuilder that matches the current configuration.
     * 
     * @return The DocumentBuilder.
     * @throws javax.xml.parsers.ParserConfigurationException if no matching
     *         DocumentBuilder could be found.
     */
    public abstract javax.xml.parsers.DocumentBuilder newDocumentBuilder()
            throws javax.xml.parsers.ParserConfigurationException;

    /**
     * Creates a new DocumentBuilderFactory that can be configured and then be
     * used for creating DocumentBuilder objects.
     * 
     * @return The DocumentBuilderFactory.
     * @throws FactoryConfigurationError If no DocumentBuilderFactory can be
     *         created.
     */
    public static javax.xml.parsers.DocumentBuilderFactory newInstance()
            throws FactoryConfigurationError {
        // TODO Properties file and META-INF case missing here. See spec.
        String factory = System
                .getProperty("javax.xml.parsers.DocumentBuilderFactory");
        if (factory != null) {
            try {
                return (DocumentBuilderFactory) Class.forName(factory)
                        .newInstance();
            } catch (Exception ex) {
                // Ignore.
            }
        }

        try {
            return new DocumentBuilderFactoryImpl();
        } catch (Exception ex) {
            // Ignore.
        }

        throw new FactoryConfigurationError(
                "Cannot create DocumentBuilderFactory");
    }

    /**
     * Sets an attribute in the underlying implementation.
     * 
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     * 
     * @throws java.lang.IllegalArgumentException if the argument is unknown to
     *         the underlying implementation.
     */
    public abstract void setAttribute(String name, Object value)
            throws IllegalArgumentException;

    /**
     * Determines whether the factory is configured to deliver parsers that
     * convert CDATA nodes to text nodes and melt them with neighbouring nodes.
     * 
     * @param value Turns coalescing on or off.
     */
    public void setCoalescing(boolean value) {
        coalesce = value;
    }

    /**
     * Determines whether the factory is configured to deliver parsers that
     * expands entity references.
     * 
     * @param value Turns entity reference expansion on or off.
     */
    public void setExpandEntityReferences(boolean value) {
        expandEntityReferences = value;
    }

    /**
     * Sets a feature in the underlying implementation.
     * 
     * @param name The name of the feature. The default Android implementation
     *             of DocumentBuilder supports only the following three
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
     * @param value The value of the feature.
     * 
     * @throws ParserConfigurationException if the feature is unknown to the
     *         underlying implementation.
     */
    public abstract void setFeature(String name, boolean value)
            throws ParserConfigurationException;

    /**
     * Determines whether the factory is configured to deliver parsers that
     * ignore comments.
     * 
     * @param value Turns comment ignorance on or off.
     */
    public void setIgnoringComments(boolean value) {
        ignoreComments = value;
    }

    /**
     * Determines whether the factory is configured to deliver parsers that
     * ignores element whitespace.
     * 
     * @param value Turns element whitespace ignorance on or off.
     */
    public void setIgnoringElementContentWhitespace(boolean value) {
        ignoreElementContentWhitespace = value;
    }

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
    //     this.schema = schema;
    // }
    
    /**
     * Determines whether the factory is configured to deliver parsers that are
     * validating.
     * 
     * @param value Turns validation on or off.
     */
    public void setValidating(boolean value) {
        validate = value;
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
