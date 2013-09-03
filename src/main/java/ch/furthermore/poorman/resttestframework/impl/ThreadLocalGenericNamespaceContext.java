package ch.furthermore.poorman.resttestframework.impl;

public class ThreadLocalGenericNamespaceContext {
	private final static ThreadLocal<GenericNamespaceContext> genericNamespaceContext = new ThreadLocal<GenericNamespaceContext>();
	
	public static void registerNamespaceURI(String prefix, String namespaceURI) {
		genericNamespaceContext.get().registerNamespaceURI(prefix, namespaceURI);
	}
	
	public static void runWithGenericNamespaceContext(GenericNamespaceContext ctx, Runnable runnable) {
		try {
			genericNamespaceContext.set(ctx);
			
			runnable.run();
		}
		finally {
			genericNamespaceContext.remove();
		}
	}
}
