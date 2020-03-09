package eu.mihosoft.jcompiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicClassLoader extends ClassLoader {

	private final Map<String, CompiledClassFile> customCompiledCode = new HashMap<>();

	public DynamicClassLoader(ClassLoader parent) {
		super(parent);
	}

	public void addCode(CompiledClassFile cc) {
		customCompiledCode.put(cc.getName(), cc);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		CompiledClassFile cc = customCompiledCode.get(name);
		if (cc == null) {
			return super.findClass(name);
		}
		byte[] byteCode = cc.getByteCode();
		return defineClass(name, byteCode, 0, byteCode.length);
	}
}
