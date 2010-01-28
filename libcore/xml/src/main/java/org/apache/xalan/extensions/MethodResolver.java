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
 * $Id: MethodResolver.java 468637 2006-10-28 06:51:02Z minchau $
 */
package org.apache.xalan.extensions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.ref.DTMNodeIterator;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XRTreeFrag;
import org.apache.xpath.objects.XString;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

/**
 * Utility class to help resolve method overloading with Xalan XSLT 
 * argument types.
 */
public class MethodResolver
{

  /**
   * Specifies a search for static methods only.
   */
  public static final int STATIC_ONLY         = 1;

  /**
   * Specifies a search for instance methods only.
   */
  public static final int INSTANCE_ONLY       = 2;

  /**
   * Specifies a search for both static and instance methods.
   */
  public static final int STATIC_AND_INSTANCE = 3;

  /**
   * Specifies a Dynamic method search.  If the method being
   * evaluated is a static method, all arguments are used.
   * Otherwise, it is an instance method and only arguments
   * beginning with the second argument are used.
   */
  public static final int DYNAMIC             = 4;

  /**
   * Given a class, figure out the resolution of 
   * the Java Constructor from the XSLT argument types, and perform the 
   * conversion of the arguments.
   * @param classObj the Class of the object to be constructed.
   * @param argsIn An array of XSLT/XPath arguments.
   * @param argsOut An array of the exact size as argsIn, which will be 
   * populated with converted arguments if a suitable method is found.
   * @return A constructor that will work with the argsOut array.
   * @throws TransformerException may be thrown for Xalan conversion
   * exceptions.
   */
  public static Constructor getConstructor(Class classObj, 
                                           Object[] argsIn, 
                                           Object[][] argsOut,
                                           ExpressionContext exprContext)
    throws NoSuchMethodException,
           SecurityException,
           TransformerException
  {
    Constructor bestConstructor = null;
    Class[] bestParamTypes = null;
    Constructor[] constructors = classObj.getConstructors();
    int nMethods = constructors.length;
    int bestScore = Integer.MAX_VALUE;
    int bestScoreCount = 0;
    for(int i = 0; i < nMethods; i++)
    {
      Constructor ctor = constructors[i];
      Class[] paramTypes = ctor.getParameterTypes();
      int numberMethodParams = paramTypes.length;
      int paramStart = 0;
      boolean isFirstExpressionContext = false;
      int scoreStart;
      // System.out.println("numberMethodParams: "+numberMethodParams);
      // System.out.println("argsIn.length: "+argsIn.length);
      // System.out.println("exprContext: "+exprContext);
      if(numberMethodParams == (argsIn.length+1))
      {
        Class javaClass = paramTypes[0];
        // System.out.println("first javaClass: "+javaClass.getName());
        if(ExpressionContext.class.isAssignableFrom(javaClass))
        {
          isFirstExpressionContext = true;
          scoreStart = 0;
          paramStart++;
          // System.out.println("Incrementing paramStart: "+paramStart);
        }
        else
          continue;
      }
      else
          scoreStart = 1000;
      
      if(argsIn.length == (numberMethodParams - paramStart))
      {
        // then we have our candidate.
        int score = scoreMatch(paramTypes, paramStart, argsIn, scoreStart);
        // System.out.println("score: "+score);
        if(-1 == score)	
          continue;
        if(score < bestScore)
        {
          // System.out.println("Assigning best ctor: "+ctor);
          bestConstructor = ctor;
          bestParamTypes = paramTypes;
          bestScore = score;
          bestScoreCount = 1;
        }
        else if (score == bestScore)
          bestScoreCount++;
      }
    }

    if(null == bestConstructor)
    {
      throw new NoSuchMethodException(errString("function", "constructor", classObj,
                                                                        "", 0, argsIn));
    }
    /*** This is commented out until we can do a better object -> object scoring 
    else if (bestScoreCount > 1)
      throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_MORE_MATCH_CONSTRUCTOR, new Object[]{classObj.getName()})); //"More than one best match for constructor for "
                                                                   + classObj.getName());
    ***/
    else
      convertParams(argsIn, argsOut, bestParamTypes, exprContext);
    
    return bestConstructor;
  }

  
  /**
   * Given the name of a method, figure out the resolution of 
   * the Java Method from the XSLT argument types, and perform the 
   * conversion of the arguments.
   * @param classObj The Class of the object that should have the method.
   * @param name The name of the method to be invoked.
   * @param argsIn An array of XSLT/XPath arguments.
   * @param argsOut An array of the exact size as argsIn, which will be 
   * populated with converted arguments if a suitable method is found.
   * @return A method that will work with the argsOut array.
   * @throws TransformerException may be thrown for Xalan conversion
   * exceptions.
   */
  public static Method getMethod(Class classObj,
                                 String name, 
                                 Object[] argsIn, 
                                 Object[][] argsOut,
                                 ExpressionContext exprContext,
                                 int searchMethod)
    throws NoSuchMethodException,
           SecurityException,
           TransformerException
  {
    // System.out.println("---> Looking for method: "+name);
    // System.out.println("---> classObj: "+classObj);
    if (name.indexOf("-")>0)
      name = replaceDash(name);
    Method bestMethod = null;
    Class[] bestParamTypes = null;
    Method[] methods = classObj.getMethods();
    int nMethods = methods.length;
    int bestScore = Integer.MAX_VALUE;
    int bestScoreCount = 0;
    boolean isStatic;
    for(int i = 0; i < nMethods; i++)
    {
      Method method = methods[i];
      // System.out.println("looking at method: "+method);
      int xsltParamStart = 0;
      if(method.getName().equals(name))
      {
        isStatic = Modifier.isStatic(method.getModifiers());
        switch(searchMethod)
        {
          case STATIC_ONLY:
            if (!isStatic)
            {
              continue;
            }
            break;

          case INSTANCE_ONLY:
            if (isStatic)
            {
              continue;
            }
            break;

          case STATIC_AND_INSTANCE:
            break;

          case DYNAMIC:
            if (!isStatic)
              xsltParamStart = 1;
        }
        int javaParamStart = 0;
        Class[] paramTypes = method.getParameterTypes();
        int numberMethodParams = paramTypes.length;
        boolean isFirstExpressionContext = false;
        int scoreStart;
        // System.out.println("numberMethodParams: "+numberMethodParams);
        // System.out.println("argsIn.length: "+argsIn.length);
        // System.out.println("exprContext: "+exprContext);
        int argsLen = (null != argsIn) ? argsIn.length : 0;
        if(numberMethodParams == (argsLen-xsltParamStart+1))
        {
          Class javaClass = paramTypes[0];
          if(ExpressionContext.class.isAssignableFrom(javaClass))
          {
            isFirstExpressionContext = true;
            scoreStart = 0;
            javaParamStart++;
          }
          else
          {
            continue;
          }
        }
        else
            scoreStart = 1000;
        
        if((argsLen - xsltParamStart) == (numberMethodParams - javaParamStart))
        {
          // then we have our candidate.
          int score = scoreMatch(paramTypes, javaParamStart, argsIn, scoreStart);
          // System.out.println("score: "+score);
          if(-1 == score)
            continue;
          if(score < bestScore)
          {
            // System.out.println("Assigning best method: "+method);
            bestMethod = method;
            bestParamTypes = paramTypes;
            bestScore = score;
            bestScoreCount = 1;
          }
          else if (score == bestScore)
            bestScoreCount++;
        }
      }
    }
    
    if (null == bestMethod)
    {
      throw new NoSuchMethodException(errString("function", "method", classObj,
                                                                name, searchMethod, argsIn));
    }
    /*** This is commented out until we can do a better object -> object scoring 
    else if (bestScoreCount > 1)
      throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_MORE_MATCH_METHOD, new Object[]{name})); //"More than one best match for method " + name);
    ***/
    else
      convertParams(argsIn, argsOut, bestParamTypes, exprContext);
    
    return bestMethod;
  }
  
  /**
   * To support EXSLT extensions, convert names with dash to allowable Java names: 
   * e.g., convert abc-xyz to abcXyz.
   * Note: dashes only appear in middle of an EXSLT function or element name.
   */
  private static String replaceDash(String name)
  {
    char dash = '-';
    StringBuffer buff = new StringBuffer("");
    for (int i=0; i<name.length(); i++)
    {
      if (name.charAt(i) == dash)
      {}
      else if (i > 0 && name.charAt(i-1) == dash)
        buff.append(Character.toUpperCase(name.charAt(i)));
      else
        buff.append(name.charAt(i));
    }
    return buff.toString();
  }
  
  /**
   * Given the name of a method, figure out the resolution of 
   * the Java Method
   * @param classObj The Class of the object that should have the method.
   * @param name The name of the method to be invoked.
   * @return A method that will work to be called as an element.
   * @throws TransformerException may be thrown for Xalan conversion
   * exceptions.
   */
  public static Method getElementMethod(Class classObj,
                                        String name)
    throws NoSuchMethodException,
           SecurityException,
           TransformerException
  {
    // System.out.println("---> Looking for element method: "+name);
    // System.out.println("---> classObj: "+classObj);
    Method bestMethod = null;
    Method[] methods = classObj.getMethods();
    int nMethods = methods.length;
    int bestScoreCount = 0;
    for(int i = 0; i < nMethods; i++)
    {
      Method method = methods[i];
      // System.out.println("looking at method: "+method);
      if(method.getName().equals(name))
      {
        Class[] paramTypes = method.getParameterTypes();
        if ( (paramTypes.length == 2)
           && paramTypes[1].isAssignableFrom(org.apache.xalan.templates.ElemExtensionCall.class)
                                         && paramTypes[0].isAssignableFrom(org.apache.xalan.extensions.XSLProcessorContext.class) )
        {
          if ( ++bestScoreCount == 1 )
            bestMethod = method;
          else
            break;
        }
      }
    }
    
    if (null == bestMethod)
    {
      throw new NoSuchMethodException(errString("element", "method", classObj,
                                                                        name, 0, null));
    }
    else if (bestScoreCount > 1)
      throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_MORE_MATCH_ELEMENT, new Object[]{name})); //"More than one best match for element method " + name);
    
    return bestMethod;
  }
  

  /**
   * Convert a set of parameters based on a set of paramTypes.
   * @param argsIn An array of XSLT/XPath arguments.
   * @param argsOut An array of the exact size as argsIn, which will be 
   * populated with converted arguments.
   * @param paramTypes An array of class objects, of the exact same 
   * size as argsIn and argsOut.
   * @throws TransformerException may be thrown for Xalan conversion
   * exceptions.
   */
  public static void convertParams(Object[] argsIn, 
                                   Object[][] argsOut, Class[] paramTypes,
                                   ExpressionContext exprContext)
    throws javax.xml.transform.TransformerException
  {
    // System.out.println("In convertParams");
    if (paramTypes == null)
      argsOut[0] = null;
    else
    {
      int nParams = paramTypes.length;
      argsOut[0] = new Object[nParams];
      int paramIndex = 0;
      if((nParams > 0) 
         && ExpressionContext.class.isAssignableFrom(paramTypes[0]))
      {
        argsOut[0][0] = exprContext;
        // System.out.println("Incrementing paramIndex in convertParams: "+paramIndex);
        paramIndex++;
      }

      if (argsIn != null)
      {
        for(int i = argsIn.length - nParams + paramIndex ; paramIndex < nParams; i++, paramIndex++)
        {
          // System.out.println("paramTypes[i]: "+paramTypes[i]);
          argsOut[0][paramIndex] = convert(argsIn[i], paramTypes[paramIndex]);
        }
      }
    }
  }
  
  /**
   * Simple class to hold information about allowed conversions 
   * and their relative scores, for use by the table below.
   */
  static class ConversionInfo
  {
    ConversionInfo(Class cl, int score)
    {
      m_class = cl;
      m_score = score;
    }
    
    Class m_class;  // Java class to convert to.
    int m_score; // Match score, closer to zero is more matched.
  }
  
  private static final int SCOREBASE=1;
  
  /**
   * Specification of conversions from XSLT type CLASS_UNKNOWN
   * (i.e. some unknown Java object) to allowed Java types.
   */
  private final static ConversionInfo[] m_javaObjConversions = {
    new ConversionInfo(Double.TYPE, 11),
    new ConversionInfo(Float.TYPE, 12),
    new ConversionInfo(Long.TYPE, 13),
    new ConversionInfo(Integer.TYPE, 14),
    new ConversionInfo(Short.TYPE, 15),
    new ConversionInfo(Character.TYPE, 16),
    new ConversionInfo(Byte.TYPE, 17),
    new ConversionInfo(java.lang.String.class, 18)
  };
  
  /**
   * Specification of conversions from XSLT type CLASS_BOOLEAN
   * to allowed Java types.
   */
  private final static ConversionInfo[] m_booleanConversions = {
    new ConversionInfo(Boolean.TYPE, 0),
    new ConversionInfo(java.lang.Boolean.class, 1),
    new ConversionInfo(java.lang.Object.class, 2),
    new ConversionInfo(java.lang.String.class, 3)
  };

  /**
   * Specification of conversions from XSLT type CLASS_NUMBER
   * to allowed Java types.
   */
  private final static ConversionInfo[] m_numberConversions = {
    new ConversionInfo(Double.TYPE, 0),
    new ConversionInfo(java.lang.Double.class, 1),
    new ConversionInfo(Float.TYPE, 3),
    new ConversionInfo(Long.TYPE, 4),
    new ConversionInfo(Integer.TYPE, 5),
    new ConversionInfo(Short.TYPE, 6),
    new ConversionInfo(Character.TYPE, 7),
    new ConversionInfo(Byte.TYPE, 8),
    new ConversionInfo(Boolean.TYPE, 9),
    new ConversionInfo(java.lang.String.class, 10),
    new ConversionInfo(java.lang.Object.class, 11)
  };

  /**
   * Specification of conversions from XSLT type CLASS_STRING
   * to allowed Java types.
   */
  private final static ConversionInfo[] m_stringConversions = {
    new ConversionInfo(java.lang.String.class, 0),
    new ConversionInfo(java.lang.Object.class, 1),
    new ConversionInfo(Character.TYPE, 2),
    new ConversionInfo(Double.TYPE, 3),
    new ConversionInfo(Float.TYPE, 3),
    new ConversionInfo(Long.TYPE, 3),
    new ConversionInfo(Integer.TYPE, 3),
    new ConversionInfo(Short.TYPE, 3),
    new ConversionInfo(Byte.TYPE, 3),
    new ConversionInfo(Boolean.TYPE, 4)
  };

  /**
   * Specification of conversions from XSLT type CLASS_RTREEFRAG
   * to allowed Java types.
   */
  private final static ConversionInfo[] m_rtfConversions = {
    new ConversionInfo(org.w3c.dom.traversal.NodeIterator.class, 0),
    new ConversionInfo(org.w3c.dom.NodeList.class, 1),
    new ConversionInfo(org.w3c.dom.Node.class, 2),
    new ConversionInfo(java.lang.String.class, 3),
    new ConversionInfo(java.lang.Object.class, 5),
    new ConversionInfo(Character.TYPE, 6),
    new ConversionInfo(Double.TYPE, 7),
    new ConversionInfo(Float.TYPE, 7),
    new ConversionInfo(Long.TYPE, 7),
    new ConversionInfo(Integer.TYPE, 7),
    new ConversionInfo(Short.TYPE, 7),
    new ConversionInfo(Byte.TYPE, 7),
    new ConversionInfo(Boolean.TYPE, 8)
  };
  
  /**
   * Specification of conversions from XSLT type CLASS_NODESET
   * to allowed Java types.  (This is the same as for CLASS_RTREEFRAG)
   */
  private final static ConversionInfo[] m_nodesetConversions = {
    new ConversionInfo(org.w3c.dom.traversal.NodeIterator.class, 0),
    new ConversionInfo(org.w3c.dom.NodeList.class, 1),
    new ConversionInfo(org.w3c.dom.Node.class, 2),
    new ConversionInfo(java.lang.String.class, 3),
    new ConversionInfo(java.lang.Object.class, 5),
    new ConversionInfo(Character.TYPE, 6),
    new ConversionInfo(Double.TYPE, 7),
    new ConversionInfo(Float.TYPE, 7),
    new ConversionInfo(Long.TYPE, 7),
    new ConversionInfo(Integer.TYPE, 7),
    new ConversionInfo(Short.TYPE, 7),
    new ConversionInfo(Byte.TYPE, 7),
    new ConversionInfo(Boolean.TYPE, 8)
  };
  
  /**
   * Order is significant in the list below, based on 
   * XObject.CLASS_XXX values.
   */
  private final static ConversionInfo[][] m_conversions = 
  {
    m_javaObjConversions, // CLASS_UNKNOWN = 0;
    m_booleanConversions, // CLASS_BOOLEAN = 1;
    m_numberConversions,  // CLASS_NUMBER = 2;
    m_stringConversions,  // CLASS_STRING = 3;
    m_nodesetConversions, // CLASS_NODESET = 4;
    m_rtfConversions      // CLASS_RTREEFRAG = 5;
  };
  
  /**
   * Score the conversion of a set of XSLT arguments to a 
   * given set of Java parameters.
   * If any invocations of this function for a method with 
   * the same name return the same positive value, then a conflict 
   * has occured, and an error should be signaled.
   * @param javaParamTypes Must be filled with valid class names, and 
   * of the same length as xsltArgs.
   * @param xsltArgs Must be filled with valid object instances, and 
   * of the same length as javeParamTypes.
   * @return -1 for no allowed conversion, or a positive score 
   * that is closer to zero for more preferred, or further from 
   * zero for less preferred.
   */
  public static int scoreMatch(Class[] javaParamTypes, int javaParamsStart,
                               Object[] xsltArgs, int score)
  {
    if ((xsltArgs == null) || (javaParamTypes == null))
      return score;
    int nParams = xsltArgs.length;
    for(int i = nParams - javaParamTypes.length + javaParamsStart, javaParamTypesIndex = javaParamsStart; 
        i < nParams; 
        i++, javaParamTypesIndex++)
    {
      Object xsltObj = xsltArgs[i];
      int xsltClassType = (xsltObj instanceof XObject) 
                          ? ((XObject)xsltObj).getType() 
                            : XObject.CLASS_UNKNOWN;
      Class javaClass = javaParamTypes[javaParamTypesIndex];
      
      // System.out.println("Checking xslt: "+xsltObj.getClass().getName()+
      //                   " against java: "+javaClass.getName());
      
      if(xsltClassType == XObject.CLASS_NULL)
      {
        // In Xalan I have objects of CLASS_NULL, though I'm not 
        // sure they're used any more.  For now, do something funky.
        if(!javaClass.isPrimitive())
        {
          // Then assume that a null can be used, but give it a low score.
          score += 10;
          continue;
        }
        else
          return -1;  // no match.
      }
      
      ConversionInfo[] convInfo = m_conversions[xsltClassType];
      int nConversions = convInfo.length;
      int k;
      for(k = 0; k < nConversions; k++)
      {
        ConversionInfo cinfo = convInfo[k];
        if(javaClass.isAssignableFrom(cinfo.m_class))
        {
          score += cinfo.m_score;
          break; // from k loop
        }
      }

      if (k == nConversions)
      {
        // If we get here, we haven't made a match on this parameter using 
        // the ConversionInfo array.  We now try to handle the object -> object
        // mapping which we can't handle through the array mechanism.  To do this,
        // we must determine the class of the argument passed from the stylesheet.

        // If we were passed a subclass of XObject, representing one of the actual
        // XSLT types, and we are here, we reject this extension method as a candidate
        // because a match should have been made using the ConversionInfo array.  If we 
        // were passed an XObject that encapsulates a non-XSLT type or we
        // were passed a non-XSLT type directly, we continue.

        // The current implementation (contributed by Kelly Campbell <camk@channelpoint.com>)
        // checks to see if we were passed an XObject from the XSLT stylesheet.  If not,
        // we use the class of the object that was passed and make sure that it will
        // map to the class type of the parameter in the extension function.
        // If we were passed an XObject, we attempt to get the class of the actual
        // object encapsulated inside the XObject.  If the encapsulated object is null,
        // we judge this method as a match but give it a low score.  
        // If the encapsulated object is not null, we use its type to determine
        // whether this java method is a valid match for this extension function call.
        // This approach eliminates the NullPointerException in the earlier implementation
        // that resulted from passing an XObject encapsulating the null java object.
                                
        // TODO:  This needs to be improved to assign relative scores to subclasses,
        // etc. 

        if (XObject.CLASS_UNKNOWN == xsltClassType)
        {
          Class realClass = null;

          if (xsltObj instanceof XObject)
          {
            Object realObj = ((XObject) xsltObj).object();
            if (null != realObj)
            {
              realClass = realObj.getClass();
            }
            else
            {
              // do the same as if we were passed XObject.CLASS_NULL
              score += 10;
              continue;
            }
          }
          else
          {
            realClass = xsltObj.getClass();
          }

          if (javaClass.isAssignableFrom(realClass))
          {
            score += 0;         // TODO: To be assigned based on subclass "distance"
          }
          else
            return -1;
        }
        else
          return -1;
      }
    }
    return score;
  }
  
  /**
   * Convert the given XSLT object to an object of 
   * the given class.
   * @param xsltObj The XSLT object that needs conversion.
   * @param javaClass The type of object to convert to.
   * @returns An object suitable for passing to the Method.invoke 
   * function in the args array, which may be null in some cases.
   * @throws TransformerException may be thrown for Xalan conversion
   * exceptions.
   */
  static Object convert(Object xsltObj, Class javaClass)
    throws javax.xml.transform.TransformerException
  {
    if(xsltObj instanceof XObject)
    {
      XObject xobj = ((XObject)xsltObj);
      int xsltClassType = xobj.getType();

      switch(xsltClassType)
      {
      case XObject.CLASS_NULL:
        return null;
        
      case XObject.CLASS_BOOLEAN:
        {
          if(javaClass == java.lang.String.class)
            return xobj.str();
          else
            return new Boolean(xobj.bool());
        }
        // break; Unreachable
      case XObject.CLASS_NUMBER:
        {
          if(javaClass == java.lang.String.class)
            return xobj.str();
          else if(javaClass == Boolean.TYPE)
            return new Boolean(xobj.bool());
          else 
          {
            return convertDoubleToNumber(xobj.num(), javaClass);
          }
        }
        // break; Unreachable
        
      case XObject.CLASS_STRING:
        {
          if((javaClass == java.lang.String.class) ||
             (javaClass == java.lang.Object.class))
            return xobj.str();
          else if(javaClass == Character.TYPE)
          {
            String str = xobj.str();
            if(str.length() > 0)
              return new Character(str.charAt(0));
            else
              return null; // ??
          }
          else if(javaClass == Boolean.TYPE)
            return new Boolean(xobj.bool());
          else 
          {
            return convertDoubleToNumber(xobj.num(), javaClass);
          }
        }
        // break; Unreachable
        
      case XObject.CLASS_RTREEFRAG:
        {
          // GLP:  I don't see the reason for the isAssignableFrom call
          //       instead of an == test as is used everywhere else.
          //       Besides, if the javaClass is a subclass of NodeIterator
          //       the condition will be true and we'll create a NodeIterator
          //       which may not match the javaClass, causing a RuntimeException.
          // if((NodeIterator.class.isAssignableFrom(javaClass)) ||
          if ( (javaClass == NodeIterator.class) ||
               (javaClass == java.lang.Object.class) )
          {
            DTMIterator dtmIter = ((XRTreeFrag) xobj).asNodeIterator();
            return new DTMNodeIterator(dtmIter);
          }
          else if (javaClass == NodeList.class)
          {
            return ((XRTreeFrag) xobj).convertToNodeset();
          }
          // Same comment as above
          // else if(Node.class.isAssignableFrom(javaClass))
          else if(javaClass == Node.class)
          {
            DTMIterator iter = ((XRTreeFrag) xobj).asNodeIterator();
            int rootHandle = iter.nextNode();
            DTM dtm = iter.getDTM(rootHandle);
            return dtm.getNode(dtm.getFirstChild(rootHandle));
          }
          else if(javaClass == java.lang.String.class)
          {
            return xobj.str();
          }
          else if(javaClass == Boolean.TYPE)
          {
            return new Boolean(xobj.bool());
          }
          else if(javaClass.isPrimitive())
          {
            return convertDoubleToNumber(xobj.num(), javaClass);
          }
          else
          {
            DTMIterator iter = ((XRTreeFrag) xobj).asNodeIterator();
            int rootHandle = iter.nextNode();
            DTM dtm = iter.getDTM(rootHandle);
            Node child = dtm.getNode(dtm.getFirstChild(rootHandle));

            if(javaClass.isAssignableFrom(child.getClass()))
              return child;
            else
              return null;
          }
        }
        // break; Unreachable
        
      case XObject.CLASS_NODESET:
        {
          // GLP:  I don't see the reason for the isAssignableFrom call
          //       instead of an == test as is used everywhere else.
          //       Besides, if the javaClass is a subclass of NodeIterator
          //       the condition will be true and we'll create a NodeIterator
          //       which may not match the javaClass, causing a RuntimeException.
          // if((NodeIterator.class.isAssignableFrom(javaClass)) ||
          if ( (javaClass == NodeIterator.class) ||
               (javaClass == java.lang.Object.class) )
          {
            return xobj.nodeset();
          }
          // Same comment as above
          // else if(NodeList.class.isAssignableFrom(javaClass))
          else if(javaClass == NodeList.class)
          {
            return xobj.nodelist();
          }
          // Same comment as above
          // else if(Node.class.isAssignableFrom(javaClass))
          else if(javaClass == Node.class)
          {
            // Xalan ensures that iter() always returns an
            // iterator positioned at the beginning.
            DTMIterator ni = xobj.iter();
            int handle = ni.nextNode();
            if (handle != DTM.NULL)
              return ni.getDTM(handle).getNode(handle); // may be null.
            else
              return null;
          }
          else if(javaClass == java.lang.String.class)
          {
            return xobj.str();
          }
          else if(javaClass == Boolean.TYPE)
          {
            return new Boolean(xobj.bool());
          }
          else if(javaClass.isPrimitive())
          {
            return convertDoubleToNumber(xobj.num(), javaClass);
          }
          else
          {
            DTMIterator iter = xobj.iter();
            int childHandle = iter.nextNode();
            DTM dtm = iter.getDTM(childHandle);
            Node child = dtm.getNode(childHandle);
            if(javaClass.isAssignableFrom(child.getClass()))
              return child;
            else
              return null;
          }
        }
        // break; Unreachable
        
        // No default:, fall-through on purpose
      } // end switch
      xsltObj = xobj.object();
      
    } // end if if(xsltObj instanceof XObject)
    
    // At this point, we have a raw java object, not an XObject.
    if (null != xsltObj)
    {
      if(javaClass == java.lang.String.class)
      {
        return xsltObj.toString();
      }
      else if(javaClass.isPrimitive())
      {
        // Assume a number conversion
        XString xstr = new XString(xsltObj.toString());
        double num = xstr.num();
        return convertDoubleToNumber(num, javaClass);
      }
      else if(javaClass == java.lang.Class.class)
      {
        return xsltObj.getClass();
      }
      else
      {
        // Just pass the object directly, and hope for the best.
        return xsltObj;
      }
                }
    else
    {
      // Just pass the object directly, and hope for the best.
      return xsltObj;
    }
  }
  
  /**
   * Do a standard conversion of a double to the specified type.
   * @param num The number to be converted.
   * @param javaClass The class type to be converted to.
   * @return An object specified by javaClass, or a Double instance.
   */
  static Object convertDoubleToNumber(double num, Class javaClass)
  {
    // In the code below, I don't check for NaN, etc., instead 
    // using the standard Java conversion, as I think we should 
    // specify.  See issue-runtime-errors.
    if((javaClass == Double.TYPE) ||
       (javaClass == java.lang.Double.class))
      return new Double(num);
    else if(javaClass == Float.TYPE)
      return new Float(num);
    else if(javaClass == Long.TYPE)
    {
      // Use standard Java Narrowing Primitive Conversion
      // See http://java.sun.com/docs/books/jls/html/5.doc.html#175672
      return new Long((long)num);
    }
    else if(javaClass == Integer.TYPE)
    {
      // Use standard Java Narrowing Primitive Conversion
      // See http://java.sun.com/docs/books/jls/html/5.doc.html#175672
      return new Integer((int)num);
    }
    else if(javaClass == Short.TYPE)
    {
      // Use standard Java Narrowing Primitive Conversion
      // See http://java.sun.com/docs/books/jls/html/5.doc.html#175672
      return new Short((short)num);
    }
    else if(javaClass == Character.TYPE)
    {
      // Use standard Java Narrowing Primitive Conversion
      // See http://java.sun.com/docs/books/jls/html/5.doc.html#175672
      return new Character((char)num);
    }
    else if(javaClass == Byte.TYPE)
    {
      // Use standard Java Narrowing Primitive Conversion
      // See http://java.sun.com/docs/books/jls/html/5.doc.html#175672
      return new Byte((byte)num);
    }
    else     // Some other type of object
    {
      return new Double(num);
    }
  }


  /**
   * Format the message for the NoSuchMethodException containing 
   * all the information about the method we're looking for.
   */
  private static String errString(String callType,    // "function" or "element"
                                  String searchType,  // "method" or "constructor"
                                  Class classObj,
                                  String funcName,
                                  int searchMethod,
                                  Object[] xsltArgs)
  {
    String resultString = "For extension " + callType
                                              + ", could not find " + searchType + " ";
    switch (searchMethod)
    {
      case STATIC_ONLY:
        return resultString + "static " + classObj.getName() + "." 
                            + funcName + "([ExpressionContext,] " + errArgs(xsltArgs, 0) + ").";

      case INSTANCE_ONLY:
        return resultString + classObj.getName() + "."
                            + funcName + "([ExpressionContext,] " + errArgs(xsltArgs, 0) + ").";

      case STATIC_AND_INSTANCE:
        return resultString + classObj.getName() + "." + funcName + "([ExpressionContext,] " + errArgs(xsltArgs, 0) + ").\n"
                            + "Checked both static and instance methods.";

      case DYNAMIC:
        return resultString + "static " + classObj.getName() + "." + funcName
                            + "([ExpressionContext, ]" + errArgs(xsltArgs, 0) + ") nor\n"
                            + classObj + "." + funcName + "([ExpressionContext,] " + errArgs(xsltArgs, 1) + ").";

      default:
        if (callType.equals("function"))      // must be a constructor
        {
          return resultString + classObj.getName()
                                  + "([ExpressionContext,] " + errArgs(xsltArgs, 0) + ").";
        }
        else                                  // must be an element call
        {
          return resultString + classObj.getName() + "." + funcName
                    + "(org.apache.xalan.extensions.XSLProcessorContext, "
                    + "org.apache.xalan.templates.ElemExtensionCall).";
        }
    }
    
  }


  private static String errArgs(Object[] xsltArgs, int startingArg)
  {
    StringBuffer returnArgs = new StringBuffer();
    for (int i = startingArg; i < xsltArgs.length; i++)
    {
      if (i != startingArg)
        returnArgs.append(", ");
      if (xsltArgs[i] instanceof XObject)
        returnArgs.append(((XObject) xsltArgs[i]).getTypeString());      
      else
        returnArgs.append(xsltArgs[i].getClass().getName());
    }
    return returnArgs.toString();
  }

}
