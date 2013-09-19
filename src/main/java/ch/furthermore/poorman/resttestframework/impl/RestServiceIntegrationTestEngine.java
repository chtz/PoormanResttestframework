package ch.furthermore.poorman.resttestframework.impl;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.jayway.jsonpath.JsonPath;

/**
 * This class is not thread-safe
 */
public class RestServiceIntegrationTestEngine {
	private final VelocityHelper velocityHelper = new VelocityHelper();
	private final HttpHelper httpHelper = new HttpHelper();
	private final XmlHelper xmlHelper = new XmlHelper();
	private final GroovyHelper groovyHelper = new GroovyHelper();
	
	private final Map<String,Object> variables = new HashMap<String, Object>();
	private final GenericNamespaceContext namespaceContext = new GenericNamespaceContext();
	private DataInputStream in;
	private String line;
	private int lineNumber = 0;
	private StringTokenizer lineTokenizer;
	private Document lastResponseDoc = null;

	private PrintWriter audit;
	private String lastTitle;
	
	public void executeTestplan(File auditDirectory, File testplanFile) throws Exception {
		audit = auditDirectory.exists() ? new PrintWriter(new File(auditDirectory, testplanFile.getName() + ".html")) : null;
		try {
			audit("<html><head /><body>");
			executeTestplan(testplanFile);
		}
		finally {
			audit("</body></html>");
			if (audit != null) {
				audit.close();
			}
		}
	}
	
	private void audit(String... ss) {
		if (audit != null) {
			for (String s : ss) {
				audit.println(s);
			}
		}
	}

	private void executeTestplan(File testplanFile) throws Exception {
		audit("<h1>Scenario</h1><p>", testplanFile.getName(), "</p>");
		
		in = new DataInputStream(new BufferedInputStream(new FileInputStream(testplanFile)));
		try {
			for (nextLine(); hasLine(); nextLine()) {
				int firstLineNumber = lineNumber;
				String firstLine = line;
				
				lineTokenizer = new StringTokenizer(line, " \t");
				
				String cmd = nextOptionalToken();
				
				boolean jsonRequest = false;
				boolean jsonResponse = false;
				if ("json".equals(cmd)) {
					cmd = nextOptionalToken();
					
					jsonRequest = true;
					jsonResponse = true;
					
					if ("onlyrequest".equals(cmd)) {
						jsonResponse = false;
						
						cmd = nextOptionalToken();
					}
					else if ("onlyresponse".equals(cmd)) {
						jsonRequest = false;
						
						cmd = nextOptionalToken();
					}
				}
				
				boolean isDelete = "delete".equals(cmd);
				boolean isGetOrDelete = "get".equals(cmd) || isDelete;
				boolean isPostOrPut = "put".equals(cmd) || "post".equals(cmd);
				
				if (isPostOrPut || isGetOrDelete) {
					String url = nextTemplateToken();
					String user = nextOptionalTemplateToken();
					String pass = nextOptionalTemplateToken();

					if (lastTitle != null) { 
						audit("<h2>Next Step</h2><p>", lastTitle, "</p>");
						
						lastTitle = null;
					}
					
					audit("<h2>Action</h2>", "<p>", cmd, " ", url, "</p>");
					
					
					String xml = null;
					if (isPostOrPut) {
						xml = replacePlaceholders(readDataUntil("end"));
						
						if (isXml(xml.getBytes())) {
							audit("<h2>Request Data</h2><p><pre>", escapeHtml(xmlHelper.xmlToString(xmlHelper.createDocument(xml.getBytes()))), "</pre></p>");
						}
						else {
							String prettyJson = JsonHelper.prettyPrint(xml);
							if (prettyJson != null) {
								audit("<h2>Request JSON Data</h2><p><pre>", escapeHtml(prettyJson), "</pre></p>");
							}
							else {
								audit("<h2>Request Raw Data</h2><p><pre>", escapeHtml(xml), "</pre></p>");
							}
						}
					}
					
					lastResponseDoc = null;
					try {
						byte[] response = httpHelper.httpRequest(cmd, url, xml, user, pass, jsonRequest, jsonResponse);
						
						String responseString = new String(response);
						variables.put("response", responseString);

						boolean responseAuditWritten = false;
						if (isXml(response)) 
						{
							try {
								lastResponseDoc = xmlHelper.createDocument(response);
								
								variables.put("xml", lastResponseDoc);
								
								audit("<h2>Response Data</h2><p><pre>", escapeHtml(xmlHelper.xmlToString(lastResponseDoc)), "</pre></p>");
								responseAuditWritten = true;
							}
							catch (Exception e) {
								variables.put("xml", null);
							}
						}
						
						if (!responseAuditWritten) {
							String prettyJson = JsonHelper.prettyPrint(responseString);
							if (prettyJson != null) {
								audit("<h2>Response JSON Data</h2><p><pre>", escapeHtml(prettyJson), "</pre></p>");
							}
							else {
								audit("<h2>Response Raw Data</h2><p><pre>", escapeHtml(responseString), "</pre></p>");
							}
						}
					}
					catch (Exception e) {
						throw new RuntimeException(firstLineNumber + ": " + firstLine, e); 
					}
				}
				else if ("extract".equals(cmd)) {
					String variable = nextToken();
					String xpathExpression = nextToken();
					
					List<String> elementAttribs = allTokens();
					
					List<Map<String,Object>> elementValues = new LinkedList<Map<String,Object>>();
					for (Element e : xmlHelper.xpathMatchElements(lastResponseDoc, namespaceContext, xpathExpression)) {
						Map<String,Object> elementAttribValues = new HashMap<String, Object>();
						for (int i = 0; i < e.getAttributes().getLength(); i++) {
							Node a = e.getAttributes().item(i);
							
							if (elementAttribs.contains(a.getNodeName())) {
								elementAttribValues.put(a.getNodeName(), a.getNodeValue());
							}
						}
						
						elementAttribValues.put("content",  e.getTextContent());
						
						elementValues.add(elementAttribValues);
					}
					
					variables.put(variable, elementValues.toArray());
				}
				else if (cmd.startsWith("#")) {
					lastTitle = line.substring(1);	
				}
				else if (cmd.trim().isEmpty() || cmd.startsWith("//")) {
					//ignore
				}
				else {
					final String groovyScript;
					if ("{".equals(line.trim())) {
						groovyScript = readDataUntil("}");
					}
					else {
						groovyScript = line;
					}
					
					try {
						ThreadLocalGenericNamespaceContext.runWithGenericNamespaceContext(namespaceContext, new Runnable() {
							public void run() {
								groovyHelper.evaluateGroovyScript(line, variables, 
										"import static " + Assert.class.getCanonicalName() + ".*\n"
										+ "import static " + JsonPath.class.getCanonicalName() + ".*\n"
										+ "import static " + ThreadLocalGenericNamespaceContext.class.getCanonicalName() + ".*\n" 
										+ groovyScript);
							}
						});
					}
					catch (Exception e) {
						throw new RuntimeException(firstLineNumber + ": " + groovyScript, e);
					}
				}
			}
		}
		finally {
			in.close();
		}
	}

	private boolean isXml(byte[] response) { //TODO improve ;-)
		return response.length >= 5 && response[0] == '<' && response[1] == '?' && response[2] == 'x' && response[3] == 'm' && response[4] == 'l'
		    || response.length >= 5 && response[0] == '<' && response[1] == 's' && response[2] == 'o' && response[3] == 'a' && response[4] == 'p';
	}

	private String escapeHtml(String s) {
		return StringEscapeUtils.escapeHtml(s);
	}

	private String replacePlaceholders(String template) {
		return velocityHelper.replacePlaceholders(template,  variables);
	}

	private boolean hasLine() {
		return line != null;
	}

	@SuppressWarnings("deprecation")
	private String nextLine() throws IOException {
		lineNumber++;
		return line = in.readLine();
	}
	
	private String readDataUntil(String endLine) throws IOException {
		StringBuilder sb = new StringBuilder();
		
		for (;;) {
			String dataLine = nextLine();
			
			if (endLine.equals(dataLine)) {
				break;
			}
			
			if (sb.length() > 0){
				sb.append("\n");
			}
			
			sb.append(dataLine);
		}
		
		return sb.toString();
	}

	private boolean hasMoreTokens() {
		return lineTokenizer.hasMoreTokens();
	}

	private String nextToken() {
		return lineTokenizer.nextToken();
	}
	
	private String nextOptionalToken() {
		return hasMoreTokens() ? nextToken() : "";
	}

	private String nextTemplateToken() {
		return velocityHelper.replacePlaceholders(nextToken(), variables);
	}
	
	private String nextOptionalTemplateToken() {
		return velocityHelper.replacePlaceholders(nextOptionalToken(), variables);
	}
	
	private List<String> allTokens() {
		List<String> elementAttribs = new LinkedList<String>();
		
		while (hasMoreTokens()) {
			elementAttribs.add(nextToken());
		}
		
		return elementAttribs;
	}
}
