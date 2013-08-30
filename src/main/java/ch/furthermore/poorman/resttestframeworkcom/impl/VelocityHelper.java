package ch.furthermore.poorman.resttestframeworkcom.impl;

import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

class VelocityHelper {
	public String replacePlaceholders(String s, Map<String,Object> placeholders) {
		Velocity.init();
		
		VelocityContext context = new VelocityContext();
		for (Entry<String, Object> e : placeholders.entrySet()) {
			context.put(e.getKey(), e.getValue());
		}
		
		StringWriter sw = new StringWriter();
		
		Velocity.evaluate(context, sw, "", s);
		
		return sw.toString();
	}
}
