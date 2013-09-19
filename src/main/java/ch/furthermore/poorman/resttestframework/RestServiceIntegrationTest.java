package ch.furthermore.poorman.resttestframework;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.furthermore.poorman.resttestframework.impl.RestServiceIntegrationTestEngine;

/**
 * Subclass me. I expect a folder named "./integrationTests" containing test files.
 */
@RunWith(Parameterized.class)
public abstract class RestServiceIntegrationTest {
	private static File auditDirectory = new File("integrationTestAudit");
	private File testFile;
	
	public RestServiceIntegrationTest(File testFile) {
		this.testFile = testFile;
	}

	@Parameters(name="{0}")
	public static Collection<Object[]> data() throws Exception {
		PrintWriter audit = auditDirectory.exists() ? new PrintWriter(new File(auditDirectory, "index.html")) : null;
		if (audit != null) {
			audit.println("<html><head></head><body><h1>Test Cases</h1><ul>");
		}
		
		Collection<Object[]> data = new LinkedList<Object[]>();
		for (File file : new File("integrationTests").listFiles()) {
			if (!file.isDirectory() && file.getPath().endsWith(".txt")) {
				if (audit != null) {
					audit.println("<li><a href='" + file.getName() + ".html'>" + file.getName() + "</a>" + "</li>");
				}
				
				data.add(new Object[]{file});
			}
		}
		
		if (audit != null) {
			audit.println("<ul></body></html>");
			audit.close();	
		}
		
		return data;
	}
	
	@Test
	public void restTest() throws Exception {
		new RestServiceIntegrationTestEngine().executeTestplan(auditDirectory, testFile);
	}
}
