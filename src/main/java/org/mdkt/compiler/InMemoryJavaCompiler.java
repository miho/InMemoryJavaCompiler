package org.mdkt.compiler;

import java.util.*;

import eu.mihosoft.jcompiler.CompilationResult;
import eu.mihosoft.jcompiler.CompiledClass;
import eu.mihosoft.jcompiler.CompiledUnit;
import eu.mihosoft.jcompiler.JCompiler;

/**
 * Compile Java sources in-memory. This API is kept in this project to stay backwards compatible.
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