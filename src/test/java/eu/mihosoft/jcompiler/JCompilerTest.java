package eu.mihosoft.jcompiler;

import org.junit.Test;
import org.mdkt.compiler.InMemoryJavaCompiler;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class JCompilerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void compile_WhenTypical() throws ClassNotFoundException {
        StringBuffer sourceCode = new StringBuffer();

        sourceCode.append("package eu.mihosoft.jcompile;\n");
        sourceCode.append("public class HelloClass {\n");
        sourceCode.append("   public String hello() { return \"hello\"; }");
        sourceCode.append("}");

        CompilationResult result = JCompiler.newInstance().compile(sourceCode.toString());
        Assert.assertTrue("expected successful compilation", result.compilationSucceeded());
        Assert.assertFalse("expected no warnings", result.hasWarnings());
        Assert.assertFalse("expected no errors", result.hasErrors());

        Assert.assertEquals(1, result.getCompiledUnits().size());
        Assert.assertEquals(1, result.getCompiledUnits().get(0).getClasses().size());
        Assert.assertTrue(result.getCompiledUnits().get(0).getMainClass() == result.getCompiledUnits().get(0)
                .getClasses().get(0));
        Assert.assertEquals("eu.mihosoft.jcompile.HelloClass",
                result.getCompiledUnits().get(0).getClasses().get(0).getClassName());

        Class<?> helloClass = result.getCompiledUnits().get(0).getMainClass().loadClass();
        Assert.assertNotNull(helloClass);
        Assert.assertEquals(1, helloClass.getDeclaredMethods().length);
    }

    @Test
    public void compileAll_WhenTypical() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		String cls1 = "public class A{ public B b() { return new B(); }}";
		String cls2 = "public class B{ public String toString() { return \"B!\"; }}";

		CompilationResult result = JCompiler.newInstance().addSource("A", cls1).addSource("B", cls2).compileAll();

        Assert.assertEquals(false, result.getCompiledUnits().isEmpty());

        Map<String, Class<?>> compiled = result.loadClasses();

		Assert.assertNotNull(compiled.get("A"));
		Assert.assertNotNull(compiled.get("B"));

		Class<?> aClass = compiled.get("A");
		Object a = aClass.newInstance();
		Assert.assertEquals("B!", aClass.getMethod("b").invoke(a).toString());

	}

	@Test
	public void compile_WhenSourceContainsInnerClasses() throws Exception {
		StringBuffer sourceCode = new StringBuffer();

		sourceCode.append("package eu.mihosoft.jcompiler;\n");
		sourceCode.append("public class HelloClass {\n");
		sourceCode.append("   private static class InnerHelloWorld { int inner; }\n");
		sourceCode.append("   public String hello() { return \"hello\"; }");
		sourceCode.append("} class AnotherClass {}");

		Class<?> helloClass = InMemoryJavaCompiler.newInstance().compile("eu.mihosoft.jcompiler.HelloClass", sourceCode.toString());
		Assert.assertNotNull(helloClass);
		Assert.assertEquals(1, helloClass.getDeclaredMethods().length);
    }
    
    @Test
	public void compile_MainClass() throws Exception {
		StringBuffer sourceCode = new StringBuffer();

		sourceCode.append("package eu.mihosoft.jcompiler;\n");
		sourceCode.append("class HelloClass {\n");
		sourceCode.append("   private static class InnerHelloWorld { int inner; }\n");
		sourceCode.append("   public String hello() { return \"hello\"; }");
		sourceCode.append("} public class AnotherClass {public String hello1() { return \"hello1\"; } public String hello2() { return \"hello2\"; }}");

        CompilationResult result = JCompiler.newInstance().compile(sourceCode.toString()).checkNoErrors();

        assertEquals(1, result.getCompiledUnits().size());

        Class<?> anotherClass =  result.getCompiledUnits().get(0).getMainClass().loadClass();

        Assert.assertNotNull(anotherClass);
        
		Assert.assertEquals("eu.mihosoft.jcompiler.AnotherClass", result.getCompiledUnits().get(0).getMainClass().getClassName());
		Assert.assertEquals(2, anotherClass.getDeclaredMethods().length);
	}

	@Test
	public void compile_whenError() throws Exception {
		thrown.expect(CompilationException.class);
		thrown.expectMessage("Unable to compile the source");
		StringBuffer sourceCode = new StringBuffer();

		sourceCode.append("package eu.mihosoft.jcompiler;\n");
		sourceCode.append("public classHelloClass {\n");
		sourceCode.append("   public String hello() { return \"hello\"; }");
		sourceCode.append("}");
        CompilationResult result = JCompiler.newInstance().compile("eu.mihosoft.jcompiler.HelloClass", sourceCode.toString());
        
        Assert.assertFalse("compilation is expected to fail", result.compilationSucceeded());
        Assert.assertTrue("errors are expected", result.hasErrors());
        Assert.assertFalse("diagnostics expected", result.getDiagnostics().isEmpty());

        result.checkNoErrors();
	}

	@Test
	public void compile_WhenFailOnWarnings() throws Exception {
		thrown.expect(CompilationException.class);
		StringBuffer sourceCode = new StringBuffer();

		sourceCode.append("package eu.mihosoft.jcompiler;\n");
		sourceCode.append("public class HelloClass {\n");
		sourceCode.append("   public java.util.List<String> hello() { return new java.util.ArrayList(); }");
		sourceCode.append("}");
        CompilationResult result = JCompiler.newInstance().compile("eu.mihosoft.jcompiler.HelloClass", sourceCode.toString());
        
        Assert.assertTrue("warnings expected", result.hasWarnings());

        try {
            result.checkNoErrors(false);
            Assert.fail("If warnings are not ignored, we expect an exception.");
        } catch(CompilationException ex) {
            ex.printStackTrace();
        }

        try {
            result.checkNoErrors(true);
            result.checkNoErrors();
        } catch(CompilationException ex) {
            ex.printStackTrace();
            Assert.fail("If warnings are ignored, we expect no exception.");
        }

        result.checkNoErrors(false);
	}

	@Test
	public void compile_WhenIgnoreWarnings() throws Exception {
		StringBuffer sourceCode = new StringBuffer();

		sourceCode.append("package eu.mihosoft.jcompiler;\n");
		sourceCode.append("public class HelloClass {\n");
		sourceCode.append("   public java.util.List<String> hello() { return new java.util.ArrayList(java.util.Arrays.asList(\"a\")); }");
		sourceCode.append("}");
        Class<?> helloClass = JCompiler.newInstance().compile("eu.mihosoft.jcompiler.HelloClass", sourceCode.toString()).
            checkNoErrors(true).loadClasses().values().iterator().next();
		List<?> res = (List<?>) helloClass.getMethod("hello").invoke(helloClass.newInstance());
		Assert.assertEquals(1, res.size());
    }
    
    @Test
	public void miniTutorialForReadMe() throws Exception {
        CompilationResult result = JCompiler.newInstance().
        compile(
            "public class MyClass {\n"+
            "  public static String hello() {\n"+
			"    return \"hello\";\n"+
			"  }\n"+
			"}"
		).checkNoErrors();
        Class<?> myClass = result.loadClasses().get("MyClass");    
		String msg = (String)myClass.getMethod("hello").invoke(null);
		Assert.assertEquals("hello", msg);
	}

	@Test
	public void compile_WhenWarningsAndErrors() throws Exception {
		thrown.expect(CompilationException.class);
		StringBuffer sourceCode = new StringBuffer();

		sourceCode.append("package eu.mihosoft.jcompiler;\n");
		sourceCode.append("public class HelloClass extends xxx {\n");
		sourceCode.append("   public java.util.List<String> hello() { return new java.util.ArrayList(); }");
		sourceCode.append("}");
		try {
			JCompiler.newInstance().compile("eu.mihosoft.jcompiler.HelloClass", sourceCode.toString()).checkNoErrors();
		} catch (Exception e) {
			throw e;
		}
    }
}