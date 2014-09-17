/**
 * Tests for de.guruz.p300.webdav.search.host.WebDAVSearch
 */
package de.guruz.p300.tests.automated;

import de.guruz.p300.utils.DOMUtils;
import de.guruz.p300.webdav.search.host.WebDAVSearch;
import de.guruz.p300.webdav.search.host.WebDAVSearchException;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Test WebDAVSearch for good output
 *
 * @author tomcat
 *
 */
public class WebDAVSearchTest {

    private static final String DAV_NAMESPACE = "DAV:";

    /**
     * Read a file that contains XML Return the XML as DOM Document
     *
     * @param filename
     * @return
     */
    private Document readDocumentFromFilename(String filename) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException exception) {
            System.err.println(exception.getLocalizedMessage());
            return null;
        }
        File file = new File(filename);
        Document document;
        try {
            document = builder.parse(file);
        } catch (Exception exception) {
            System.err.println(exception.getLocalizedMessage());
            return null;
        }
        return document;
    }

    /**
     * Test a search that should fail Taken directly from the WebDAV SEARCH standard "This query
     * retrieves the content length values for all resources located under the server's
     * "/container1/" URI namespace whose length exceeds 10000 sorted ascending by size." The second
     * one is a valid query with "/shares" instead of "/shares/" as location.
     */
    @Ignore(value = "should me moved into manual testcase folder")
    @Test
    public void testSearchFail() {
        Document queryDocument = readDocumentFromFilename("de/guruz/p300/tests/automated/WebDAVSearchTestResources/ExampleQuery");
        boolean error = false;
        try {
            WebDAVSearch.search(queryDocument);
        } catch (WebDAVSearchException exception) {
            error = true;
        }
        assertTrue(error);
        queryDocument = readDocumentFromFilename("de/guruz/p300/tests/automated/WebDAVSearchTestResources/ExampleQuery3");
        error = false;
        try {
            WebDAVSearch.search(queryDocument);
        } catch (WebDAVSearchException exception) {
            error = true;
        }
        assertTrue(error);
    }

    /**
     * Test a search that should not fail
     */
    @Ignore(value = "should me moved into manual testcase folder")
    @Test
    public void testSearchSucceed() {
        Document queryDocument = readDocumentFromFilename("de/guruz/p300/tests/automated/WebDAVSearchTestResources/ExampleQuery2");
        boolean error = false;
        try {
            WebDAVSearch.search(queryDocument);
        } catch (WebDAVSearchException exception) {
            assertTrue(false);
        }
    }

    /**
     * Look for a "TestFile" file in any share For this to succeed, you need a file named "TestFile"
     * in any share or subdirectory of a share. The file needs to be exactly 42 bytes in size.
     * Command to create it: dd if=/dev/zero of=$YOURSHARE/TestFile count=42 bs=1
     */
    @Ignore(value = "should me moved into manual testcase folder")
    @Test
    public void testSearchTestFile() {
        Document queryDocument = readDocumentFromFilename("de/guruz/p300/tests/automated/WebDAVSearchTestResources/ExampleQuery2");
        Document resultDocument = null;
        try {
            resultDocument = WebDAVSearch.search(queryDocument);
        } catch (WebDAVSearchException exception) {
            assertTrue(false);
        }
        Node multistatusNode = resultDocument.getFirstChild();
        assertNotNull(multistatusNode);
        Node responseNode = DOMUtils.getFirstNamedChildNS(multistatusNode, DAV_NAMESPACE, "response");
        assertNotNull(responseNode);
        Node hrefNode = DOMUtils.getFirstNamedChildNS(responseNode, DAV_NAMESPACE, "href");
        assertNotNull(hrefNode);
        Node hrefTextNode = DOMUtils.getFirstTextChild(hrefNode);
        assertNotNull(hrefTextNode);
        assertTrue(hrefTextNode.getTextContent().startsWith("/shares/"));
        assertTrue(hrefTextNode.getTextContent().endsWith("/TestFile"));
        Node propStatNode = DOMUtils.getFirstNamedChildNS(responseNode, DAV_NAMESPACE, "propstat");
        assertNotNull(propStatNode);
        Node propNode = DOMUtils.getFirstNamedChildNS(propStatNode, DAV_NAMESPACE, "prop");
        assertNotNull(propNode);
        Node displayNameNode = DOMUtils.getFirstNamedChildNS(propNode, DAV_NAMESPACE, "displayname");
        assertNotNull(displayNameNode);
        Node displayNameText = DOMUtils.getFirstTextChild(displayNameNode, "/TestFile");
        assertNotNull(displayNameText);
        Node contentLengthNode = DOMUtils.getFirstNamedChildNS(propNode, DAV_NAMESPACE, "getcontentlength");
        assertNotNull(contentLengthNode);
        Node contentLengthText = DOMUtils.getFirstTextChild(contentLengthNode, "42");
        assertNotNull(contentLengthText);
        Node resourceTypeNode = DOMUtils.getFirstNamedChildNS(propNode, DAV_NAMESPACE, "resourcetype");
        assertNull(resourceTypeNode);
    }

    /**
     * Look for a "TestDirectory" directory in any share For this to succeed, you need a directory
     * named "TestDirectory" in any share or subdirectory of a share. Command to create it: mkdir
     * $YOURSHARE/TestDirectory
     */
    @Ignore(value = "should me moved into manual testcase folder")
    @Test
    public void testSearchTestDirectory() {
        Document queryDocument = readDocumentFromFilename("de/guruz/p300/tests/automated/WebDAVSearchTestResources/ExampleQuery4");
        Document resultDocument = null;
        try {
            resultDocument = WebDAVSearch.search(queryDocument);
        } catch (WebDAVSearchException exception) {
            assertTrue(false);
        }
        Node multistatusNode = resultDocument.getFirstChild();
        assertNotNull(multistatusNode);
        Node responseNode = DOMUtils.getFirstNamedChildNS(multistatusNode, DAV_NAMESPACE, "response");
        assertNotNull(responseNode);
        Node hrefNode = DOMUtils.getFirstNamedChildNS(responseNode, DAV_NAMESPACE, "href");
        assertNotNull(hrefNode);
        Node hrefTextNode = DOMUtils.getFirstTextChild(hrefNode);
        assertNotNull(hrefTextNode);
        assertTrue(hrefTextNode.getTextContent().startsWith("/shares/"));
        assertTrue(hrefTextNode.getTextContent().endsWith("/TestDirectory"));
        Node propStatNode = DOMUtils.getFirstNamedChildNS(responseNode, DAV_NAMESPACE, "propstat");
        assertNotNull(propStatNode);
        Node propNode = DOMUtils.getFirstNamedChildNS(propStatNode, DAV_NAMESPACE, "prop");
        assertNotNull(propNode);
        Node displayNameNode = DOMUtils.getFirstNamedChildNS(propNode, DAV_NAMESPACE, "displayname");
        assertNotNull(displayNameNode);
        Node displayNameText = DOMUtils.getFirstTextChild(displayNameNode, "/TestDirectory");
        assertNotNull(displayNameText);
        Node contentLengthNode = DOMUtils.getFirstNamedChildNS(propNode, DAV_NAMESPACE, "getcontentlength");
        assertNotNull(contentLengthNode);
        Node contentLengthText = DOMUtils.getFirstTextChild(contentLengthNode, "0");
        assertNotNull(contentLengthText);
        Node resourceTypeNode = DOMUtils.getFirstNamedChildNS(propNode, DAV_NAMESPACE, "resourcetype");
        assertNotNull(resourceTypeNode);
        Node collectionTypeNode = DOMUtils.getFirstNamedChildNS(resourceTypeNode, DAV_NAMESPACE, "collection");
        assertNotNull(collectionTypeNode);
    }

}
