package eu.mihosoft.jcompiler;

/**
 * Indicates a compilation error.
 */
public class CompilationException extends RuntimeException {
	private static final long serialVersionUID = 5272588827551900536L;

	/**
	 * Creates a new instance of this class.
	 * @param msg message of this exception
	 */
	public CompilationException(String msg) {
		super(msg);
	}

	/**
	 *  Creates a new instance of this class.
	 * @param msg message of this exception
	 * @param ex exception that caused this compilation exception
	 */
	public CompilationException(String msg, Exception ex) {
		super(msg, ex);
	}

}
