package eu.mihosoft.jcompiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

/**
 * Created by trung on 5/3/15. Edited by turpid-monkey on 9/25/15, completed
 * support for multiple compile units.
 * 
 * Edited by miho on March 9, 2020. Support for multiple compile units improved.
 */
/*pkg private */final class ExtendedStandardJavaFileManager extends
		ForwardingJavaFileManager<JavaFileManager> {

	private final Map<String, List<CompiledClass>> compiledCode = new HashMap<>();
	private final List<CompiledUnit> compiledUnits = new ArrayList<>();
	private final List<CompiledUnit> compiledUnitsUnmodifiable = 
		Collections.unmodifiableList(compiledUnits);


	private DynamicClassLoader cl;

	/**
	 * Creates a new instance of ForwardingJavaFileManager.
	 *
	 * @param fileManager
	 *            delegate to this file manager
	 * @param cl classloader to use for compile code
	 */
	protected ExtendedStandardJavaFileManager(JavaFileManager fileManager,
			DynamicClassLoader cl) {
		super(fileManager);
		this.cl = cl;
	}

	@Override
	public JavaFileObject getJavaFileForOutput(
			JavaFileManager.Location location, String className,
			JavaFileObject.Kind kind, FileObject sibling) throws IOException {

		try {
			CompiledClassFile containedClass = new CompiledClassFile(cl, className);

			String fName = sibling.getName();

			List<CompiledClass> codeList = compiledCode.get(fName);
			if(codeList ==null) {
				codeList = new ArrayList<>();
				compiledCode.put(fName, codeList);
				compiledUnits.add(new CompiledUnit(fName, codeList));
			}
			codeList.add(new CompiledClass(containedClass));
			cl.addCode(containedClass);
			return containedClass;
		} catch (Exception e) {
			throw new RuntimeException(
					"Error while creating in-memory output file for "
							+ className, e);
		}
	}

	@Override
	public ClassLoader getClassLoader(JavaFileManager.Location location) {
		return cl;
	}

	/**
	 * @return the compiled code as list of compiled units
	 */
	List<CompiledUnit> getCompiledCode() {
		return this.compiledUnitsUnmodifiable;
	}

}
