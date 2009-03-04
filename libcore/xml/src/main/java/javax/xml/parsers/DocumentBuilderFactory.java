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
 * Provides a factory for {@link DocumentBuilder} instances. The class first
 * needs to be instantiated using the {@link #newInstance()} method. The
 * instance can be configured as desired. A call to
 * {@link #newDocumentBuilder()} then provides a {@code DocumentBuilder}
 * instance matching this configuration (if possible).
 * 
 * @since Android 1.0
 */
public abstract class DocumentBuilderFactory extends Object {

    private boolean coalesce;

    private boolean expandEntityReferences;

    private boolean ignoreComments;

    private boolean ignoreElementContentWhitespace;

    private boolean namespaceAware;

    private boolean validate;

    /**
     * Do-nothing constructor. To be overridden by concrete document builders.
     * 
     * @since Android 1.0
     */
    protected DocumentBuilderFactory() {
        // Does nothing.
    }

    /**
     * Queries an attribute from the underlying implementation.
     * 
     * @param name the name of the attribute.
     * @return the value of the attribute.
     * 
     * @throws IllegalArgumentException if the argument is unknown to the
     *         underlying implementation.
     * 
     * @since Android 1.0
     */
    public abstract Object getAttribute(String name)
            throws IllegalArgumentException;

    /**
     * Queries a feature from the underlying implementation.
     * 
     * @param name The name of the feature. The default Android implementation
     *             of {@link DocumentBuilder} supports only the following three
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
     * @return the status of the feature.
     * 
     * @throws IllegalArgumentException if the feature is unknown to
     *         the underlying implementation.
     * @throws ParserConfigurationException if the feature is
     *         known, but not supported.
     * 
     * @since Android 1.0
     */
    public abstract boolean getFeature(String name)
            throws ParserConfigurationException;

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
     * Queries whether the factory is configured to deliver parsers that convert
     * CDATA nodes to text nodes and melt them with neighboring nodes. This is
     * called "coalescing".
     * 
     * @return {@code true} if coalescing is desired, {@code false} otherwise.
     * 
     * @since Android 1.0
     */
    public boolean isCoalescing() {
        return coalesce;
    }

    /**
     * Queries whether the factory is configured to deliver parsers that expand
     * entity references.
     * 
     * @return {@code true} if entity expansion is desired, {@code false}
     * otherwise.
     * 
     * @since Android 1.0
     */
    public boolean isExpandEntityReferences() {
        return expandEntityReferences;
    }

    /**
     * Queries whether the factory is configured to deliver parsers that ignore
     * comments.
     * 
     * @return {@code true} if comment ignorance is desired, {@code false}
     * otherwise.
     * 
     * @since Android 1.0
     */
    public boolean isIgnoringComments() {
        return ignoreComments;
    }

    /**
     * Queries whether the factory is configured to deliver parsers that ignore
     * whitespace in elements.
     * 
     * @return {@code true} if whitespace ignorance is desired, {@code false}
     * otherwise.
     * 
     * @since Android 1.0
     */
    public boolean isIgnoringElementContentWhitespace() {
        return ignoreElementContentWhitespace;
    }

    /**
     * Queries whether the factory is configured to deliver parsers that are
     * namespace-aware.
     * 
     * @return {@code true} if namespace-awareness is desired, {@code false}
     * otherwise.
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
        return validate;
    }

    /**
     * Queries whether the factory is configured to deliver parsers that are
     * XInclude-aware.
     * 
     * @return {@code true} if XInclude-awareness is desired, {@code false}
     * otherwise.
     * 
     * @since Android 1.0
     */
    public boolean isXIncludeAware() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new {@link DocumentBuilder} that matches the current
     * configuration of the factory.
     * 
     * @return the DocumentBuilder.
     * @throws ParserConfigurationException if no matching
     *         {@code DocumentBuilder} could be found.
     * 
     * @since Android 1.0
     */
    public abstract DocumentBuilder newDocumentBuilder()
            throws ParserConfigurationException;

    /**
     * Creates a new DocumentBuilderFactory that can be configured and then be
     * used for creating DocumentBuilder objects. The method first checks the
     * value of the {@code DocumentBuilderFactory} property.
     * If this is non-{@code null}, it is assumed to be the name of a class
     * that serves as the factory. The class is instantiated, and the instance
     * is returned. If the property value is {@code null}, the system's default
     * factory implementation is returned.
     * 
     * @return the DocumentBuilderFactory.
     * @throws FactoryConfigurationError if no {@code DocumentBuilderFactory}
     *         can be created.
     * 
     * @since Android 1.0
     */
    public static DocumentBuilderFactory newInstance()
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
     * @param name the name of the attribute.
     * @param value the value of the attribute.
     * 
     * @throws IllegalArgumentException if the argument is unknown to the
     *         underlying implementation.
     * 
     * @since Android 1.0
     */
    public abstract void setAttribute(String name, Object value)
            throws IllegalArgumentException;

    /**
     * Determines whether the factory is configured to deliver parsers that
     * convert CDATA nodes to text nodes and melt them with neighboring nodes.
     * This is called "coalescing".
     * 
     * @param value turns coalescing on or off.
     * 
     * @since Android 1.0
     */
    public void setCoalescing(boolean value) {
        coalesce = value;
    }

    /**
     * Determines whether the factory is configured to deliver parsers that
     * expands entity references.
     * 
     * @param value turns entity reference expansion on or off.
     * 
     * @since Android 1.0
     */
    public void setExpandEntityReferences(boolean value) {
        expandEntityReferences = value;
    }

    /**
     * Sets a feature in the underlying implementation.
     * 
     * @param name the name of the feature. The default Android implementation
     *             of {@link DocumentBuilder} supports only the following three
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
     *             Note that despite the ability to set the validation
     *             feature, there is currently no validating parser available.
     *             Also note that currently either namespaces or
     *             namespace prefixes can be enabled, but not both at the same
     *             time.
     * 
     * @param value the value of the feature.
     * 
     * @throws ParserConfigurationException if the feature is unknown to the
     *         underlying implementation.
     * 
     * @since Android 1.0
     */
    public abstract void setFeature(String name, boolean value)
            throws ParserConfigurationException;

    /**
     * Determines whether the factory is configured to deliver parsers that
     * ignore comments.
     * 
     * @param value turns comment ignorance on or off.
     * 
     * @since Android 1.0
     */
    public void setIgnoringComments(boolean value) {
        ignoreComments = value;
    }

    /**
     * Determines whether the factory is configured to deliver parsers that
     * ignores element whitespace.
     * 
     * @param value turns element whitespace ignorance on or off.
     * 
     * @since Android 1.0
     */
    public void setIgnoringElementContentWhitespace(boolean value) {
        ignoreElementContentWhitespace = value;
    }

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
//        this.schema = schema;
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
        validate = value;
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
