/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package memorycompiler;

import eu.mihosoft.jcompiler.CompilationResult;
import eu.mihosoft.jcompiler.CompiledClass;
import eu.mihosoft.jcompiler.CompiledUnit;
import eu.mihosoft.jcompiler.JCompiler;

public class App {

    public static void main(String[] args) throws Exception {

        JCompiler compiler = JCompiler.newInstance();

        compiler.addSource("MyClass1", "public interface MyClass1 {}; interface MyClass2 {}");
        compiler.addSource("MyClass3", "interface MyClass3 {}; interface MyClass4 {}");

        CompilationResult modelClasses = compiler.compileAll().checkNoErrors();

        for(CompiledUnit compilationUnit : modelClasses.getCompiledUnits()) {
            System.out.println("> compilation unit: " + compilationUnit.getName());
            for(CompiledClass cls : compilationUnit.getClasses()) {
                System.out.println("> out: " + cls.loadClass().getName());
            }
        }

    }
}
