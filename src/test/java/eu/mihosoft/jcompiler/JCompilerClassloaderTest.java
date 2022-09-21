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

import org.junit.Assert;
import org.junit.Test;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Test for verifying classloader behavior (parent url classloader).
 * Based on Repro sketch by @treimers.
 */
public class JCompilerClassloaderTest {
    private static final String CLASS_NAME = "eu.mihosoft.vmf.core.Container";
    private static final String VMF_JAR = "build/vmf-0.2.7.14.jar";

    @Test
    public void testClassloader() throws MalformedURLException {
        JCompiler compiler = JCompiler.newInstance();
        System.out.println("Part 0: Verifying that class is not in standard classpath.");
        try {
            ClassLoader classLoader = JCompiler.class.getClassLoader();
            classLoader.loadClass(CLASS_NAME);
            Assert.fail("Failure, class " + CLASS_NAME + " is in standard class path, please remove and run test again.");
        } catch (ClassNotFoundException e) {
            System.out.println("Fine, class " + CLASS_NAME + " is not in standard class path.");
        }
        System.out.println();
        File file = new File(VMF_JAR);
        URL url = file.toURI().toURL();
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { url }, JCompilerClassloaderTest.class.getClassLoader());
        System.out.println("Part 1: Trying to load a class with url classloader.");
        try {
            Class<?> cl = urlClassLoader.loadClass(CLASS_NAME);
            System.out.println("Fine, class loaded: " + cl.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Assert.fail("Classloading must not fail.");
        }
        System.out.println();
        System.out.println("Part 2: Trying to compile a class with url classloader");
        compiler.setParentClassLoader(urlClassLoader);
        CompilationResult result = compiler.compile("MyClass", "package eu.mihosoft.vmfmodel;\n"
                + "import eu.mihosoft.vmf.core.Container;\n"
                + "import eu.mihosoft.vmf.core.Contains;\n"
                + "\n"
                + "interface Parent {\n"
                + "\n"
                + "    @Contains(opposite = \"parent\")\n"
                + "    Child[] getChildren();\n"
                + "\n"
                + "    String getName();\n"
                + "}\n"
                + "\n"
                + "interface Child {\n"
                + "    @Container(opposite=\"children\")\n"
                + "    Parent getParent();\n"
                + "    \n"
                + "    int getValue();\n"
                + "}");
        if (result.hasErrors()) {
            System.out.println(result.getMessage());
            System.out.println();
            List<Diagnostic<? extends JavaFileObject>> diagnostics = result.getDiagnostics();
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
                System.out.println("message: "
                        + diagnostic.getMessage(Locale.getDefault()));
                System.out.println("line: "
                        + diagnostic.getLineNumber());
                System.out.println("column: "
                        + diagnostic.getColumnNumber());
                System.out.println("kind: "
                        + diagnostic.getKind());
                JavaFileObject source = diagnostic.getSource();
                System.out.println("name: "
                        + source.getName());
                System.out.println("source: "
                        + source);
                System.out.println("code: "
                        + diagnostic.getCode());
                System.out.println();
            }

            Assert.fail(result.getMessage());
        } else {
            System.out.println("Fine: Class compiled successfully.");
            List<CompiledUnit> compiledUnits = result.getCompiledUnits();
            for (CompiledUnit compiledUnit : compiledUnits) {
                System.out.println("Source: "
                        + compiledUnit.getName());
            }
            try {
                Map<String, Class<?>> classes = result.loadClasses();
                System.out.println("Number of classes: " + classes.size());
                for (String key : classes.keySet()) {
                    System.out.println(" -> class: " + key);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}

