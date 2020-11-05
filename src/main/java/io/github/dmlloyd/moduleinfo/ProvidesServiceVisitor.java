package io.github.dmlloyd.moduleinfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 */
public class ProvidesServiceVisitor extends ServiceVisitor {
    private final Map<String, List<String>> providesNames;
    private String serviceName;

    public ProvidesServiceVisitor(final ServiceVisitor sv, final Map<String, List<String>> providesNames) {
        super(sv);
        this.providesNames = providesNames;
    }

    public ProvidesServiceVisitor(final Map<String, List<String>> providesNames) {
        this(null, providesNames);
    }

    public void visit(final String serviceName) {
        this.serviceName = serviceName;
        super.visit(serviceName);
    }

    public void visitImplementation(final String implName) {
        List<String> val = providesNames.computeIfAbsent(serviceName, ignored -> new ArrayList<>());
        val.add(implName);
        super.visitImplementation(implName);
    }
}
