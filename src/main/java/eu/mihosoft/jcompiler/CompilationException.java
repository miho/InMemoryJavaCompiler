package eu.mihosoft.jcompiler;

/**
 * Indicates a compilation error.
 */
public class CompilationException extends RuntimeException {
	private static final long serialVersionUID = 5272588827551900536L;

	public CompilationException(String msg) {
		super(msg);
	}

	public CompilationException(String msg, Exception ex) {
		super(msg, ex);
	}

}
