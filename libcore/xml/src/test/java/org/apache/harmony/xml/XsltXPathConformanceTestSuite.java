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

package org.apache.harmony.xml;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The <a href="http://www.oasis-open.org/committees/tc_home.php?wg_abbrev=xslt">OASIS
 * XSLT conformance test suite</a>, adapted for use by JUnit. To run these tests
 * on a device:
 * <ul>
 *    <li>Obtain the <a href="http://www.oasis-open.org/committees/download.php/12171/XSLT-testsuite-04.ZIP">test
 *        suite zip file from the OASIS project site.</li>
 *    <li>Unzip.
 *    <li>Copy the files to a device: <code>adb shell mkdir /data/oasis ;
 *        adb push ./XSLT-Conformance-TC /data/oasis</code>.
 *    <li>Invoke this class' main method, passing the on-device path to the test
 *        suite's <code>catalog.xml</code> file as an argument.
 * </ul>
 */
public class XsltXPathConformanceTestSuite {

    private static final String defaultCatalogFile
            = "/home/dalvik-prebuild/OASIS/XSLT-Conformance-TC/TESTS/catalog.xml";

    /** Orders element attributes by optional URI and name. */
    private static final Comparator<Attr> orderByName = new Comparator<Attr>() {
        public int compare(Attr a, Attr b) {
            int result = compareNullsFirst(a.getBaseURI(), b.getBaseURI());
            return result == 0 ? result
                    : compareNullsFirst(a.getName(), b.getName());
        }

        <T extends Comparable<T>> int compareNullsFirst(T a, T b) {
            return (a == b) ? 0
                    : (a == null) ? -1
                    : (b == null) ? 1
                    : a.compareTo(b);
        }
    };

    private final DocumentBuilder documentBuilder;
    private final TransformerFactory transformerFactory;
    private final XmlPullParserFactory xmlPullParserFactory;

    public XsltXPathConformanceTestSuite()
            throws ParserConfigurationException, XmlPullParserException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setCoalescing(true);
        documentBuilder = factory.newDocumentBuilder();

        transformerFactory = TransformerFactory.newInstance();
        xmlPullParserFactory = XmlPullParserFactory.newInstance();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: XsltXPathConformanceTestSuite <catalog-xml>");
            System.out.println();
            System.out.println("  catalog-xml: an XML file describing an OASIS test suite");
            System.out.println("               such as: " + defaultCatalogFile);
            return;
        }

        File catalogXml = new File(args[0]);
        TestRunner.run(suite(catalogXml));
    }

    public static Test suite() throws Exception {
        return suite(new File(defaultCatalogFile));
    }

    /**
     * Returns a JUnit test suite for the tests described by the given document.
     */
    public static Test suite(File catalogXml) throws Exception {
        XsltXPathConformanceTestSuite suite = new XsltXPathConformanceTestSuite();

        /*
         * Extract the tests from an XML document with the following structure:
         *
         *  <test-suite>
         *    <test-catalog submitter="Lotus">
         *      <creator>Lotus/IBM</creator>
         *      <major-path>Xalan_Conformance_Tests</major-path>
         *      <date>2001-11-16</date>
         *      <test-case ...> ... </test-case>
         *      <test-case ...> ... </test-case>
         *      <test-case ...> ... </test-case>
         *    </test-catalog>
         *  </test-suite>
         */

        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(catalogXml);
        Element testSuiteElement = document.getDocumentElement();
        TestSuite result = new TestSuite();
        for (Element testCatalog : elementsOf(testSuiteElement.getElementsByTagName("test-catalog"))) {
            Element majorPathElement = (Element) testCatalog.getElementsByTagName("major-path").item(0);
            String majorPath = majorPathElement.getTextContent();
            File base = new File(catalogXml.getParentFile(), majorPath);

            for (Element testCaseElement : elementsOf(testCatalog.getElementsByTagName("test-case"))) {
                result.addTest(suite.create(base, testCaseElement));
            }
        }

        return result;
    }

    /**
     * Returns a JUnit test for the test described by the given element.
     */
    private Test create(File base, Element testCaseElement) {

        /*
         * Extract the XSLT test from a DOM entity with the following structure:
         *
         *   <test-case category="XSLT-Result-Tree" id="attribset_attribset01">
         *       <file-path>attribset</file-path>
         *       <creator>Paul Dick</creator>
         *       <date>2001-11-08</date>
         *       <purpose>Set attribute of a LRE from single attribute set.</purpose>
         *       <spec-citation place="7.1.4" type="section" version="1.0" spec="xslt"/>
         *        <scenario operation="standard">
         *           <input-file role="principal-data">attribset01.xml</input-file>
         *           <input-file role="principal-stylesheet">attribset01.xsl</input-file>
         *           <output-file role="principal" compare="XML">attribset01.out</output-file>
         *       </scenario>
         *   </test-case>
         */

        Element filePathElement = (Element) testCaseElement.getElementsByTagName("file-path").item(0);
        Element purposeElement = (Element) testCaseElement.getElementsByTagName("purpose").item(0);
        Element specCitationElement = (Element) testCaseElement.getElementsByTagName("spec-citation").item(0);
        Element scenarioElement = (Element) testCaseElement.getElementsByTagName("scenario").item(0);

        String category = testCaseElement.getAttribute("category");
        String id = testCaseElement.getAttribute("id");
        String name = category + "." + id;
        String purpose = purposeElement != null ? purposeElement.getTextContent() : "";
        String spec = "place=" + specCitationElement.getAttribute("place")
                + " type" + specCitationElement.getAttribute("type")
                + " version=" + specCitationElement.getAttribute("version")
                + " spec=" + specCitationElement.getAttribute("spec");
        String operation = scenarioElement.getAttribute("operation");

        Element principalDataElement = null;
        Element principalStylesheetElement = null;
        Element principalElement = null;

        for (Element element : elementsOf(scenarioElement.getChildNodes())) {
            String role = element.getAttribute("role");
            if (role.equals("principal-data")) {
                principalDataElement = element;
            } else if (role.equals("principal-stylesheet")) {
                principalStylesheetElement = element;
            } else if (role.equals("principal")) {
                principalElement = element;
            } else if (!role.equals("supplemental-stylesheet")
                    && !role.equals("supplemental-data")) {
                return new MisspecifiedTest("Unexpected element at " + name);
            }
        }

        String testDirectory = filePathElement.getTextContent();
        File inBase = new File(base, testDirectory);
        File outBase = new File(new File(base, "REF_OUT"), testDirectory);

        if (principalDataElement == null || principalStylesheetElement == null) {
            return new MisspecifiedTest("Expected <scenario> to have "
                    + "principal=data and principal-stylesheet elements at " + name);
        }

        try {
            File principalData = findFile(inBase, principalDataElement.getTextContent());
            File principalStylesheet = findFile(inBase, principalStylesheetElement.getTextContent());

            final File principal;
            final String compareAs;
            if (!operation.equals("execution-error")) {
                if (principalElement == null) {
                    return new MisspecifiedTest("Expected <scenario> to have principal element at " + name);
                }

                principal = findFile(outBase, principalElement.getTextContent());
                compareAs = principalElement.getAttribute("compare");
            } else {
                principal = null;
                compareAs = null;
            }

            return new XsltTest(category, id, purpose, spec, principalData,
                    principalStylesheet, principal, operation, compareAs);
        } catch (FileNotFoundException e) {
            return new MisspecifiedTest(e.getMessage() + " at " + name);
        }
    }

    /**
     * Finds the named file in the named directory. This tries extra hard to
     * avoid case-insensitive-naming problems, where the requested file is
     * available in a different casing.
     */
    private File findFile(File directory, String name) throws FileNotFoundException {
        File file = new File(directory, name);
        if (file.exists()) {
            return file;
        }

        for (String child : directory.list()) {
            if (child.equalsIgnoreCase(name)) {
                return new File(directory, child);
            }
        }

        throw new FileNotFoundException("Missing file: " + file);
    }

    /**
     * Placeholder for a test that couldn't be configured to run properly.
     */
    public class MisspecifiedTest extends TestCase {
        private final String message;

        MisspecifiedTest(String message) {
            super("test");
            this.message = message;
        }

        public void test() {
            fail(message);
        }
    }

    /**
     * Processes an input XML file with an input XSLT stylesheet and compares
     * the result to an expected output file.
     */
    public class XsltTest extends TestCase {
        // TODO: include these in toString
        private final String category;
        private final String id;
        private final String purpose;
        private final String spec;

        private final File principalData;
        private final File principalStylesheet;
        private final File principal;

        /** either "standard" or "execution-error" */
        private final String operation;

        /** the syntax to compare the output file using, such as "XML" or "HTML" */
        private final String compareAs;

        XsltTest(String category, String id, String purpose, String spec,
                File principalData, File principalStylesheet, File principal,
                String operation, String compareAs) {
            super("test");
            this.category = category;
            this.id = id;
            this.purpose = purpose;
            this.spec = spec;
            this.principalData = principalData;
            this.principalStylesheet = principalStylesheet;
            this.principal = principal;
            this.operation = operation;
            this.compareAs = compareAs;
        }

        public void test() throws Exception {
            if (purpose != null) {
                System.out.println("Purpose: " + purpose);
            }
            if (spec != null) {
                System.out.println("Spec: " + spec);
            }

            Source xslt = new StreamSource(principalStylesheet);
            Source in = new StreamSource(principalData);

            Transformer transformer;
            try {
                transformer = transformerFactory.newTransformer(xslt);
                assertEquals("Expected transformer creation to fail",
                        "standard", operation);
            } catch (TransformerConfigurationException e) {
                if (operation.equals("execution-error")) {
                    return; // expected, such as in XSLT-Result-Tree.Attributes__78369
                }
                AssertionFailedError failure = new AssertionFailedError();
                failure.initCause(e);
                throw failure;
            }

            Result result;
            if (compareAs.equals("XML")) {
                result = new DOMResult();
            } else {
                // TODO: implement support for comparing HTML etc.
                throw new UnsupportedOperationException("Cannot compare as " + compareAs);
            }

            transformer.transform(in, result);

            if (compareAs.equals("XML")) {
                DOMResult domResult = (DOMResult) result;
                assertNodesAreEquivalent(principal, domResult.getNode());
            }
        }

        @Override public String getName() {
            return category + "." + id;
        }
    }

    /**
     * Ensures both XML documents represent the same semantic data. Non-semantic
     * data such as namespace prefixes, comments, and whitespace is ignored.
     */
    private void assertNodesAreEquivalent(File expected, Node actual)
            throws ParserConfigurationException, IOException, SAXException,
            XmlPullParserException {

        Document expectedDocument = documentBuilder.parse(new FileInputStream(expected));
        String expectedString = nodeToNormalizedString(expectedDocument);
        String actualString = nodeToNormalizedString(actual);

        Assert.assertEquals("Expected XML to match file " + expected,
                expectedString, actualString);
    }

    private String nodeToNormalizedString(Node node)
            throws XmlPullParserException, IOException {
        StringWriter writer = new StringWriter();
        XmlSerializer xmlSerializer = xmlPullParserFactory.newSerializer();
        xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        xmlSerializer.setOutput(writer);
        emitNode(xmlSerializer, node);
        xmlSerializer.flush();
        return writer.toString();
    }

    private void emitNode(XmlSerializer serializer, Node node) throws IOException {
        if (node instanceof Element) {
            Element element = (Element) node;
            serializer.startTag(element.getBaseURI(), element.getLocalName());
            emitAttributes(serializer, element);
            emitChildren(serializer, element);
            serializer.endTag(element.getBaseURI(), element.getLocalName());

        } else if (node instanceof Text) {
            // TODO: is it okay to trim whitespace in general? This may cause
            //     false positives for elements like HTML's <pre> tag
            String trimmed = node.getTextContent().trim();
            if (trimmed.length() > 0) {
                serializer.text(trimmed);
            }

        } else if (node instanceof Document) {
            Document document = (Document) node;
            serializer.startDocument("UTF-8", true);
            emitNode(serializer, document.getDocumentElement());
            serializer.endDocument();

        } else if (node instanceof ProcessingInstruction) {
            ProcessingInstruction processingInstruction = (ProcessingInstruction) node;
            String data = processingInstruction.getData();
            String target = processingInstruction.getTarget();
            serializer.processingInstruction(target + " " + data);

        } else if (node instanceof Comment) {
            // ignore!

        } else {
            Object nodeClass = node != null ? node.getClass() : null;
            throw new UnsupportedOperationException(
                    "Cannot serialize nodes of type " + nodeClass);
        }
    }

    private void emitAttributes(XmlSerializer serializer, Node node)
            throws IOException {
        NamedNodeMap map = node.getAttributes();
        if (map == null) {
            return;
        }

        List<Attr> attributes = new ArrayList<Attr>();
        for (int i = 0; i < map.getLength(); i++) {
            attributes.add((Attr) map.item(i));
        }
        Collections.sort(attributes, orderByName);

        for (Attr attr : attributes) {
            if ("xmlns".equals(attr.getPrefix()) || "xmlns".equals(attr.getLocalName())) {
                /*
                 * Omit namespace declarations because they aren't considered
                 * data. Ie. <foo:a xmlns:bar="http://google.com"> is semantically
                 * equal to <bar:a xmlns:bar="http://google.com"> since the
                 * prefix doesn't matter, only the URI it points to.
                 *
                 * When we omit the prefix, our XML serializer will still
                 * generate one for us, using a predictable pattern.
                 */
            } else {
                serializer.attribute(attr.getBaseURI(), attr.getLocalName(), attr.getValue());
            }
        }
    }

    private void emitChildren(XmlSerializer serializer, Node node)
            throws IOException {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            emitNode(serializer, childNodes.item(i));
        }
    }

    private static List<Element> elementsOf(NodeList nodeList) {
        List<Element> result = new ArrayList<Element>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                result.add((Element) node);
            }
        }
        return result;
    }
}
