# JCompiler 
[ ![Download](https://api.bintray.com/packages/miho/JCompiler/JCompiler/images/download.svg) ](https://bintray.com/miho/JCompiler/JCompiler/_latestVersion) [![javadoc](https://javadoc.io/badge2/eu.mihosoft.jcompiler/jcompiler/javadoc.svg)](https://javadoc.io/doc/eu.mihosoft.jcompiler/jcompiler) ![Java CI with Gradle](https://github.com/miho/JCompiler/workflows/Java%20CI%20with%20Gradle/badge.svg)

The purpose of this project is to provide a simple API for in-memory compilation of Java code. While the JDK provides a full-blown API for this, in many cases a much simpler API is sufficient.

This project is based on abandoned code by [@trung](https://github.com/trung/InMemoryJavaCompiler) and a reduced version of language utils from [VRL](https://github.com/VRL-Studio/VRL).

## Using JCompiler

**Note**: This project needs the full JDK as runtime-dependency to work properly (the JRE does not contain the JavaCompiler API).

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

## How To Build The Project

### 1. Dependencies

- JDK >= 8 (tested with JDK 11 and 13)
- Internet Connection (other dependencies will be downloaded automatically)
- Optional: IDE with [Gradle](http://www.gradle.org/) support

### 2. Building

#### IDE

To build the project from an IDE do the following:

- open the  [Gradle](http://www.gradle.org/) project
- call the `assemble` Gradle task to build the project

#### Command Line

Building the project from the command line is also possible.

Navigate to the project folder and call the `assemble` [Gradle](http://www.gradle.org/)
task to build the project.

##### Bash (Linux/OS X/Cygwin/other Unix-like OS)

    cd Path/To/JCompiler
    ./gradlew assemble
    
##### Windows (CMD)

    cd Path\To\JCompiler
    gradlew assemble

