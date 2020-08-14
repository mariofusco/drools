/*
 * Copyright (c) 2020. Red Hat, Inc. and/or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.mvel;

import java.util.Collections;
import java.util.Date;

import org.drools.compiler.compiler.AnalysisResult;
import org.drools.compiler.compiler.BoundIdentifiers;
import org.drools.compiler.compiler.Dialect;
import org.drools.compiler.lang.descr.BaseDescr;
import org.drools.compiler.lang.descr.BindingDescr;
import org.drools.compiler.rule.builder.RuleBuildContext;
import org.drools.compiler.rule.builder.dialect.DialectUtil;
import org.drools.core.base.CoreComponentsBuilder;
import org.drools.core.rule.Pattern;
import org.drools.core.spi.InternalReadAccessor;
import org.drools.core.spi.ObjectType;
import org.drools.mvel.builder.MVELAnalysisResult;
import org.drools.mvel.builder.MVELDialect;
import org.drools.mvel.expr.MVELCompileable;
import org.drools.mvel.extractors.MVELDateClassFieldReader;
import org.drools.mvel.extractors.MVELNumberClassFieldReader;
import org.drools.mvel.extractors.MVELObjectClassFieldReader;

public class MVELCoreComponentsBuilder implements CoreComponentsBuilder {

    @Override
    public InternalReadAccessor getReadAcessor( String className, String expr, boolean typesafe, Class<?> returnType) {
        if (Number.class.isAssignableFrom( returnType ) ||
                ( returnType == byte.class ||
                        returnType == short.class ||
                        returnType == int.class ||
                        returnType == long.class ||
                        returnType == float.class ||
                        returnType == double.class ) ) {
            return new MVELNumberClassFieldReader( className, expr, typesafe );
        } else if (  Date.class.isAssignableFrom( returnType ) ) {
            return new MVELDateClassFieldReader( className, expr, typesafe );
        } else {
            return new MVELObjectClassFieldReader( className, expr, typesafe );
        }
    }
}
