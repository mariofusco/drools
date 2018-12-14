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

import org.drools.reflective.util.ByteArrayClassLoader;

public class ClassLoaderFactory {
    private static ClassLoaderSupplier supplier = initSupplier();

    public static ProjectClassLoader createProjectClassLoader( ClassLoader parent, ResourceProvider resourceProvider ) {
        return supplier.createProjectClassLoader(parent, resourceProvider);
    }

    public static ByteArrayClassLoader createByteArrayClassLoader( ClassLoader parent ) {
        return supplier.createByteArrayClassLoader(parent);
    }

    private static ClassLoaderSupplier initSupplier() {
        try {
            return ( ClassLoaderSupplier ) Class.forName("org.drools.dynamic.common.DynamicClassLoaderSupplier").newInstance();
        } catch (Exception e1) {
            try {
                return ( ClassLoaderSupplier ) Class.forName("org.drools.statics.common.StaticClassLoaderSupplier").newInstance();
            } catch (Exception e2) {
                throw new RuntimeException( e1 );
            }
        }
    }
}
