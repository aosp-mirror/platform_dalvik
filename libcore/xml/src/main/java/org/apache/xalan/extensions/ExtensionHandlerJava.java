/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: ExtensionHandlerJava.java 468637 2006-10-28 06:51:02Z minchau $
 */
package org.apache.xalan.extensions;

import java.util.Hashtable;

/**
 * Abstract base class handling the java language extensions for XPath.
 * This base class provides cache management shared by all of the
 * various java extension handlers.
 *
 * @xsl.usage internal
 */
public abstract class ExtensionHandlerJava extends ExtensionHandler
{

  /** Extension class name         */
  protected String m_className = "";

  /** Table of cached methods          */
  private Hashtable m_cachedMethods = new Hashtable();

  /**
   * Construct a new extension handler given all the information
   * needed.
   *
   * @param namespaceUri the extension namespace URI that I'm implementing
   * @param funcNames    string containing list of functions of extension NS
   * @param lang         language of code implementing the extension
   * @param srcURL       value of src attribute (if any) - treated as a URL
   *                     or a classname depending on the value of lang. If
   *                     srcURL is not null, then scriptSrc is ignored.
   * @param scriptSrc    the actual script code (if any)
   * @param scriptLang   the scripting language
   * @param className    the extension class name 
   */
  protected ExtensionHandlerJava(String namespaceUri, String scriptLang,
                                 String className)
  {

    super(namespaceUri, scriptLang);

    m_className = className;
  }

  /**
   * Look up the entry in the method cache.
   * @param methodKey   A key that uniquely identifies this invocation in
   *                    the stylesheet.
   * @param objType     A Class object or instance object representing the type
   * @param methodArgs  An array of the XObject arguments to be used for
   *                    function mangling.
   *
   * @return The given method from the method cache
   */
  public Object getFromCache(Object methodKey, Object objType,
                             Object[] methodArgs)
  {

    // Eventually, we want to insert code to mangle the methodKey with methodArgs
    return m_cachedMethods.get(methodKey);
  }

  /**
   * Add a new entry into the method cache.
   * @param methodKey   A key that uniquely identifies this invocation in
   *                    the stylesheet.
   * @param objType     A Class object or instance object representing the type
   * @param methodArgs  An array of the XObject arguments to be used for
   *                    function mangling.
   * @param methodObj   A Class object or instance object representing the method
   *
   * @return The cached method object
   */
  public Object putToCache(Object methodKey, Object objType,
                           Object[] methodArgs, Object methodObj)
  {

    // Eventually, we want to insert code to mangle the methodKey with methodArgs
    return m_cachedMethods.put(methodKey, methodObj);
  }
}
