package ch.furthermore.poorman.resttestframework.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

class GenericNamespaceContext implements NamespaceContext {
	private final Map<String,String> uriByPrefix = new HashMap<String, String>();
	
	public void registerNamespaceURI(String prefix, String namespaceURI) {
		uriByPrefix.put(prefix, namespaceURI);
	}
	
    public String getNamespaceURI(String prefix) {
    	return uriByPrefix.get(prefix);
    }

    public String getPrefix(String namespaceURI) {
        return null;
    }

    @SuppressWarnings("rawtypes")
	public Iterator getPrefixes(String namespaceURI) {
        return null;
    }
}