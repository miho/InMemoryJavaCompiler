/*
 * Copyright 2020-2022 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
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

/**
 * Represents a compiled class. A compiled class can be returned as byte array as well as class object,
 * loaded by the classloader used during compilation.
 */
public final class CompiledClass implements Comparable<String> {

    // the internal representation of this compiled class
    private final CompiledClassFile file;

    /**
     * Creates a new instance of this class.
     * 
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
     * 
     * @return the name of thic class
     */
    public String getClassName() {
        return file.getClassName();
    }

    /**
     * Loads this class with the classloader used during compilation.
     * 
     * @return a class object that represents this compiled class
     * @throws ClassNotFoundException if classloading failed
     */
    public Class<?> loadClass() throws ClassNotFoundException {
        return file.loadClass();
    }

    @Override
    public int compareTo(String o) {
        return this.getClassName().compareTo(o);
    }
}