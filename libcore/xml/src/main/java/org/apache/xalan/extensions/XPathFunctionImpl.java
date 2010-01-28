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

import java.util.List;
import java.util.Vector;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

/**
 * A sample implementation of XPathFunction, with support for
 * EXSLT extension functions and Java extension functions.
 */
public class XPathFunctionImpl implements XPathFunction
{
    private ExtensionHandler m_handler;
    private String m_funcName;
    
    /**
     * Construct an instance of XPathFunctionImpl from the
     * ExtensionHandler and function name.
     */
    public XPathFunctionImpl(ExtensionHandler handler, String funcName)
    {
        m_handler = handler;
        m_funcName = funcName;
    }
        
    /**
     * @see javax.xml.xpath.XPathFunction#evaluate(java.util.List)
     */
    public Object evaluate(List args)
        throws XPathFunctionException
    {
        Vector  argsVec = listToVector(args);
        
        try {
            // The method key and ExpressionContext are set to null.
            return m_handler.callFunction(m_funcName, argsVec, null, null);
        }
        catch (TransformerException e)
        {
            throw new XPathFunctionException(e);
        }
    }
    
    /**
     * Convert a java.util.List to a java.util.Vector. 
     * No conversion is done if the List is already a Vector.
     */
    private static Vector listToVector(List args)
    {
        if (args == null)
            return null;
        else if (args instanceof Vector)
            return (Vector)args;
        else
        {
            Vector result = new Vector();
            result.addAll(args);
            return result;
        }        
    }
}
