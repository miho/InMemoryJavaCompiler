package eu.mihosoft.jcompiler;

import java.net.URISyntaxException;
import java.util.*;
import javax.tools.*;

/**
 * In-memory compiler for Java code.
 */
public final class JCompiler {
	
	private final JavaCompiler javac;
	private DynamicClassLoader classLoader;
	private Iterable<String> options;
	boolean ignoreWarnings = false;

	private final Map<String, CompilationUnitSource> sourceCodes = new HashMap<String, CompilationUnitSource>();

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
		this.classLoader = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
	}

	/**
	 * Specifies a custom parent classloader to be used during compilation.
	 * 
	 * @param parentClassLoader parent classloader to be used during compilation/class loading
	 * @return this instance (for chaining multiple configuration commands)
	 */
	public JCompiler useParentClassLoader(ClassLoader parent) {
		this.classLoader = new DynamicClassLoader(parent);
		return this;
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
	 * Specifies the options to be used by the compiler, e.g. '-Xlint:unchecked'.
	 *
	 * @param options compiler options
	 */
	public void setOptions(String... options) {
		this.options = Arrays.asList(options);
	}

	/**
	 * Specifies whether the default diagnostocs listener should ignore non-critical compiler output, like unchecked/unsafe operation
	 * warnings (only applies to default diagnostics listener).
	 */
	public void setIgnoreWarnings(boolean ignoreWarnings) {
		this.ignoreWarnings = ignoreWarnings;
	}

	/**
	 * Compiles all sources added with {@link #addSource(String, String)}.
	 *
	 * @return Map containing instances of all compiled classes
	 * @throws CompilationException if an error occurs during compilation
	 */
	public Map<String, List<CompiledClass>> compileAll() throws CompilationException {
		return compileAll(null);
	}

	/**
	 * Compiles all sources added with {@link #addSource(String, String)}. If the custom diagnostics listener is used, it is responsible for
	 * throwing compilation exceptions if errors occur.
	 * 
	 * @param diagnosticListener a custom diagnostocs listener (may be null)
	 * @return Map containing compile code, grouped by compilation unit
	 * @throws CompilationException if an error occurs during compilation (only thrown if default diagnostics listener is used)
	 */
	public Map<String, List<CompiledClass>> compileAll(DiagnosticListener<JavaFileObject> diagnosticListener) throws CompilationException {
		if (sourceCodes.size() == 0) {
			throw new CompilationException("No source code to compile");
		}
		Collection<CompilationUnitSource> compilationUnits = sourceCodes.values();
		CompiledClassFile[] code;

		code = new CompiledClassFile[compilationUnits.size()];
		Iterator<CompilationUnitSource> iter = compilationUnits.iterator();
		for (int i = 0; i < code.length; i++) {
			try{
				code[i] = new CompiledClassFile(classLoader, iter.next().getClassName());
			} catch(URISyntaxException ex) {
				throw new CompilationException("Illegal code name", ex);
			}
		}

		DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
		ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(javac.getStandardFileManager(null, null, null), classLoader);
		JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, diagnosticListener==null?collector:diagnosticListener, options, null, compilationUnits);
		boolean result = task.call();
		if (!result || collector.getDiagnostics().size() > 0) {
			StringBuilder exceptionMsg = new StringBuilder();
			exceptionMsg.append("Unable to compile the source");
			boolean hasWarnings = false;
			boolean hasErrors = false;
			for (Diagnostic<? extends JavaFileObject> d : collector.getDiagnostics()) {
				switch (d.getKind()) {
				case NOTE:
				case MANDATORY_WARNING:
				case WARNING:
					hasWarnings = true;
					break;
				case OTHER:
				case ERROR:
				default:
					hasErrors = true;
					break;
				}
				exceptionMsg.append("\n").append("[location=").append(d.getSource().getName().substring(1));
				exceptionMsg.append(", ").append("kind=").append(d.getKind());
				exceptionMsg.append(", ").append("line=").append(d.getLineNumber());
				exceptionMsg.append(", ").append("message=").append(d.getMessage(Locale.US)).append("]");
			}
			if (hasWarnings && !ignoreWarnings || hasErrors) {
				throw new CompilationException(exceptionMsg.toString());
			}
			
		}

		return fileManager.getCompiledCode();
	}

	/**
	 * Compiles a single source unit.
	 *
	 * @param compilationUnitName name of the compilation unit/file, used for
	 *                            diagnostics
	 * @param sourceCode code to compile
	 * @return
	 * @throws CompilationException
	 */
	public List<CompiledClass> compile(String compilationUnitName, String sourceCode) throws CompilationException {
		return addSource(compilationUnitName, sourceCode).compileAll().get(compilationUnitName);
	}

	/**
	 * Add source code to the compiler.
	 *
	 * @param compilationUnitName name of the compilation unit/file, used for
	 *                            diagnostics and for grouping compiled code
	 * @param sourceCode          code to compile
	 * @return this instance (for chaining invocation of this method)
	 * @see {@link #compileAll()}
	 */
	public JCompiler addSource(String compilationUnitName, String sourceCode) {
		try {
		sourceCodes.put(compilationUnitName, new CompilationUnitSource(compilationUnitName, sourceCode));
		} catch(URISyntaxException ex) {
			throw new IllegalArgumentException("Invalid name specified", ex);
		}
		return this;
	}
}
