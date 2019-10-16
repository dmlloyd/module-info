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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 */
public class ProvidesAddingModuleVisitor extends ModuleVisitor {
    private static final String[] NO_STRINGS = new String[0];

    private final Map<String, List<String>> providesNames;
    private final Map<String, Set<String>> added = new HashMap<>();

    public ProvidesAddingModuleVisitor(final ModuleVisitor mv, final Map<String, List<String>> providesNames) {
        super(Opcodes.ASM7, mv);
        this.providesNames = providesNames;
    }

    public void visitProvide(final String service, final String... providers) {
        final Set<String> set = added.computeIfAbsent(service, ignored -> new HashSet<>());
        Collections.addAll(set, providers);
        super.visitProvide(service, providers);
    }

    public void visitEnd() {
        final Logger logger = Logger.getLogger();
        for (Map.Entry<String, List<String>> entry : providesNames.entrySet()) {
            final String serviceName = entry.getKey();
            final Set<String> addedSet = added.getOrDefault(serviceName, Collections.emptySet());
            final List<String> srcList = entry.getValue();
            final List<String> providersList = new ArrayList<>(srcList.size());
            for (String implName : srcList) {
                if (! addedSet.contains(implName)) {
                    logger.info("Automatically adding provide for service \"%s\" with implementation \"%s\"", serviceName.replace('/', '.'), implName.replace('/', '.'));
                    providersList.add(implName);
                }
            }
            super.visitProvide(serviceName, providersList.toArray(NO_STRINGS));
        }
        super.visitEnd();
    }
}
