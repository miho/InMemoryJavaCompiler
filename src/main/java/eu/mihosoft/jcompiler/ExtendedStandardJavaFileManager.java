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

import io.github.classgraph.ClassGraph;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import javax.tools.*;

/**
 * Copyright 2015-2017 Trung (https://github.com/trung/InMemoryJavaCompiler). All rights reserved.
 * 
 * Code is under APL 2.0 just like the new API. This class has just been imrpoved but since 
 * it is internal API we keep it for now.
 * 
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
	private InMemoryClassLoader cl;

	/**
	 * Creates a new instance of ForwardingJavaFileManager.
	 *
	 * @param fileManager
	 *            delegate to this file manager
	 * @param cl classloader to use for compile code
	 */
	protected ExtendedStandardJavaFileManager(JavaFileManager fileManager,
			InMemoryClassLoader cl) {
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
				String code = sibling.getCharContent(true).toString();
				compiledUnits.add(new CompiledUnit(fName, code,
					codeList)
				);
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

		// sort classes in units
		for(CompiledUnit cU : this.compiledUnitsUnmodifiable) {
			cU.initAndSortClassNames();
		}

		return this.compiledUnitsUnmodifiable;
	}

	@Override
	public String inferBinaryName(Location location, JavaFileObject file) {
		if (file instanceof ClassLoaderClassFile) {
			return ((ClassLoaderClassFile)file).getClassName();
		} else {
			return super.inferBinaryName(location, file);
		}
	}

	@Override
	public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {

		Iterable<JavaFileObject> superResults = super.list(location, packageName, kinds, recurse);

		// NOTE we might want to scan classpath via classgraph

		return superResults;

	}

}
