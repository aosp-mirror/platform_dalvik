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

package org.apache.xalan.extensions;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionResolver;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;

/**
 * A sample implementation of XPathFunctionResolver, with support for
 * EXSLT extension functions and Java extension functions.
 */
public class XPathFunctionResolverImpl implements XPathFunctionResolver
{
    /**
     * Resolve an extension function from the qualified name and arity.
     */
    public XPathFunction resolveFunction(QName qname, int arity)
    {
        if (qname == null)
            throw new NullPointerException(
                XSLMessages.createMessage(
                    XSLTErrorResources.ER_XPATH_RESOLVER_NULL_QNAME, null));
        
        if (arity < 0)
            throw new IllegalArgumentException(
                XSLMessages.createMessage(
                    XSLTErrorResources.ER_XPATH_RESOLVER_NEGATIVE_ARITY, null));
        
        String uri = qname.getNamespaceURI();
        if (uri == null || uri.length() == 0)
            return null;
        
        String className = null;
        String methodName = null;
        if (uri.startsWith("http://exslt.org"))
        {
            className = getEXSLTClassName(uri);
            methodName = qname.getLocalPart();
        }
        else if (!uri.equals(ExtensionNamespaceContext.JAVA_EXT_URI))
        {
            int lastSlash = className.lastIndexOf("/");
            if (-1 != lastSlash)
                className = className.substring(lastSlash + 1);
        }           
 
        String localPart = qname.getLocalPart();
        int lastDotIndex = localPart.lastIndexOf('.');
        if (lastDotIndex > 0)
        {
            if (className != null)
                className = className + "." + localPart.substring(0, lastDotIndex);
            else
                className = localPart.substring(0, lastDotIndex);
                
            methodName = localPart.substring(lastDotIndex + 1);
        }
        else
            methodName = localPart;
     
        if(null == className || className.trim().length() == 0 
           || null == methodName || methodName.trim().length() == 0) 
            return null;
    
        ExtensionHandler handler = null;
        try
        {
            ExtensionHandler.getClassForName(className);
            handler = new ExtensionHandlerJavaClass(uri, "javaclass", className);
        }
        catch (ClassNotFoundException e)
        {
           return null;
        }
        return new XPathFunctionImpl(handler, methodName);
    }
    
    /**
     * Return the implementation class name of an EXSLT extension from
     * a given namespace uri. The uri must starts with "http://exslt.org".
     */
    private String getEXSLTClassName(String uri)
    {
        if (uri.equals(ExtensionNamespaceContext.EXSLT_MATH_URI))
            return "org.apache.xalan.lib.ExsltMath";
        else if (uri.equals(ExtensionNamespaceContext.EXSLT_SET_URI))
            return "org.apache.xalan.lib.ExsltSets";
        else if (uri.equals(ExtensionNamespaceContext.EXSLT_STRING_URI))
            return "org.apache.xalan.lib.ExsltStrings";
        else if (uri.equals(ExtensionNamespaceContext.EXSLT_DATETIME_URI))
            return "org.apache.xalan.lib.ExsltDatetime";
        else if (uri.equals(ExtensionNamespaceContext.EXSLT_DYNAMIC_URI))
            return "org.apache.xalan.lib.ExsltDynamic";
        else if (uri.equals(ExtensionNamespaceContext.EXSLT_URI))
            return "org.apache.xalan.lib.ExsltCommon";
        else
            return null;
    }
}
