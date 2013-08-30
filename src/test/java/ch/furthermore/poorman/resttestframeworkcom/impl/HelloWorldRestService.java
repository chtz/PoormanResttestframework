package ch.furthermore.poorman.resttestframeworkcom.impl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HelloWorldRestService extends AbstractHandler
{
	private String lastHello;
	
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
		if ("GET".equals(request.getMethod())) {
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);
	        response.getWriter().println("<?xml version=\"1.0\"?><foo><hello>" + lastHello + "</hello><world>Welt</world></foo>");
		}
		else {  
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(request.getInputStream());
				
				for (Element match : new XmlHelper().xpathMatchElements(doc,  new GenericNamespaceContext(), "//foo/hello")) {
					match.setTextContent(lastHello = match.getTextContent().toUpperCase());
				}
				
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer transformer = tf.newTransformer();
				transformer.transform(new DOMSource(doc), new StreamResult(response.getWriter()));
			}
			catch (Exception e) {
				response.sendError(500);
			}
		}
    }
}
