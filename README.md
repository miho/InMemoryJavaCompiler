This project is based on abandoned code by [@trung](https://github.com/trung/InMemoryJavaCompiler).

**Work in Progress**

The purpose of this project is to provide a simple API for in-memory compilation of Java code. While the JDK provides a full-blown API for this, in many cases a much simpler API is sufficient.

Compiling Java classes is as simple as this:

```java
// compile code
CompilationResult result = JCompiler.newInstance().
compile(
    "public class MyClass {\n"+
    "  public static String hello() {\n"+
    "    return \"hello\";\n"+
    "  }\n"+
    "}"
).checkNoErrors();
// load class and invoke static method
Class<?> myClass = result.loadClasses().get("MyClass");    
String msg = (String)myClass.getMethod("hello").invoke(null);          
```

**Note**: This project needs the full JDK as runtime-dependency to work properly (the JRE does not contain the JavaCompiler API).

