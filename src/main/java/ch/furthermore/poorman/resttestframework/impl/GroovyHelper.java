package ch.furthermore.poorman.resttestframework.impl;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.util.Map;
import java.util.Map.Entry;

class GroovyHelper {
	@SuppressWarnings("unchecked")
	public void evaluateGroovyScript(String contextInfo, Map<String,Object> inOutVariables, String groovyScript) {
		try {
			Binding binding = new Binding();
			for (Entry<String, Object> e : inOutVariables.entrySet()) {
				binding.setVariable(e.getKey(), e.getValue());
			}
		
			GroovyShell shell = new GroovyShell(binding);
			
			Object last = shell.evaluate(groovyScript);
			
			inOutVariables.putAll(binding.getVariables());
			
			inOutVariables.put("last",  last);
		}
		catch (Exception e) {
			throw new RuntimeException(contextInfo + " => " + e.getMessage());
		}
	}
}
