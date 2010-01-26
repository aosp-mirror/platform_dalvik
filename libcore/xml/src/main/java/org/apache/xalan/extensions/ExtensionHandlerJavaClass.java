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
 * $Id: ExtensionHandlerJavaClass.java 469672 2006-10-31 21:56:19Z minchau $
 */

package org.apache.xalan.extensions;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.trace.ExtensionEvent;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.functions.FuncExtFunction;
import org.apache.xpath.objects.XObject;

/**
 * Represents an extension namespace for XPath that handles java classes.
 * It is recommended that the class URI be of the form:
 * <pre>
 *   xalan://fully.qualified.class.name
 * </pre>
 * However, we do not enforce this.  If the class name contains a
 * a /, we only use the part to the right of the rightmost slash.
 * In addition, we ignore any "class:" prefix.
 * Provides functions to test a function's existence and call a function.
 * Also provides functions to test an element's existence and call an
 * element.
 *
 * @author <a href="mailto:garyp@firstech.com">Gary L Peskin</a>
 * @xsl.usage internal
 */

public class ExtensionHandlerJavaClass extends ExtensionHandlerJava
{

  private Class m_classObj = null;

  /**
   * Provides a default Instance for use by elements that need to call 
   * an instance method.
   */

  private Object m_defaultInstance = null;


  /**
   * Construct a new extension namespace handler given all the information
   * needed. 
   * @param namespaceUri the extension namespace URI that I'm implementing
   * @param scriptLang   language of code implementing the extension
   * @param className    the fully qualified class name of the class
   */
  public ExtensionHandlerJavaClass(String namespaceUri,
                                   String scriptLang,
                                   String className)
  {
    super(namespaceUri, scriptLang, className);
    try
    {
      m_classObj = getClassForName(className);
    }
    catch (ClassNotFoundException e)
    {
      // For now, just let this go.  We'll catch it when we try to invoke a method.
    }
  }


  /**
   * Tests whether a certain function name is known within this namespace.
   * Simply looks for a method with the appropriate name.  There is
   * no information regarding the arguments to the function call or
   * whether the method implementing the function is a static method or
   * an instance method.
   * @param function name of the function being tested
   * @return true if its known, false if not.
   */

  public boolean isFunctionAvailable(String function) 
  {
    Method[] methods = m_classObj.getMethods();
    int nMethods = methods.length;
    for (int i = 0; i < nMethods; i++)
    {
      if (methods[i].getName().equals(function))
        return true;
    }
    return false;
  }


  /**
   * Tests whether a certain element name is known within this namespace.
   * Looks for a method with the appropriate name and signature.
   * This method examines both static and instance methods.
   * @param element name of the element being tested
   * @return true if its known, false if not.
   */

  public boolean isElementAvailable(String element) 
  {
    Method[] methods = m_classObj.getMethods();
    int nMethods = methods.length;
    for (int i = 0; i < nMethods; i++)
    {
      if (methods[i].getName().equals(element))
      {
        Class[] paramTypes = methods[i].getParameterTypes();
        if ( (paramTypes.length == 2)
          && paramTypes[0].isAssignableFrom(
                        org.apache.xalan.extensions.XSLProcessorContext.class)
          && paramTypes[1].isAssignableFrom(
                        org.apache.xalan.templates.ElemExtensionCall.class) )
        {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * Process a call to a function in the java class represented by
   * this <code>ExtensionHandlerJavaClass<code>.
   * There are three possible types of calls:
   * <pre>
   *   Constructor:
   *     classns:new(arg1, arg2, ...)
   *
   *   Static method:
   *     classns:method(arg1, arg2, ...)
   *
   *   Instance method:
   *     classns:method(obj, arg1, arg2, ...)
   * </pre>
   * We use the following rules to determine the type of call made:
   * <ol type="1">
   * <li>If the function name is "new", call the best constructor for
   *     class represented by the namespace URI</li>
   * <li>If the first argument to the function is of the class specified
   *     in the namespace or is a subclass of that class, look for the best
   *     method of the class specified in the namespace with the specified
   *     arguments.  Compare all static and instance methods with the correct
   *     method name.  For static methods, use all arguments in the compare.
   *     For instance methods, use all arguments after the first.</li>
   * <li>Otherwise, select the best static or instance method matching
   *     all of the arguments.  If the best method is an instance method,
   *     call the function using a default object, creating it if needed.</li>
   * </ol>
   *
   * @param funcName Function name.
   * @param args     The arguments of the function call.
   * @param methodKey A key that uniquely identifies this class and method call.
   * @param exprContext The context in which this expression is being executed.
   * @return the return value of the function evaluation.
   * @throws TransformerException
   */

  public Object callFunction (String funcName, 
                              Vector args, 
                              Object methodKey,
                              ExpressionContext exprContext)
    throws TransformerException 
  {

    Object[] methodArgs;
    Object[][] convertedArgs;
    Class[] paramTypes;

    try
    {
      TransformerImpl trans = (exprContext != null) ?
          (TransformerImpl)exprContext.getXPathContext().getOwnerObject() : null;
      if (funcName.equals("new")) {                   // Handle constructor call

        methodArgs = new Object[args.size()];
        convertedArgs = new Object[1][];
        for (int i = 0; i < methodArgs.length; i++)
        {
          methodArgs[i] = args.get(i);
        }
        Constructor c = null;
        if (methodKey != null)
          c = (Constructor) getFromCache(methodKey, null, methodArgs);
        
        if (c != null && !trans.getDebug())
        {
          try
          {
            paramTypes = c.getParameterTypes();
            MethodResolver.convertParams(methodArgs, convertedArgs, 
                        paramTypes, exprContext);
            return c.newInstance(convertedArgs[0]);
          }
          catch (InvocationTargetException ite)
          {
            throw ite;
          }
          catch(Exception e)
          {
            // Must not have been the right one
          }
        }
        c = MethodResolver.getConstructor(m_classObj, 
                                          methodArgs,
                                          convertedArgs,
                                          exprContext);
        if (methodKey != null)
          putToCache(methodKey, null, methodArgs, c);
        
        if (trans != null && trans.getDebug()) {            
            trans.getTraceManager().fireExtensionEvent(new 
                    ExtensionEvent(trans, c, convertedArgs[0]));
            Object result;
            try {            
                result = c.newInstance(convertedArgs[0]);
            } catch (Exception e) {
                throw e;
            } finally {
                trans.getTraceManager().fireExtensionEndEvent(new 
                        ExtensionEvent(trans, c, convertedArgs[0]));
            }
            return result;
        } else
            return c.newInstance(convertedArgs[0]);
      }

      else
      {

        int resolveType;
        Object targetObject = null;
        methodArgs = new Object[args.size()];
        convertedArgs = new Object[1][];
        for (int i = 0; i < methodArgs.length; i++)
        {
          methodArgs[i] = args.get(i);
        }
        Method m = null;
        if (methodKey != null)
          m = (Method) getFromCache(methodKey, null, methodArgs);
        
        if (m != null && !trans.getDebug())
        {
          try
          {
            paramTypes = m.getParameterTypes();
            MethodResolver.convertParams(methodArgs, convertedArgs, 
                        paramTypes, exprContext);
            if (Modifier.isStatic(m.getModifiers()))
              return m.invoke(null, convertedArgs[0]);
            else
            {
              // This is tricky.  We get the actual number of target arguments (excluding any
              //   ExpressionContext).  If we passed in the same number, we need the implied object.
              int nTargetArgs = convertedArgs[0].length;
              if (ExpressionContext.class.isAssignableFrom(paramTypes[0]))
                nTargetArgs--;
              if (methodArgs.length <= nTargetArgs)
                return m.invoke(m_defaultInstance, convertedArgs[0]);
              else  
              {
                targetObject = methodArgs[0];
                
                if (targetObject instanceof XObject)
                  targetObject = ((XObject) targetObject).object();
                  
                return m.invoke(targetObject, convertedArgs[0]);
              }
            }
          }
          catch (InvocationTargetException ite)
          {
            throw ite;
          }
          catch(Exception e)
          {
            // Must not have been the right one
          }
        }

        if (args.size() > 0)
        {
          targetObject = methodArgs[0];

          if (targetObject instanceof XObject)
            targetObject = ((XObject) targetObject).object();

          if (m_classObj.isAssignableFrom(targetObject.getClass()))
            resolveType = MethodResolver.DYNAMIC;
          else
            resolveType = MethodResolver.STATIC_AND_INSTANCE;
        }
        else
        {
          targetObject = null;
          resolveType = MethodResolver.STATIC_AND_INSTANCE;
        }

        m = MethodResolver.getMethod(m_classObj,
                                     funcName,
                                     methodArgs, 
                                     convertedArgs,
                                     exprContext,
                                     resolveType);
        if (methodKey != null)
          putToCache(methodKey, null, methodArgs, m);

        if (MethodResolver.DYNAMIC == resolveType) {         // First argument was object type
          if (trans != null && trans.getDebug()) {
            trans.getTraceManager().fireExtensionEvent(m, targetObject, 
                        convertedArgs[0]);
            Object result;
            try {
                result = m.invoke(targetObject, convertedArgs[0]);
            } catch (Exception e) {
                throw e;
            } finally {
                trans.getTraceManager().fireExtensionEndEvent(m, targetObject, 
                        convertedArgs[0]);
            }
            return result;
          } else                  
            return m.invoke(targetObject, convertedArgs[0]);
        }
        else                                  // First arg was not object.  See if we need the implied object.
        {
          if (Modifier.isStatic(m.getModifiers())) {
            if (trans != null && trans.getDebug()) {
              trans.getTraceManager().fireExtensionEvent(m, null, 
                        convertedArgs[0]);
              Object result;
              try {
                  result = m.invoke(null, convertedArgs[0]);
              } catch (Exception e) {
                throw e;
              } finally {
                trans.getTraceManager().fireExtensionEndEvent(m, null, 
                        convertedArgs[0]);
              }
              return result;
            } else                  
              return m.invoke(null, convertedArgs[0]);
          }
          else
          {
            if (null == m_defaultInstance)
            {
              if (trans != null && trans.getDebug()) {
                trans.getTraceManager().fireExtensionEvent(new 
                        ExtensionEvent(trans, m_classObj));
                try {
                    m_defaultInstance = m_classObj.newInstance();
                } catch (Exception e) {
                    throw e;
                } finally {
                    trans.getTraceManager().fireExtensionEndEvent(new 
                        ExtensionEvent(trans, m_classObj));
                }
              }    else
                  m_defaultInstance = m_classObj.newInstance();
            }
            if (trans != null && trans.getDebug()) {
              trans.getTraceManager().fireExtensionEvent(m, m_defaultInstance, 
                    convertedArgs[0]);
              Object result;
              try {
                result = m.invoke(m_defaultInstance, convertedArgs[0]);
              } catch (Exception e) {
                throw e;
              } finally {
                trans.getTraceManager().fireExtensionEndEvent(m, 
                        m_defaultInstance, convertedArgs[0]);
              }
              return result;
            } else                  
              return m.invoke(m_defaultInstance, convertedArgs[0]);
          }  
        }

      }
    }
    catch (InvocationTargetException ite)
    {
      Throwable resultException = ite;
      Throwable targetException = ite.getTargetException();
 
      if (targetException instanceof TransformerException)
        throw ((TransformerException)targetException);
      else if (targetException != null)
        resultException = targetException;
            
      throw new TransformerException(resultException);
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      throw new TransformerException(e);
    }
  }

  /**
   * Process a call to an XPath extension function
   *
   * @param extFunction The XPath extension function
   * @param args The arguments of the function call.
   * @param exprContext The context in which this expression is being executed.
   * @return the return value of the function evaluation.
   * @throws TransformerException
   */
  public Object callFunction(FuncExtFunction extFunction,
                             Vector args,
                             ExpressionContext exprContext)
      throws TransformerException
  {
    return callFunction(extFunction.getFunctionName(), args, 
                        extFunction.getMethodKey(), exprContext);
  }

  /**
   * Process a call to this extension namespace via an element. As a side
   * effect, the results are sent to the TransformerImpl's result tree. 
   * We invoke the static or instance method in the class represented by
   * by the namespace URI.  If we don't already have an instance of this class,
   * we create one upon the first call.
   *
   * @param localPart      Element name's local part.
   * @param element        The extension element being processed.
   * @param transformer      Handle to TransformerImpl.
   * @param stylesheetTree The compiled stylesheet tree.
   * @param methodKey      A key that uniquely identifies this element call.
   * @throws IOException           if loading trouble
   * @throws TransformerException          if parsing trouble
   */

  public void processElement(String localPart,
                             ElemTemplateElement element,
                             TransformerImpl transformer,
                             Stylesheet stylesheetTree,
                             Object methodKey)
    throws TransformerException, IOException
  {
    Object result = null;

    Method m = (Method) getFromCache(methodKey, null, null);
    if (null == m)
    {
      try
      {
        m = MethodResolver.getElementMethod(m_classObj, localPart);
        if ( (null == m_defaultInstance) && 
                !Modifier.isStatic(m.getModifiers()) ) {
          if (transformer.getDebug()) {            
            transformer.getTraceManager().fireExtensionEvent(
                    new ExtensionEvent(transformer, m_classObj));
            try {
              m_defaultInstance = m_classObj.newInstance();
            } catch (Exception e) {
              throw e;
            } finally {
              transformer.getTraceManager().fireExtensionEndEvent(
                    new ExtensionEvent(transformer, m_classObj));
            }
          } else 
            m_defaultInstance = m_classObj.newInstance();
        }
      }
      catch (Exception e)
      {
        // e.printStackTrace ();
        throw new TransformerException (e.getMessage (), e);
      }
      putToCache(methodKey, null, null, m);
    }

    XSLProcessorContext xpc = new XSLProcessorContext(transformer, 
                                                      stylesheetTree);

    try
    {
      if (transformer.getDebug()) {
        transformer.getTraceManager().fireExtensionEvent(m, m_defaultInstance, 
                new Object[] {xpc, element});
        try {
          result = m.invoke(m_defaultInstance, new Object[] {xpc, element});
        } catch (Exception e) {
          throw e;
        } finally {
          transformer.getTraceManager().fireExtensionEndEvent(m, 
                m_defaultInstance, new Object[] {xpc, element});
        }
      } else                  
        result = m.invoke(m_defaultInstance, new Object[] {xpc, element});
    }
    catch (InvocationTargetException e)
    {
      Throwable targetException = e.getTargetException();
      
      if (targetException instanceof TransformerException)
        throw (TransformerException)targetException;
      else if (targetException != null)
        throw new TransformerException (targetException.getMessage (), 
                targetException);
      else
        throw new TransformerException (e.getMessage (), e);
    }
    catch (Exception e)
    {
      // e.printStackTrace ();
      throw new TransformerException (e.getMessage (), e);
    }

    if (result != null)
    {
      xpc.outputToResultTree (stylesheetTree, result);
    }
 
  }
 
}
