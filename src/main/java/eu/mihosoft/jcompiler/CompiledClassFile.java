package eu.mihosoft.jcompiler;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A compiled class.
 */
public final class CompiledClassFile extends SimpleJavaFileObject {
    private final ClassLoader loader;
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final String className;
    private Class<?> loadedClass;

    /*pkg private*/ CompiledClassFile(ClassLoader loader, String className) throws URISyntaxException {
        super(new URI(className), Kind.CLASS);
        this.loader = loader;
        this.className = className;
    }
    
    /**
     * Returns the full name of this class (including package name).
     * @return the full name of this class
     */
    public String getClassName() {
		return className;
	}

    @Override
    public OutputStream openOutputStream() throws IOException {
        return baos;
    }

    /**
     * Returns the 
     * @return
     */
    public byte[] getByteCode() {
        return baos.toByteArray();
    }

    public Class<?> loadClass() throws ClassNotFoundException {
        if(this.loadedClass!=null) {
            return this.loadedClass;
        }

        this.loadedClass = loader.loadClass(className);

        return this.loadedClass;
    }

    /*pkg private*/ boolean hasCachedClass() {
        return this.loadedClass!= null;
    }

    /*pkg private*/ Class<?> getCachedClass() {
        return this.loadedClass;
    }
}
