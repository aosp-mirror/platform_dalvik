/*
 * Copyright (C) 2009 The Android Open Source Project
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

import java.io.ByteArrayInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.Test;
import junit.framework.TestSuite;

public class DocumentBuilderTest extends junit.framework.TestCase {
    private static String parse(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setCoalescing(true);
        dbf.setExpandEntityReferences(true);
        
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        DocumentBuilder builder = dbf.newDocumentBuilder();
        
        Document doc = builder.parse(stream);
        
        Node titleNode = doc.getFirstChild();
        NodeList children = titleNode.getChildNodes();
        assertEquals(1, children.getLength());
        return children.item(0).getNodeValue();
    }
    
    // http://code.google.com/p/android/issues/detail?id=2607
    public void test_characterReferences() throws Exception {
        assertEquals("aAb", parse("<p>a&#65;b</p>"));
        assertEquals("aAb", parse("<p>a&#x41;b</p>"));
    }

    // http://code.google.com/p/android/issues/detail?id=2607
    public void test_predefinedEntities() throws Exception {
        assertEquals("a<b", parse("<p>a&lt;b</p>"));
        assertEquals("a>b", parse("<p>a&gt;b</p>"));
        assertEquals("a&b", parse("<p>a&amp;b</p>"));
        assertEquals("a'b", parse("<p>a&apos;b</p>"));
        assertEquals("a\"b", parse("<p>a&quot;b</p>"));
    }
}
