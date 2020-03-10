package eu.mihosoft.jcompiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public final class CompilationResult {

    private final List<CompiledUnit> compiledUnits;
    private final List<Diagnostic<? extends JavaFileObject>> diagnostics;
    private final boolean compilationSucceeded;
    private boolean hasWarnings;
    private boolean hasErrors;
    private final String compilationErrorMsg;
    

    /*pkg private*/ CompilationResult(
         List<CompiledUnit> compiledUnits,
         List<Diagnostic<? extends JavaFileObject>> diagnostics, boolean compilationSucceeded) {
            this.compiledUnits = Collections.unmodifiableList(new ArrayList<>(compiledUnits));
            this.diagnostics = Collections.unmodifiableList(new ArrayList<>(diagnostics));
            this.compilationErrorMsg = checkWarningsAndErrors();
            this.compilationSucceeded = compilationSucceeded;
    }

	/**
	 * Returns the compiled classes
	 */
	public List<CompiledUnit> getCompiledUnits() {
        return this.compiledUnits;
    }

	/**
	 * Determines if the compilation did succeed.
	 */
	public boolean compilationSucceeded() {
        return this.compilationSucceeded;
    }

	/**
	 * Determines if any warnings are present.
	 */
	public boolean hasWarnings() {
        return this.hasWarnings;
    }

	/**
	 * Determines if any errors are presents.
	 */
	public boolean hasErrors() {
        return this.hasErrors;
    }

	/**
	 * Returns the diagnostics produced by the compiler.
	 */
	public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
        return diagnostics;
    }

    /**
     * Returns error and warning messages created by the compiler.
     * @return error and warning messages created by the compiler
     */
    public String getMessage() {
        return compilationErrorMsg;
    }

    /**
     * Checks whether compilation errors occured and throws an exception if that's the case.
     * @param ignoreWarnings determines whether to ignore warnings
     * @throws CompilationException if errors occured during compilation
     */
    public CompilationResult checkNoErrors(boolean ignoreWarnings) throws CompilationException {
        if(hasErrors() || (!ignoreWarnings && hasWarnings())) {
            throw new CompilationException(getMessage());
        }

        return this;
    }

    /**
     * Checks whether compilation errors occured and throws an exception if that's the case.
     * Warnings are ignored by default.
     * @throws CompilationException if errors occured during compilation
     */
    public CompilationResult checkNoErrors() {
        return checkNoErrors(true);
    }

    private String checkWarningsAndErrors() {

        StringBuilder exceptionMsg = new StringBuilder();
        exceptionMsg.append("Unable to compile the source");

        for (Diagnostic<? extends JavaFileObject> d : this.getDiagnostics()) {
            
            switch (d.getKind()) {
            case NOTE:
            case MANDATORY_WARNING:
            case WARNING:
                this.hasWarnings = true;
                break;
            case OTHER:
            case ERROR:
            default:
                this.hasErrors = true;
                break;
            }

            exceptionMsg.append("\n").append("[location=").append(d.getSource().getName());
            exceptionMsg.append(", ").append("kind=").append(d.getKind());
            exceptionMsg.append(", ").append("line=").append(d.getLineNumber());
            exceptionMsg.append(", ").append("message=").append(d.getMessage(Locale.US)).append("]");
        }

        return exceptionMsg.toString();
    }
}