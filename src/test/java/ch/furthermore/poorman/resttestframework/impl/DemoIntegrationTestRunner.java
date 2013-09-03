package ch.furthermore.poorman.resttestframework.impl;

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import ch.furthermore.poorman.resttestframework.RestServiceIntegrationTest;

public class DemoIntegrationTestRunner extends RestServiceIntegrationTest {
	public DemoIntegrationTestRunner(File testFile) {
		super(testFile);
	}
	
	private static Server server;
	
	@BeforeClass
	public static void startService() throws Exception {
		server = new Server(8787);
        server.setHandler(new HelloWorldRestService());
 
        server.start();
	}
	
	@AfterClass
	public static void stopService() throws Exception {
		server.stop();
	}
}
