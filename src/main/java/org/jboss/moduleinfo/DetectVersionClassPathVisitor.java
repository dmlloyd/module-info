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

import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 */
public class DetectVersionClassPathVisitor extends ClassPathVisitor {
    private final Item<String> versionItem;

    public DetectVersionClassPathVisitor(final ClassPathVisitor cpv, final Item<String> versionItem) {
        super(cpv);
        this.versionItem = versionItem;
    }

    public DetectVersionClassPathVisitor(final Item<String> versionItem) {
        this(null, versionItem);
    }

    public ManifestVisitor visitManifest() {
        final ManifestVisitor mv = super.visitManifest();
        // check the manifest
        return new ManifestVisitor(mv) {
            public void visit(final Manifest manifest) {
                final Object version = manifest.getMainAttributes().get(Attributes.Name.IMPLEMENTATION_VERSION);
                if (version != null) {
                    versionItem.set(version.toString());
                }
                super.visit(manifest);
            }
        };
    }
}
