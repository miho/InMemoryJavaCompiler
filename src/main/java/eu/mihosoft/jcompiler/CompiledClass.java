package eu.mihosoft.jcompiler;

/**
 * Represents a compiled class. A compiled class can be returned as byte array as well as class object,
 * loaded by the classloader used during compilation.
 */
public final class CompiledClass {

    // the internal representation of this compiled class
    private final CompiledClassFile file;

    /**
     * Creates a new instance of this class
     * @param file the CompiledClassFile to use
     */
    /*pkg private*/ CompiledClass(CompiledClassFile file) {
        this.file = file;
    }

    /**
     * Returns this class as binary byte code.
     * 
     * @return the byte code of this class
     */
    public byte[] getByteCode() {
        return file.getByteCode();
    }

    /**
     * Returns the name of thic class.
     * @return the name of thic class
     */
    public String getClassName() {
        return file.getClassName();
    }

    /**
     * Loads this class with the classloader used during compilation.
     * @return a class object that represents this compiled class
     * @throws ClassNotFoundException if classloading failed
     */
    public Class<?> loadClass() throws ClassNotFoundException {
        return file.loadClass();
    }
}