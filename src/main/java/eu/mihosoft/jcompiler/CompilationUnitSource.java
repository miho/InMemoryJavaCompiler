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

	/**
	 * Creates a new instance of this class.
	 * @param className classname
	 * @param contents code of this compilation unit
	 * @throws URISyntaxException if the classname is invalid
	 */
	/*pkg private*/ CompilationUnitSource(String className, String contents) throws URISyntaxException {
		super(URI.create("string:///" + className.replace('.', '/')
				+ Kind.SOURCE.extension), Kind.SOURCE);
		this.contents = contents;
		this.className = className;
	}

	/**
	 * Returns the name of this class.
	 * @return the name of this class
	 */
	public String getClassName() {
		return className;
	}

	@Override
	public String getCharContent(boolean ignoreEncodingErrors)
			throws IOException {
		return contents;
	}
}
