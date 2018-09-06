/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
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

package org.jboss.moduleinfo;

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
