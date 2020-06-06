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
package eu.mihosoft.jcompiler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import javax.tools.*;

/**
 * Simple and efficient compiler API for Java code (defaults to in-memory compilation).
 */
public final class JCompiler {
	
	// instance to the java compiler instance
	private final JavaCompiler javac;
	// classloader used to load compiled compilation units/classes
	private InMemoryClassLoader classLoader;
	// compiler options
	private Iterable<String> options;

	// source code map (compilation units by name)
	private final Map<String, CompilationUnitSource> sourceCodes 
		= new HashMap<String, CompilationUnitSource>();

	/**
	 * Creates a new instance of this class.
	 * 
	 * @return a new instance of this class
	 */
	public static JCompiler newInstance() {
		return new JCompiler();
	}

	/**
	 * Creates a new instance of this class.
	 */
	private JCompiler() {
		this.javac = ToolProvider.getSystemJavaCompiler();
		this.classLoader = new InMemoryClassLoader(ClassLoader.getSystemClassLoader());
	}

	/**
	 * Specifies a custom parent classloader to be used during compilation.
	 * 
	 * @param parentClassLoader parent classloader to be used during compilation/class loading
	 */
	public void setParentClassLoader(URLClassLoader parentClassLoader) {
		this.classLoader = new InMemoryClassLoader(parentClassLoader);
	}

	/**
	 * Returns the class loader used by the compiler.
	 * 
	 * @return the class loader used by the compiler
	 */
	public ClassLoader getClassloader() {
		return classLoader;
	}

	/**
	 * Specifies the options to be used by the compiler, e.g. {@code -Xlint:unchecked}.
	 *
	 * @param options compiler options to be used during compilation
	 */
	public void setOptions(String... options) {
		this.options = Arrays.asList(options);
	}

	/**
	 * Compiles all sources added with {@link #addSource(String, String)}.
	 *
	 * @return Map containing instances of all compiled classes
	 * @throws CompilationException if an error occurs during compilation
	 */

	/**
	 * Compiles all sources added with {@link #addSource(String, String)}.
	 * 
	 * @return compilation result (compiled classes, warnings and errors)
	 */
	public CompilationResult compileAll() {
		if (sourceCodes.size() == 0) {
			throw new CompilationException("No source code to compile");
		}
		Collection<CompilationUnitSource> compilationUnits = sourceCodes.values();
		CompiledClassFile[] code = new CompiledClassFile[compilationUnits.size()];
		Iterator<CompilationUnitSource> iter = compilationUnits.iterator();
		for (int i = 0; i < code.length; i++) {
			try{
				code[i] = new CompiledClassFile(classLoader, iter.next().getClassName());
			} catch(URISyntaxException ex) {
				throw new CompilationException("Illegal code name", ex);
			}
		}


		DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();

		final StandardJavaFileManager fileManagerStandard = javac.getStandardFileManager(null, null, null);

		// add urls from url classloader to classpath
		if( classLoader.getParent() instanceof URLClassLoader) {
			URLClassLoader urlClassLoader = (URLClassLoader) classLoader.getParent();
			List<File> paths = new ArrayList<>();

			Iterable<? extends File> files = fileManagerStandard.getLocation(StandardLocation.CLASS_PATH);
			files.forEach(f -> paths.add(f));

			for (URL url : urlClassLoader.getURLs()) {
				File file = new File(url.getFile());
				paths.add(file);
			}

			try {
				fileManagerStandard.setLocation(StandardLocation.CLASS_PATH, paths);
			} catch (IOException e) {
				throw new RuntimeException("Error while loading urls from classloader", e);
			}
		}

		// perform the compilation
		ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(fileManagerStandard, classLoader);
		JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector, options, null, compilationUnits);
		boolean result = task.call();

		return new CompilationResult(fileManager.getCompiledCode(), collector.getDiagnostics(), result);
	}

	/**
	 * Compiles a single source unit. The name of the compilation unit is inferred from the code (at least one type declaration necessary).
	 *
	 * @param sourceCode code to compile
	 */
	public CompilationResult compile(String sourceCode) throws CompilationException {
		return addSource(sourceCode).compileAll();
	}

	/**
	 * Compiles a single source unit.
	 *
	 * @param compilationUnitName name of the compilation unit/public class
	 * @param sourceCode code to compile
	 */
	public CompilationResult compile(String compilationUnitName, String sourceCode) throws CompilationException {
		return addSource(compilationUnitName, sourceCode).compileAll();
	}

	/**
	 * Add source code to the compiler.
	 *
	 * @param compilationUnitName name of the compilation unit/file, used for
	 *                            diagnostics and for grouping compiled code
	 * @param sourceCode          code to compile
	 * @return this instance (for chaining invocation of this method)
	 * @see {@link #compileAll()} {@link #addSource(String sourceCode)}
	 */
	public JCompiler addSource(String compilationUnitName, String sourceCode) {
		try {
			sourceCodes.put(compilationUnitName, new CompilationUnitSource(compilationUnitName, sourceCode));
		} catch(URISyntaxException ex) {
			throw new IllegalArgumentException("Invalid name specified", ex);
		}
		return this;
	}

	/**
	 * Add source code to the compiler.
	 *
	 * @param sourceCode          code to compile
	 * @return this instance (for chaining invocation of this method)
	 * @see {@link #compileAll()}, {@link #addSource(String compilationUnitName, String sourceCode)}
	 */
	public JCompiler addSource(String sourceCode) {
		try {
			String compilationUnitName = LangUtils.getClassNameFromCode(sourceCode);

			if(compilationUnitName.isEmpty()) {
				throw new CompilationException(
					"Cannot infer compilation unit name from compilation unit without type declaration\n\n"
					+sourceCode);
			}

			sourceCodes.put(compilationUnitName, new CompilationUnitSource(compilationUnitName, sourceCode));
		} catch(URISyntaxException ex) {
			throw new IllegalArgumentException("Invalid name specified", ex);
		}
		return this;
	}
}
