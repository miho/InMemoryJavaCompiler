package eu.mihosoft.jcompiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Result of a compilation attempt. The result contains compiled compilation units as well as diagnostics
 * provided by the compiler (including success, warnings and errors).
 */
public final class CompilationResult {

    private final List<CompiledUnit> compiledUnits;
    private final List<Diagnostic<? extends JavaFileObject>> diagnostics;
    private final boolean compilationSucceeded;
    private boolean hasWarnings;
    private boolean hasErrors;
    private final String compilationErrorMsg;
    

    /**
     * Creates a new instance of this class.
     * @param compiledUnits compiled compilation units
     * @param diagnostics diagnostics collected during compilation
     * @param compilationSucceeded determines if the compilation attempt was successful
     */
    /*pkg private*/ CompilationResult(
         List<CompiledUnit> compiledUnits,
         List<Diagnostic<? extends JavaFileObject>> diagnostics, boolean compilationSucceeded) {
            this.compiledUnits = Collections.unmodifiableList(new ArrayList<>(compiledUnits));
            this.diagnostics = Collections.unmodifiableList(new ArrayList<>(diagnostics));
            this.compilationErrorMsg = checkWarningsAndErrors();
            this.compilationSucceeded = compilationSucceeded;
    }

	/**
	 * Returns the compiled compilation units.
     * @return list of compiled compilation units
	 */
	public List<CompiledUnit> getCompiledUnits() {
        return this.compiledUnits;
    }

	/**
	 * Determines if the compilation was successful.
     * @return {@code true} if compilation was successful; {@code false} otherwise
	 */
	public boolean compilationSucceeded() {
        return this.compilationSucceeded;
    }

	/**
	 * Determines if the compiler produced any warnings during the compilation attempt.
     * @return {@code true} if warnings are present; {@code false} otherwise
	 */
	public boolean hasWarnings() {
        return this.hasWarnings;
    }

	/**
	 * Determines if the compiler produced any errors during the compilation attempt.
     * @return {@code true} if errors are present; {@code false} otherwise
	 */
	public boolean hasErrors() {
        return this.hasErrors;
    }

	/**
	 * Returns the diagnostics produced by the compiler.
     * @return list of diagnostics produced by the compiler
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

    /**
     * Checks this result for warnings and errors.
     * @return human readable version of the diagnostics (e.g. warnings and errors)
     */
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

            exceptionMsg.append("\n").append("[location=").append(d.getSource()!=null?d.getSource().getName():"UNKNOWN");
            exceptionMsg.append(", ").append("kind=").append(d.getKind());
            exceptionMsg.append(", ").append("line=").append(d.getLineNumber());
            exceptionMsg.append(", ").append("message=").append(d.getMessage(Locale.US)).append("]");
        }

        return exceptionMsg.toString();
    }

    /**
     * Loads all classes obtained from this compilation attempt.
     * @return all classes obtained from this compilation attempt by name
     * @throws ClassNotFoundException if classloading failed
     */
    public Map<String,Class<?>> loadClasses() throws ClassNotFoundException {
        Map<String, Class<?>> classesByName = new HashMap<>();

        List<CompiledClass> classes = compiledUnits.stream().flatMap(cu->cu.getClasses().stream()).collect(Collectors.toList());

        for(CompiledClass cc : classes) {
            classesByName.put(cc.getClassName(), cc.loadClass());
        }

        return classesByName;
    } 
}