package eu.mihosoft.jcompiler;

import java.util.HashMap;
import java.util.Map;

/**
 * An in-memory classloader for compiled code.
 */
/*pkg private*/ class InMemoryClassLoader extends ClassLoader {

	// compiled classes by name
	private final Map<String, CompiledClassFile> customCompiledCode = new HashMap<>();

	/**
	 * Creates a new instance of this class.
	 * @param parent parent classloader
	 */
	public InMemoryClassLoader(ClassLoader parent) {
		super(parent);
	}

	/**
	 * Adds the specified compiled class to this classloader. Existing classes with identical names are 
	 * replaced.
	 * @param cc compiled class to add
	 */
	public void addCode(CompiledClassFile cc) {
		customCompiledCode.put(cc.getName(), cc);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		CompiledClassFile cc = customCompiledCode.get(name);
		if (cc == null) {
			return super.findClass(name);
		}

		// TODO 10.03.2020: check that caching actually works (do previously defined/loaded class objects work with newly defined/loaded class objects?)

		// use cached version if available
		if(cc.hasCachedClass()) {
			return cc.getCachedClass();
		} else {
		// (re)load class otherwise	
			byte[] byteCode = cc.getByteCode();
			return defineClass(name, byteCode, 0, byteCode.length);
		}
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		CompiledClassFile cc = customCompiledCode.get(name);

		if (cc != null) {
			Class<?> cls = this.findClass(name);
			return cls;
		}

		return super.loadClass(name, resolve);
	}

}
