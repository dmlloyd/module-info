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

import java.nio.file.Path;

/**
 */
public class ClassPathVisitor {
    protected final ClassPathVisitor cpv;

    public ClassPathVisitor(final ClassPathVisitor cpv) {
        this.cpv = cpv;
    }

    public ClassPathVisitor() {
        this(null);
    }

    public void visit(Path classPathRoot) {
        if (cpv != null) {
            cpv.visit(classPathRoot);
        }
    }

    public ManifestVisitor visitManifest() {
        if (cpv != null) {
            return cpv.visitManifest();
        }
        return null;
    }

    public PackageVisitor visitPackage() {
        if (cpv != null) {
            return cpv.visitPackage();
        }
        return null;
    }

    public ServiceVisitor visitService() {
        if (cpv != null) {
            return cpv.visitService();
        }
        return null;
    }
}
