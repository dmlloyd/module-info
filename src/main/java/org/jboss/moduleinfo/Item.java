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

class Item<T> {
    private T item;

    public Item() {
    }

    public Item(final T item) {
        this.item = item;
    }

    public T get() {
        return item;
    }

    public T getOrDefault(final T defVal) {
        final T item = this.item;
        return item == null ? defVal : item;
    }

    public void set(final T item) {
        this.item = item;
    }
}
