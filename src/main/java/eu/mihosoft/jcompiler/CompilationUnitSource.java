package eu.mihosoft.jcompiler;

import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Represents source code of a compilation unit.
 */
/*pkg private*/ final class CompilationUnitSource extends SimpleJavaFileObject {
	private String contents = null;
	private String className;

	public CompilationUnitSource(String className, String contents) throws URISyntaxException {
		super(URI.create("string:///" + className.replace('.', '/')
				+ Kind.SOURCE.extension), Kind.SOURCE);
		this.contents = contents;
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	public CharSequence getCharContent(boolean ignoreEncodingErrors)
			throws IOException {
		return contents;
	}
}
