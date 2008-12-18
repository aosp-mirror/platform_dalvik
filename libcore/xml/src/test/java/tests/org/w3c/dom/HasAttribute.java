package tests.org.w3c.dom;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;

/**
 * The "hasAttribute()" method for an Element should return true if the element
 * has an attribute with the given name. Retrieve the first "address" element
 * and the "hasAttribute()" method should return false since the element does
 * not have a default value.
 * 
 * @author NIST
 * @author Mary Brady
 * @see <a
 *      href="http://www.w3.org/TR/DOM-Level-2-Core/core#ID-ElHasAttr">http://www.w3.org/TR/DOM-Level-2-Core/core#ID-ElHasAttr</a>
 */
@TestTargetClass(Element.class) 
public final class HasAttribute extends DOMTestCase {

    DOMDocumentBuilderFactory factory;

    DocumentBuilder builder;

    protected void setUp() throws Exception {
        super.setUp();
        try {
            factory = new DOMDocumentBuilderFactory(DOMDocumentBuilderFactory
                    .getConfiguration1());
            builder = factory.getBuilder();
        } catch (Exception e) {
            fail("Unexpected exception" + e.getMessage());
        }
    }

    protected void tearDown() throws Exception {
        factory = null;
        builder = null;
        super.tearDown();
    }

    /**
     * Runs the test case.
     * 
     * @throws Throwable
     *             Any uncaught exception causes test to fail
     */
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies that hasAttribute method returns false.",
      targets = {
        @TestTarget(
          methodName = "hasAttribute",
          methodArgs = {java.lang.String.class}
        )
    })
    public void testHasAttribute1() throws Throwable {
        Document doc;
        NodeList elementList;
        Element testNode;
        boolean state;
        doc = (Document) load("staff", builder);
        elementList = doc.getElementsByTagName("address");
        testNode = (Element) elementList.item(4);
        state = testNode.hasAttribute("domestic");
        assertFalse("throw_False", state);
    }

// Assumes validation.
//    public void testHasAttribute2() throws Throwable {
//        Document doc;
//        NodeList elementList;
//        Element testNode;
//        boolean state;
//        doc = (Document) load("staff", builder);
//        elementList = doc.getElementsByTagName("address");
//        testNode = (Element) elementList.item(0);
//        state = testNode.hasAttribute("street");
//        assertTrue("throw_True", state);
//    }
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies that hasAttribute method returns false.",
      targets = {
        @TestTarget(
          methodName = "hasAttribute",
          methodArgs = {java.lang.String.class}
        )
    })
    public void testHasAttribute3() throws Throwable {
        Document doc;
        NodeList elementList;
        Element testNode;
        boolean state;
        doc = (Document) load("staff", builder);
        elementList = doc.getElementsByTagName("address");
        testNode = (Element) elementList.item(0);
        state = testNode.hasAttribute("nomatch");
        assertFalse("throw_False", state);
    }
    @TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "Verifies that hasAttribute method returns true.",
      targets = {
        @TestTarget(
          methodName = "hasAttribute",
          methodArgs = {java.lang.String.class}
        )
    })
    public void testHasAttribute4() throws Throwable {
        Document doc;
        NodeList elementList;
        Element testNode;
        boolean state;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("address");
        testNode = (Element) elementList.item(0);
        state = testNode.hasAttribute("dmstc:domestic");
        assertTrue("hasDomesticAttr", state);
    }
}
