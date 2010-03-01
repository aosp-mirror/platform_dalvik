/*
 * Copyright (C) 2010 The Android Open Source Project
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

package org.apache.harmony.xml.dom;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;

import java.util.Map;
import java.util.TreeMap;

/**
 * A minimal implementation of DOMConfiguration. This implementation uses inner
 * parameter instances to centralize each parameter's behaviour.
 */
public final class DOMConfigurationImpl implements DOMConfiguration {

    private static final Map<String, Parameter> PARAMETERS
            = new TreeMap<String, Parameter>(String.CASE_INSENSITIVE_ORDER);

    static {
        /*
         * True to canonicalize the document (unsupported). This includes
         * removing DocumentType nodes from the tree and removing unused
         * namespace declarations. Setting this to true also sets these
         * parameters:
         *   entities = false
         *   normalize-characters = false
         *   cdata-sections = false
         *   namespaces = true
         *   namespace-declarations = true
         *   well-formed = true
         *   element-content-whitespace = true
         * Setting these parameters to another value shall revert the canonical
         * form to false.
         */
        PARAMETERS.put("canonical-form", new FixedParameter(false));

        /*
         * True to keep existing CDATA nodes; false to replace them/merge them
         * into adjacent text nodes.
         */
        PARAMETERS.put("cdata-sections", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.cdataSections;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                config.cdataSections = (Boolean) value;
            }
        });

        /*
         * True to check character normalization (unsupported).
         */
        PARAMETERS.put("check-character-normalization", new FixedParameter(false));

        /*
         * True to keep comments in the document; false to discard them.
         */
        PARAMETERS.put("comments", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.comments;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                config.comments = (Boolean) value;
            }
        });

        /*
         * True to expose schema normalized values. Setting this to true sets
         * the validate parameter to true. Has no effect when validate is false.
         */
        PARAMETERS.put("datatype-normalization", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.datatypeNormalization;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                if ((Boolean) value) {
                    config.datatypeNormalization = true;
                    config.validate = true;
                } else {
                    config.datatypeNormalization = false;
                }
            }
        });

        /*
         * True to keep whitespace elements in the document; false to discard
         * them (unsupported).
         */
        PARAMETERS.put("element-content-whitespace", new FixedParameter(true));

        /*
         * True to keep entity references in the document; false to expand them.
         */
        PARAMETERS.put("entities", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.entities;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                config.entities = (Boolean) value;
            }
        });

        /*
         * Handler to be invoked when errors are encountered.
         */
        PARAMETERS.put("error-handler", new Parameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.errorHandler;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                config.errorHandler = (DOMErrorHandler) value;
            }
            public boolean canSet(DOMConfigurationImpl config, Object value) {
                return value == null || value instanceof DOMErrorHandler;
            }
        });

        /*
         * Bulk alias to set the following parameter values:
         *   validate-if-schema = false
         *   entities = false
         *   datatype-normalization = false
         *   cdata-sections = false
         *   namespace-declarations = true
         *   well-formed = true
         *   element-content-whitespace = true
         *   comments = true
         *   namespaces = true.
         * Querying this returns true if all of the above parameters have the
         * listed values; false otherwise.
         */
        PARAMETERS.put("infoset", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                // validate-if-schema is always false
                // element-content-whitespace is always true
                // namespace-declarations is always true
                return !config.entities
                        && !config.datatypeNormalization
                        && !config.cdataSections
                        && config.wellFormed
                        && config.comments
                        && config.namespaces;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                if ((Boolean) value) {
                    // validate-if-schema is always false
                    // element-content-whitespace is always true
                    // namespace-declarations is always true
                    config.entities = false;
                    config.datatypeNormalization = false;
                    config.cdataSections = false;
                    config.wellFormed = true;
                    config.comments = true;
                    config.namespaces = true;
                }
            }
        });

        /*
         * True to perform namespace processing; false for none.
         */
        PARAMETERS.put("namespaces", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.namespaces;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                config.namespaces = (Boolean) value;
            }
        });

        /**
         * True to include namespace declarations; false to discard them
         * (unsupported). Even when namespace declarations are discarded,
         * prefixes are retained.
         *
         * Has no effect if namespaces is false.
         */
        PARAMETERS.put("namespace-declarations", new FixedParameter(true));

        /*
         * True to fully normalize characters (unsupported).
         */
        PARAMETERS.put("normalize-characters", new FixedParameter(false));

        /*
         * A list of whitespace-separated URIs representing the schemas to validate
         * against. Has no effect if schema-type is null.
         */
        PARAMETERS.put("schema-location", new Parameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.schemaLocation;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                config.schemaLocation = (String) value;
            }
            public boolean canSet(DOMConfigurationImpl config, Object value) {
                return value == null || value instanceof String;
            }
        });

        /*
         * URI representing the type of schema language, such as
         * "http://www.w3.org/2001/XMLSchema" or "http://www.w3.org/TR/REC-xml".
         */
        PARAMETERS.put("schema-type", new Parameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.schemaType;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                config.schemaType = (String) value;
            }
            public boolean canSet(DOMConfigurationImpl config, Object value) {
                return value == null || value instanceof String;
            }
        });

        /*
         * True to split CDATA sections containing "]]>"; false to signal an
         * error instead.
         */
        PARAMETERS.put("split-cdata-sections", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.splitCdataSections;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                config.splitCdataSections = (Boolean) value;
            }
        });

        /*
         * True to require validation against a schema or DTD. Validation will
         * recompute element content whitespace, ID and schema type data.
         *
         * Setting this unsets validate-if-schema.
         */
        PARAMETERS.put("validate", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.validate;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                // validate-if-schema is always false
                config.validate = (Boolean) value;
            }
        });

        /*
         * True to validate if a schema was declared (unsupported). Setting this
         * unsets validate.
         */
        PARAMETERS.put("validate-if-schema", new FixedParameter(false));

        /*
         * True to report invalid characters in node names, attributes, elements,
         * comments, text, CDATA sections and processing instructions.
         */
        PARAMETERS.put("well-formed", new BooleanParameter() {
            public Object get(DOMConfigurationImpl config) {
                return config.wellFormed;
            }
            public void set(DOMConfigurationImpl config, Object value) {
                config.wellFormed = (Boolean) value;
            }
        });

        // TODO add "resource-resolver" property for use with LS feature...
    }

    private boolean cdataSections = true;
    private boolean comments = true;
    private boolean datatypeNormalization = false;
    private boolean entities = true;
    private DOMErrorHandler errorHandler;
    private boolean namespaces = true;
    private String schemaLocation;
    private String schemaType;
    private boolean splitCdataSections = true;
    private boolean validate = false;
    private boolean wellFormed = true;

    interface Parameter {
        Object get(DOMConfigurationImpl config);
        void set(DOMConfigurationImpl config, Object value);
        boolean canSet(DOMConfigurationImpl config, Object value);
    }

    static class FixedParameter implements Parameter {
        final Object onlyValue;
        FixedParameter(Object onlyValue) {
            this.onlyValue = onlyValue;
        }
        public Object get(DOMConfigurationImpl config) {
            return onlyValue;
        }
        public void set(DOMConfigurationImpl config, Object value) {
            if (!onlyValue.equals(value)) {
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                        "Unsupported value: " + value);
            }
        }
        public boolean canSet(DOMConfigurationImpl config, Object value) {
            return onlyValue.equals(value);
        }
    }

    static abstract class BooleanParameter implements Parameter {
        public boolean canSet(DOMConfigurationImpl config, Object value) {
            return value instanceof Boolean;
        }
    }

    public boolean canSetParameter(String name, Object value) {
        Parameter parameter = PARAMETERS.get(name);
        return parameter != null && parameter.canSet(this, value);
    }

    public void setParameter(String name, Object value) throws DOMException {
        Parameter parameter = PARAMETERS.get(name);
        if (parameter == null) {
            throw new DOMException(DOMException.NOT_FOUND_ERR, "No such parameter: " + name);
        }
        try {
            parameter.set(this, value);
        } catch (NullPointerException e) {
            throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
                    "Null not allowed for " + name);
        } catch (ClassCastException e) {
            throw new DOMException(DOMException.TYPE_MISMATCH_ERR,
                    "Invalid type for " + name + ": " + value.getClass());
        }
    }

    public Object getParameter(String name) throws DOMException {
        Parameter parameter = PARAMETERS.get(name);
        if (parameter == null) {
            throw new DOMException(DOMException.NOT_FOUND_ERR, "No such parameter: " + name);
        }
        return parameter.get(this);
    }

    public DOMStringList getParameterNames() {
        final String[] result = PARAMETERS.keySet().toArray(new String[PARAMETERS.size()]);
        return new DOMStringList() {
            public String item(int index) {
                return index < result.length ? result[index] : null;
            }
            public int getLength() {
                return result.length;
            }
            public boolean contains(String str) {
                return PARAMETERS.containsKey(str); // case-insensitive.
            }
        };
    }
}
