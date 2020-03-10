package eu.mihosoft.jcompiler;

import java.util.Collections;
import java.util.List;

public final class CompiledUnit {
    private final String name;
    private final List<CompiledClass> classes;
     
    /*pkg private*/ CompiledUnit(String name, List<CompiledClass> classes) {
        this.name = name;
        this.classes = Collections.unmodifiableList(classes);
    } 

    /**
     * @return the compiled classes contained in this unit
     */
    public List<CompiledClass> getClasses() {
        return classes;
    }

    /**
     * @return the name of this compiled unit
     */
    public String getName() {
        return name;
    }

}