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
 * $Id: Redirect.java 468639 2006-10-28 06:52:33Z minchau $
 */
package org.apache.xalan.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.templates.OutputProperties;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.XPath;
import org.apache.xpath.objects.XObject;
import org.apache.xml.serializer.SerializationHandler;
import org.xml.sax.ContentHandler;

/**
 * Implements three extension elements to allow an XSLT transformation to
 * redirect its output to multiple output files.
 *
 * It is accessed by specifying a namespace URI as follows:
 * <pre>
 *    xmlns:redirect="http://xml.apache.org/xalan/redirect"
 * </pre>
 *
 * <p>You can either just use redirect:write, in which case the file will be
 * opened and immediately closed after the write, or you can bracket the
 * write calls by redirect:open and redirect:close, in which case the
 * file will be kept open for multiple writes until the close call is
 * encountered.  Calls can be nested.  
 *
 * <p>Calls can take a 'file' attribute
 * and/or a 'select' attribute in order to get the filename.  If a select
 * attribute is encountered, it will evaluate that expression for a string
 * that indicates the filename.  If the string evaluates to empty, it will
 * attempt to use the 'file' attribute as a default.  Filenames can be relative
 * or absolute.  If they are relative, the base directory will be the same as
 * the base directory for the output document.  This is obtained by calling
 * getOutputTarget() on the TransformerImpl.  You can set this base directory
 * by calling TransformerImpl.setOutputTarget() or it is automatically set
 * when using the two argument form of transform() or transformNode().
 *
 * <p>Calls to redirect:write and redirect:open also take an optional 
 * attribute append="true|yes", which will attempt to simply append 
 * to an existing file instead of always opening a new file.  The 
 * default behavior of always overwriting the file still happens 
 * if you do not specify append.
 * <p><b>Note:</b> this may give unexpected results when using xml 
 * or html output methods, since this is <b>not</b> coordinated 
 * with the serializers - hence, you may get extra xml decls in 
 * the middle of your file after appending to it.
 *
 * <p>Example:</p>
 * <PRE>
 * &lt;?xml version="1.0"?>
 * &lt;xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
 *                 version="1.0"
 *                 xmlns:redirect="http://xml.apache.org/xalan/redirect"
 *                 extension-element-prefixes="redirect">
 *
 *   &lt;xsl:template match="/">
 *     &lt;out>
 *       default output.
 *     &lt;/out>
 *     &lt;redirect:open file="doc3.out"/>
 *     &lt;redirect:write file="doc3.out">
 *       &lt;out>
 *         &lt;redirect:write file="doc1.out">
 *           &lt;out>
 *             doc1 output.
 *             &lt;redirect:write file="doc3.out">
 *               Some text to doc3
 *             &lt;/redirect:write>
 *           &lt;/out>
 *         &lt;/redirect:write>
 *         &lt;redirect:write file="doc2.out">
 *           &lt;out>
 *             doc2 output.
 *             &lt;redirect:write file="doc3.out">
 *               Some more text to doc3
 *               &lt;redirect:write select="doc/foo">
 *                 text for doc4
 *               &lt;/redirect:write>
 *             &lt;/redirect:write>
 *           &lt;/out>
 *         &lt;/redirect:write>
 *       &lt;/out>
 *     &lt;/redirect:write>
 *     &lt;redirect:close file="doc3.out"/>
 *   &lt;/xsl:template>
 *
 * &lt;/xsl:stylesheet>
 * </PRE>
 *
 * @author Scott Boag
 * @version 1.0
 * @see <a href="../../../../../../extensions.html#ex-redirect" target="_top">Example with Redirect extension</a>
 */
public class Redirect
{
  /**
   * List of formatter listeners indexed by filename.
   */
  protected Hashtable m_formatterListeners = new Hashtable ();

  /**
   * List of output streams indexed by filename.
   */
  protected Hashtable m_outputStreams = new Hashtable ();

  /** 
   * Default append mode for bare open calls.  
   * False for backwards compatibility (I think). 
   */
  public static final boolean DEFAULT_APPEND_OPEN = false;

  /** 
   * Default append mode for bare write calls.  
   * False for backwards compatibility. 
   */
  public static final boolean DEFAULT_APPEND_WRITE = false;

  /**
   * Open the given file and put it in the XML, HTML, or Text formatter listener's table.
   */
  public void open(XSLProcessorContext context, ElemExtensionCall elem)
    throws java.net.MalformedURLException,
           java.io.FileNotFoundException,
           java.io.IOException,
           javax.xml.transform.TransformerException
  {
    String fileName = getFilename(context, elem);
    Object flistener = m_formatterListeners.get(fileName);
    if(null == flistener)
    {
      String mkdirsExpr 
        = elem.getAttribute ("mkdirs", context.getContextNode(), 
                                                  context.getTransformer());
      boolean mkdirs = (mkdirsExpr != null)
                       ? (mkdirsExpr.equals("true") || mkdirsExpr.equals("yes")) : true;

      // Whether to append to existing files or not, <jpvdm@iafrica.com>
      String appendExpr = elem.getAttribute("append", context.getContextNode(), context.getTransformer());
	  boolean append = (appendExpr != null)
                       ? (appendExpr.equals("true") || appendExpr.equals("yes")) : DEFAULT_APPEND_OPEN;

      Object ignored = makeFormatterListener(context, elem, fileName, true, mkdirs, append);
    }
  }
  
  /**
   * Write the evalutation of the element children to the given file. Then close the file
   * unless it was opened with the open extension element and is in the formatter listener's table.
   */
  public void write(XSLProcessorContext context, ElemExtensionCall elem)
    throws java.net.MalformedURLException,
           java.io.FileNotFoundException,
           java.io.IOException,
           javax.xml.transform.TransformerException
  {
    String fileName = getFilename(context, elem);
    Object flObject = m_formatterListeners.get(fileName);
    ContentHandler formatter;
    boolean inTable = false;
    if(null == flObject)
    {
      String mkdirsExpr 
        = ((ElemExtensionCall)elem).getAttribute ("mkdirs", 
                                                  context.getContextNode(), 
                                                  context.getTransformer());
      boolean mkdirs = (mkdirsExpr != null)
                       ? (mkdirsExpr.equals("true") || mkdirsExpr.equals("yes")) : true;

      // Whether to append to existing files or not, <jpvdm@iafrica.com>
      String appendExpr = elem.getAttribute("append", context.getContextNode(), context.getTransformer());
	  boolean append = (appendExpr != null)
                       ? (appendExpr.equals("true") || appendExpr.equals("yes")) : DEFAULT_APPEND_WRITE;

      formatter = makeFormatterListener(context, elem, fileName, true, mkdirs, append);
    }
    else
    {
      inTable = true;
      formatter = (ContentHandler)flObject;
    }
    
    TransformerImpl transf = context.getTransformer();
    
    startRedirection(transf, formatter);  // for tracing only
    
    transf.executeChildTemplates(elem,
                                 context.getContextNode(),
                                 context.getMode(), formatter);
                                 
    endRedirection(transf); // for tracing only
    
    if(!inTable)
    {
      OutputStream ostream = (OutputStream)m_outputStreams.get(fileName);
      if(null != ostream)
      {
        try
        {
          formatter.endDocument();
        }
        catch(org.xml.sax.SAXException se)
        {
          throw new TransformerException(se);
        }
        ostream.close();
        m_outputStreams.remove(fileName);
        m_formatterListeners.remove(fileName);
      }
    }
  }


  /**
   * Close the given file and remove it from the formatter listener's table.
   */
  public void close(XSLProcessorContext context, ElemExtensionCall elem)
    throws java.net.MalformedURLException,
    java.io.FileNotFoundException,
    java.io.IOException,
    javax.xml.transform.TransformerException
  {
    String fileName = getFilename(context, elem);
    Object formatterObj = m_formatterListeners.get(fileName);
    if(null != formatterObj)
    {
      ContentHandler fl = (ContentHandler)formatterObj;
      try
      {
        fl.endDocument();
      }
      catch(org.xml.sax.SAXException se)
      {
        throw new TransformerException(se);
      }
      OutputStream ostream = (OutputStream)m_outputStreams.get(fileName);
      if(null != ostream)
      {
        ostream.close();
        m_outputStreams.remove(fileName);
      }
      m_formatterListeners.remove(fileName);
    }
  }

  /**
   * Get the filename from the 'select' or the 'file' attribute.
   */
  private String getFilename(XSLProcessorContext context, ElemExtensionCall elem)
    throws java.net.MalformedURLException,
    java.io.FileNotFoundException,
    java.io.IOException,
    javax.xml.transform.TransformerException
  {
    String fileName;
    String fileNameExpr 
      = ((ElemExtensionCall)elem).getAttribute ("select", 
                                                context.getContextNode(), 
                                                context.getTransformer());
    if(null != fileNameExpr)
    {
      org.apache.xpath.XPathContext xctxt 
        = context.getTransformer().getXPathContext();
      XPath myxpath = new XPath(fileNameExpr, elem, xctxt.getNamespaceContext(), XPath.SELECT);
      XObject xobj = myxpath.execute(xctxt, context.getContextNode(), elem);
      fileName = xobj.str();
      if((null == fileName) || (fileName.length() == 0))
      {
        fileName = elem.getAttribute ("file", 
                                      context.getContextNode(), 
                                      context.getTransformer());
      }
    }
    else
    {
      fileName = elem.getAttribute ("file", context.getContextNode(), 
                                                               context.getTransformer());
    }
    if(null == fileName)
    {
      context.getTransformer().getMsgMgr().error(elem, elem, 
                                     context.getContextNode(), 
                                     XSLTErrorResources.ER_REDIRECT_COULDNT_GET_FILENAME);
                              //"Redirect extension: Could not get filename - file or select attribute must return vald string.");
    }
    return fileName;
  }
  
  // yuck.
  // Note: this is not the best way to do this, and may not even 
  //    be fully correct! Patches (with test cases) welcomed. -sc
  private String urlToFileName(String base)
  {
    if(null != base)
    {
      if(base.startsWith("file:////"))
      {
        base = base.substring(7);
      }
      else if(base.startsWith("file:///"))
      {
        base = base.substring(6);
      }
      else if(base.startsWith("file://"))
      {
        base = base.substring(5); // absolute?
      }
      else if(base.startsWith("file:/"))
      {
        base = base.substring(5);
      }
      else if(base.startsWith("file:"))
      {
        base = base.substring(4);
      }
    }
    return base;
  }

  /**
   * Create a new ContentHandler, based on attributes of the current ContentHandler.
   */
  private ContentHandler makeFormatterListener(XSLProcessorContext context,
                                               ElemExtensionCall elem,
                                               String fileName,
                                               boolean shouldPutInTable,
                                               boolean mkdirs, 
                                               boolean append)
    throws java.net.MalformedURLException,
    java.io.FileNotFoundException,
    java.io.IOException,
    javax.xml.transform.TransformerException
  {
    File file = new File(fileName);
    TransformerImpl transformer = context.getTransformer();
    String base;          // Base URI to use for relative paths

    if(!file.isAbsolute())
    {
      // This code is attributed to Jon Grov <jon@linpro.no>.  A relative file name
      // is relative to the Result used to kick off the transform.  If no such
      // Result was supplied, the filename is relative to the source document.
      // When transforming with a SAXResult or DOMResult, call
      // TransformerImpl.setOutputTarget() to set the desired Result base.
  //      String base = urlToFileName(elem.getStylesheet().getSystemId());

      Result outputTarget = transformer.getOutputTarget();
      if ( (null != outputTarget) && ((base = outputTarget.getSystemId()) != null) ) {
        base = urlToFileName(base);
      }
      else
      {
        base = urlToFileName(transformer.getBaseURLOfSource());
      }

      if(null != base)
      {
        File baseFile = new File(base);
        file = new File(baseFile.getParent(), fileName);
      }
      // System.out.println("file is: "+file.toString());
    }

    if(mkdirs)
    {
      String dirStr = file.getParent();
      if((null != dirStr) && (dirStr.length() > 0))
      {
        File dir = new File(dirStr);
        dir.mkdirs();
      }
    }

    // This should be worked on so that the output format can be 
    // defined by a first child of the redirect element.
    OutputProperties format = transformer.getOutputFormat();

    // FileOutputStream ostream = new FileOutputStream(file);
    // Patch from above line to below by <jpvdm@iafrica.com>
    //  Note that in JDK 1.2.2 at least, FileOutputStream(File)
    //  is implemented as a call to 
    //  FileOutputStream(File.getPath, append), thus this should be 
    //  the equivalent instead of getAbsolutePath()
    FileOutputStream ostream = new FileOutputStream(file.getPath(), append);
    
    try
    {
      SerializationHandler flistener = 
        createSerializationHandler(transformer, ostream, file, format);
        
      try
      {
        flistener.startDocument();
      }
      catch(org.xml.sax.SAXException se)
      {
        throw new TransformerException(se);
      }
      if(shouldPutInTable)
      {
        m_outputStreams.put(fileName, ostream);
        m_formatterListeners.put(fileName, flistener);
      }
      return flistener;
    }
    catch(TransformerException te)
    {
      throw new javax.xml.transform.TransformerException(te);
    }
    
  }

  /**
   * A class that extends this class can over-ride this public method and recieve
   * a callback that redirection is about to start
   * @param transf The transformer.
   * @param formatter The handler that receives the redirected output
   */
  public void startRedirection(TransformerImpl transf, ContentHandler formatter)
  {
      // A class that extends this class could provide a method body        
  }
    
  /**
   * A class that extends this class can over-ride this public method and receive
   * a callback that redirection to the ContentHandler specified in the startRedirection()
   * call has ended
   * @param transf The transformer.
   */
  public void endRedirection(TransformerImpl transf)
  {
      // A class that extends this class could provide a method body        
  }
    
  /**
   * A class that extends this one could over-ride this public method and receive
   * a callback for the creation of the serializer used in the redirection.
   * @param transformer The transformer
   * @param ostream The output stream that the serializer wraps
   * @param file The file associated with the ostream
   * @param format The format parameter used to create the serializer
   * @return the serializer that the redirection will go to.
   * 
   * @throws java.io.IOException
   * @throws TransformerException
   */
  public SerializationHandler createSerializationHandler(
        TransformerImpl transformer,
        FileOutputStream ostream,
        File file,
        OutputProperties format) 
        throws java.io.IOException, TransformerException
  {

      SerializationHandler serializer =
          transformer.createSerializationHandler(
              new StreamResult(ostream),
              format);
      return serializer;
  }
}
