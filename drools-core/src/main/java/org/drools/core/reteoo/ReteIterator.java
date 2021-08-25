/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.core.reteoo;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Consumer;

import org.drools.core.common.BaseNode;

public class ReteIterator {

    public static void traverseRete(Rete rete, Consumer<BaseNode> consumer) {
        for (EntryPointNode entryPointNode : rete.getEntryPointNodes().values()) {
            visitNode( entryPointNode, createIdentitySet(), consumer);
        }
    }

    private static <T> Set<T> createIdentitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    private static void visitNode(BaseNode node, Set<BaseNode> visitedNodes, Consumer<BaseNode> consumer ) {
        if (!visitedNodes.add( node )) {
            return;
        }
        consumer.accept( node );
        Sink[] sinks = node.getSinks();
        if (sinks != null) {
            for (Sink sink : sinks) {
                if (sink instanceof BaseNode) {
                    visitNode( ( BaseNode ) sink, visitedNodes, consumer );
                }
            }
        }
    }
}
