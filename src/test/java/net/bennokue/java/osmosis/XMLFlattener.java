/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/* TODO Benno Javadoc */
package net.bennokue.java.osmosis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author bennokue
 */
public class XMLFlattener {
    private final Document xmlDocument;
    
    public XMLFlattener(File inputFile) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(false);
        DocumentBuilder builder = docFactory.newDocumentBuilder();
        this.xmlDocument = builder.parse(inputFile);
    }
    
    public String[] getXPathAsArray(String xpathString) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expression = xpath.compile(xpathString);
        
        NodeList nodeList = (NodeList) expression.evaluate(this.xmlDocument, XPathConstants.NODESET);
        
        String[] result = new String[nodeList.getLength()];
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            result[i] = node.getNodeValue();
        }
        
        return result;
    }
}
