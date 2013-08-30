package ch.furthermore.poorman.resttestframework;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.furthermore.poorman.resttestframeworkcom.impl.RestServiceIntegrationTestEngine;

/**
 * Subclass me. I expect a folder named "./integrationTests" containing test files.
 */
@RunWith(Parameterized.class)
public abstract class RestServiceIntegrationTest {
	private File testFile;
	
	public RestServiceIntegrationTest(File testFile) {
		this.testFile = testFile;
	}

	@Parameters(name="{0}")
	public static Collection<Object[]> data() {
		Collection<Object[]> data = new LinkedList<Object[]>();
		for (File file : new File("integrationTests").listFiles()) {
			data.add(new Object[]{file});
		}
		return data;
	}
	
	@Test
	public void restTest() throws Exception {
		new RestServiceIntegrationTestEngine().executeTestplan(testFile);
	}
}
