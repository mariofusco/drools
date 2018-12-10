/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.dynamic.common;

public class ProjectClassLoaderFactory {
    private static ProjectClassLoaderSupplier supplier = initSupplier();

    static ProjectClassLoader create(ClassLoader parent, ResourceProvider resourceProvider) {
        return supplier.create(parent, resourceProvider);
    }

    private static ProjectClassLoaderSupplier initSupplier() {
        try {
            return (ProjectClassLoaderSupplier) Class.forName("org.drools.dynamic.common.DynamicProjectClassLoaderSupplier").newInstance();
        } catch (Exception e1) {
            try {
                return (ProjectClassLoaderSupplier) Class.forName("org.drools.statics.common.StaticProjectClassLoaderSupplier").newInstance();
            } catch (Exception e2) {
                throw new RuntimeException( e1 );
            }
        }
    }
}
