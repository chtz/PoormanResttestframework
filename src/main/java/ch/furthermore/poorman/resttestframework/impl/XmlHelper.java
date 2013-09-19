package ch.furthermore.poorman.resttestframework.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class XmlHelper {
	public org.w3c.dom.Document createDocument(byte[] responseXmlData) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		
		return builder.parse(new ByteArrayInputStream(responseXmlData));
	}
	
	public String xmlToString(Node doc) throws TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
		return result.getWriter().toString();
	}
	
	public List<Element> xpathMatchElements(org.w3c.dom.Document doc, NamespaceContext ctx, String expression) throws XPathExpressionException {
		List<Element> result = new LinkedList<Element>();
		
		NodeList nodes = xpathMatchNodeset(doc, ctx, expression); 
		for (int i = 0; i < nodes.getLength(); i++) {
			Element item = (Element)nodes.item(i);
			result.add(item);
		}
		
		return result;
	}

	private NodeList xpathMatchNodeset(org.w3c.dom.Document doc, NamespaceContext ctx, String expression) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(ctx);
		
		XPathExpression expr = xpath.compile(expression);
		
		return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
	}
}
