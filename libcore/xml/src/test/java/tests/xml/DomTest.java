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

package tests.xml;

import junit.framework.TestCase;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Construct a DOM and then interrogate it.
 */
public class DomTest extends TestCase {

    private final String xml
            = "<!DOCTYPE menu ["
            + "  <!ENTITY sp \"Maple Syrup\">"
            + "]>"
            + "<menu>\n"
            + "  <item xmlns=\"http://food\" xmlns:a=\"http://addons\">\n"
            + "    <name a:standard=\"strawberry\" deluxe=\"&sp;\">Waffles</name>\n"
            + "    <description xmlns=\"http://marketing\">Belgian<![CDATA[ waffles & strawberries (< 5g ]]>of fat)</description>\n"
            + "    <a:option>Whipped Cream</a:option>\n"
            + "    <a:option>&sp;</a:option>\n"
            + "    <?wafflemaker square shape?>\n"
            + "    <nutrition>\n"
            + "      <a:vitamins xmlns:a=\"http://usda\">\n"
            + "        <!-- add other vitamins? --> \n"
            + "        <a:vitaminc>60%</a:vitaminc>\n"
            + "      </a:vitamins>\n"
            + "    </nutrition>\n"
            + "  </item>\n"
            + "</menu>";

    private Document document;
    private DocumentType doctype;
    private Entity sp;
    private Element menu;
    private Element item;
    private Attr itemXmlns;
    private Attr itemXmlnsA;
    private Element name;
    private Attr standard;
    private Attr deluxe;
    private Element description;
    private Text descriptionText1;
    private CDATASection descriptionText2;
    private Text descriptionText3;
    private Element option1;
    private Element option2;
    private Node option2Reference; // Text on RI, EntityReference on Dalvik
    private ProcessingInstruction wafflemaker;
    private Element nutrition;
    private Element vitamins;
    private Attr vitaminsXmlnsA;
    private Comment comment;
    private Element vitaminc;
    private Text vitamincText;

    private List<Node> allNodes;

    @Override protected void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        document = factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));

        // doctype nodes
        doctype = document.getDoctype();
        if (doctype.getEntities() != null) {
            sp = (Entity) doctype.getEntities().item(0);
        }

        // document nodes
        menu = document.getDocumentElement();
        item = (Element) menu.getChildNodes().item(1);
        itemXmlns = item.getAttributeNode("xmlns");
        itemXmlnsA = item.getAttributeNode("xmlns:a");
        name = (Element) item.getChildNodes().item(1);
        standard = name.getAttributeNode("a:standard");
        deluxe = name.getAttributeNode("deluxe");
        description = (Element) item.getChildNodes().item(3);
        descriptionText1 = (Text) description.getChildNodes().item(0);
        descriptionText2 = (CDATASection) description.getChildNodes().item(1);
        descriptionText3 = (Text) description.getChildNodes().item(2);
        option1 = (Element) item.getChildNodes().item(5);
        option2 = (Element) item.getChildNodes().item(7);
        option2Reference = option2.getChildNodes().item(0);
        wafflemaker = (ProcessingInstruction) item.getChildNodes().item(9);
        nutrition = (Element) item.getChildNodes().item(11);
        vitamins = (Element) nutrition.getChildNodes().item(1);
        vitaminsXmlnsA = vitamins.getAttributeNode("xmlns:a");
        comment = (Comment) vitamins.getChildNodes().item(1);
        vitaminc = (Element) vitamins.getChildNodes().item(3);
        vitamincText = (Text) vitaminc.getChildNodes().item(0);

        allNodes = new ArrayList<Node>();

        if (sp != null) {
            allNodes.add(sp);
        }

        allNodes.addAll(Arrays.asList(document, doctype, menu, item, itemXmlns,
                itemXmlnsA, name, standard, deluxe, description,
                descriptionText1, descriptionText2, descriptionText3, option1,
                option2, option2Reference, wafflemaker, nutrition, vitamins,
                vitaminsXmlnsA, comment, vitaminc, vitamincText));
    }

    /**
     * Android's parsed DOM doesn't include entity declarations. These nodes will
     * only be tested for implementations that support them.
     */
    public void testEntityDeclarations() {
        assertNotNull("This implementation does not parse entity declarations", sp);
    }

    public void testLookupNamespaceURIByPrefix() {
        assertEquals(null, doctype.lookupNamespaceURI("a"));
        if (sp != null) {
            assertEquals(null, sp.lookupNamespaceURI("a"));
        }
        assertEquals(null, document.lookupNamespaceURI("a"));
        assertEquals(null, menu.lookupNamespaceURI("a"));
        assertEquals("http://addons", item.lookupNamespaceURI("a"));
        assertEquals("http://addons", itemXmlns.lookupNamespaceURI("a"));
        assertEquals("http://addons", itemXmlnsA.lookupNamespaceURI("a"));
        assertEquals("http://addons", name.lookupNamespaceURI("a"));
        assertEquals("http://addons", standard.lookupNamespaceURI("a"));
        assertEquals("http://addons", deluxe.lookupNamespaceURI("a"));
        assertEquals("http://addons", description.lookupNamespaceURI("a"));
        assertEquals("http://addons", descriptionText1.lookupNamespaceURI("a"));
        assertEquals("http://addons", descriptionText2.lookupNamespaceURI("a"));
        assertEquals("http://addons", descriptionText3.lookupNamespaceURI("a"));
        assertEquals("http://addons", option1.lookupNamespaceURI("a"));
        assertEquals("http://addons", option2.lookupNamespaceURI("a"));
        assertEquals("http://addons", option2Reference.lookupNamespaceURI("a"));
        assertEquals("http://addons", wafflemaker.lookupNamespaceURI("a"));
        assertEquals("http://addons", nutrition.lookupNamespaceURI("a"));
        assertEquals("http://usda", vitamins.lookupNamespaceURI("a"));
        assertEquals("http://usda", vitaminsXmlnsA.lookupNamespaceURI("a"));
        assertEquals("http://usda", comment.lookupNamespaceURI("a"));
        assertEquals("http://usda", vitaminc.lookupNamespaceURI("a"));
        assertEquals("http://usda", vitamincText.lookupNamespaceURI("a"));
    }

    public void testLookupNamespaceURIWithNullPrefix() {
        assertEquals(null, document.lookupNamespaceURI(null));
        assertEquals(null, doctype.lookupNamespaceURI(null));
        if (sp != null) {
            assertEquals(null, sp.lookupNamespaceURI(null));
        }
        assertEquals(null, menu.lookupNamespaceURI(null));
        assertEquals("http://food", item.lookupNamespaceURI(null));
        assertEquals("http://food", itemXmlns.lookupNamespaceURI(null));
        assertEquals("http://food", itemXmlnsA.lookupNamespaceURI(null));
        assertEquals("http://food", name.lookupNamespaceURI(null));
        assertEquals("http://food", standard.lookupNamespaceURI(null));
        assertEquals("http://food", deluxe.lookupNamespaceURI(null));
        assertEquals("http://marketing", description.lookupNamespaceURI(null));
        assertEquals("http://marketing", descriptionText1.lookupNamespaceURI(null));
        assertEquals("http://marketing", descriptionText2.lookupNamespaceURI(null));
        assertEquals("http://marketing", descriptionText3.lookupNamespaceURI(null));
        assertEquals("http://food", option1.lookupNamespaceURI(null));
        assertEquals("http://food", option2.lookupNamespaceURI(null));
        assertEquals("http://food", option2Reference.lookupNamespaceURI(null));
        assertEquals("http://food", wafflemaker.lookupNamespaceURI(null));
        assertEquals("http://food", nutrition.lookupNamespaceURI(null));
        assertEquals("http://food", vitamins.lookupNamespaceURI(null));
        assertEquals("http://food", vitaminsXmlnsA.lookupNamespaceURI(null));
        assertEquals("http://food", comment.lookupNamespaceURI(null));
        assertEquals("http://food", vitaminc.lookupNamespaceURI(null));
        assertEquals("http://food", vitamincText.lookupNamespaceURI(null));
    }

    public void testLookupNamespaceURIWithXmlnsPrefix() {
        for (Node node : allNodes) {
            assertEquals(null, node.lookupNamespaceURI("xmlns"));
        }
    }

    public void testLookupPrefixWithShadowedUri() {
        assertEquals(null, document.lookupPrefix("http://addons"));
        assertEquals(null, doctype.lookupPrefix("http://addons"));
        if (sp != null) {
            assertEquals(null, sp.lookupPrefix("http://addons"));
        }
        assertEquals(null, menu.lookupPrefix("http://addons"));
        assertEquals("a", item.lookupPrefix("http://addons"));
        assertEquals("a", itemXmlns.lookupPrefix("http://addons"));
        assertEquals("a", itemXmlnsA.lookupPrefix("http://addons"));
        assertEquals("a", name.lookupPrefix("http://addons"));
        assertEquals("a", standard.lookupPrefix("http://addons"));
        assertEquals("a", deluxe.lookupPrefix("http://addons"));
        assertEquals("a", description.lookupPrefix("http://addons"));
        assertEquals("a", descriptionText1.lookupPrefix("http://addons"));
        assertEquals("a", descriptionText2.lookupPrefix("http://addons"));
        assertEquals("a", descriptionText3.lookupPrefix("http://addons"));
        assertEquals("a", option1.lookupPrefix("http://addons"));
        assertEquals("a", option2.lookupPrefix("http://addons"));
        assertEquals("a", option2Reference.lookupPrefix("http://addons"));
        assertEquals("a", wafflemaker.lookupPrefix("http://addons"));
        assertEquals("a", nutrition.lookupPrefix("http://addons"));
        assertEquals(null, vitamins.lookupPrefix("http://addons"));
        assertEquals(null, vitaminsXmlnsA.lookupPrefix("http://addons"));
        assertEquals(null, comment.lookupPrefix("http://addons"));
        assertEquals(null, vitaminc.lookupPrefix("http://addons"));
        assertEquals(null, vitamincText.lookupPrefix("http://addons"));
    }

    public void testLookupPrefixWithUnusedUri() {
        for (Node node : allNodes) {
            assertEquals(null, node.lookupPrefix("http://unused"));
        }
    }

    public void testLookupPrefixWithNullUri() {
        for (Node node : allNodes) {
            assertEquals(null, node.lookupPrefix(null));
        }
    }

    public void testLookupPrefixWithShadowingUri() {
        assertEquals(null, document.lookupPrefix("http://usda"));
        assertEquals(null, doctype.lookupPrefix("http://usda"));
        if (sp != null) {
            assertEquals(null, sp.lookupPrefix("http://usda"));
        }
        assertEquals(null, menu.lookupPrefix("http://usda"));
        assertEquals(null, item.lookupPrefix("http://usda"));
        assertEquals(null, itemXmlns.lookupPrefix("http://usda"));
        assertEquals(null, itemXmlnsA.lookupPrefix("http://usda"));
        assertEquals(null, name.lookupPrefix("http://usda"));
        assertEquals(null, standard.lookupPrefix("http://usda"));
        assertEquals(null, deluxe.lookupPrefix("http://usda"));
        assertEquals(null, description.lookupPrefix("http://usda"));
        assertEquals(null, descriptionText1.lookupPrefix("http://usda"));
        assertEquals(null, descriptionText2.lookupPrefix("http://usda"));
        assertEquals(null, descriptionText3.lookupPrefix("http://usda"));
        assertEquals(null, option1.lookupPrefix("http://usda"));
        assertEquals(null, option2.lookupPrefix("http://usda"));
        assertEquals(null, option2Reference.lookupPrefix("http://usda"));
        assertEquals(null, wafflemaker.lookupPrefix("http://usda"));
        assertEquals(null, nutrition.lookupPrefix("http://usda"));
        assertEquals("a", vitamins.lookupPrefix("http://usda"));
        assertEquals("a", vitaminsXmlnsA.lookupPrefix("http://usda"));
        assertEquals("a", comment.lookupPrefix("http://usda"));
        assertEquals("a", vitaminc.lookupPrefix("http://usda"));
        assertEquals("a", vitamincText.lookupPrefix("http://usda"));
    }
}
