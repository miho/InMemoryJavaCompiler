/*
 * Copyright 2020 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
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
package org.mdkt.compiler;

import java.util.*;

import eu.mihosoft.jcompiler.CompilationResult;
import eu.mihosoft.jcompiler.CompiledClass;
import eu.mihosoft.jcompiler.CompiledUnit;
import eu.mihosoft.jcompiler.JCompiler;

/**
 * Compile Java sources in-memory. This API is kept in this project to stay backwards compatible.
 * The implementation has been completely replaced by the new {@link JCompiler} API.
 */
public final class InMemoryJavaCompiler {

    boolean ignoreWarnings = false;
    private final JCompiler compiler;

	public static InMemoryJavaCompiler newInstance() {
		return new InMemoryJavaCompiler();
	}

	private InMemoryJavaCompiler() {
        this.compiler = JCompiler.newInstance();
	}

	public InMemoryJavaCompiler useParentClassLoader(ClassLoader parent) {
        this.compiler.setParentClassLoader(parent);
		return this;
	}

	/**
	 * @return the class loader used internally by the compiler
	 */
	public ClassLoader getClassloader() {
		return this.compiler.getClassloader();
	}

	/**
	 * Options used by the compiler, e.g. '-Xlint:unchecked'.
	 *
	 * @param options
	 * @return
	 */
	public InMemoryJavaCompiler useOptions(String... options) {
		this.compiler.setOptions(options);
		return this;
	}

	/**
	 * Ignore non-critical compiler output, like unchecked/unsafe operation
	 * warnings.
	 *
	 * @return
	 */
	public InMemoryJavaCompiler ignoreWarnings() {
		ignoreWarnings = true;
		return this;
	}

	/**
	 * Compile all sources
	 *
	 * @return Map containing instances of all compiled classes
	 * @throws Exception
	 */
	public Map<String, Class<?>> compileAll() throws Exception {

        CompilationResult result = this.compiler.compileAll();

		List<CompiledUnit> compiledUnits = result.checkNoErrors(ignoreWarnings).getCompiledUnits();

		Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
		for (CompiledUnit cu : compiledUnits) {
            if(cu.getClasses().isEmpty()) continue;
			CompiledClass cc = cu.getMainClass();
			if(cc!=null) {
				classes.put(cc.getClassName(), cc.loadClass());
			}
		}
		return classes;
	}

	/**
	 * Compile single source
	 *
	 * @param className
	 * @param sourceCode
	 * @return
	 * @throws Exception
	 */
	public Class<?> compile(String className, String sourceCode) throws Exception {
		return addSource(className, sourceCode).compileAll().get(className);
	}

	/**
	 * Add source code to the compiler
	 *
	 * @param className
	 * @param sourceCode
	 * @return
	 * @throws Exception
	 * @see {@link #compileAll()}
	 */
	public InMemoryJavaCompiler addSource(String className, String sourceCode) throws Exception {
		this.compiler.addSource(className, sourceCode);
		return this;
	}
}