/*
 * Copyright 2020-2022 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Code is based on abandoned project by Trung (https://github.com/trung/InMemoryJavaCompiler).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
		} else {
			byte[] byteCode = cc.getByteCode();
			return defineClass(name, byteCode, 0, byteCode.length);
		}
		
	}
}
