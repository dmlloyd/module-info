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

/**
 */
public class ClassPathsVisitor {
    protected final ClassPathsVisitor cpv;

    public ClassPathsVisitor(final ClassPathsVisitor cpv) {
        this.cpv = cpv;
    }

    public ClassPathsVisitor() {
        this(null);
    }

    public ClassPathVisitor visitClassPath() {
        if (cpv != null) {
            return cpv.visitClassPath();
        }
        return null;
    }
}
