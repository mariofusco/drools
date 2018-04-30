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

package org.drools.modelcompiler.exchange;

import org.drools.model.Model;
import org.drools.model.Rule;
import org.drools.model.impl.Exchange;
import org.drools.model.impl.ModelImpl;
import org.drools.modelcompiler.ReteDumper;
import org.drools.modelcompiler.builder.KieBaseBuilder;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;

import static org.drools.model.DSL.exchangeOf;
import static org.drools.model.DSL.on;
import static org.drools.model.PatternDSL.receive;
import static org.drools.model.PatternDSL.rule;
import static org.drools.model.PatternDSL.send;

public class SendReceiveTest {

    @Test
    public void testAsync() {
        Exchange<String> exchange = exchangeOf( String.class );

        Rule rule = rule( "async" )
                .build(
                        send(exchange).message( () -> {
                            try {
                                Thread.sleep(1_000L);
                            } catch (InterruptedException e) {
                                throw new RuntimeException( e );
                            }
                            return "Hello World!";
                        } ),

                        receive(exchange).expr(s -> s.length() > 10),

                        on(exchange).execute(s -> System.out.println( "received long message: " + s))
                );

        Model model = new ModelImpl().addRule( rule );
        KieBase kieBase = KieBaseBuilder.createKieBaseFromModel( model );

        KieSession ksession = kieBase.newKieSession();

        ReteDumper.dumpRete( ksession );

        new Thread( () -> ksession.fireUntilHalt() ).start();

        try {
            Thread.sleep( 2_000L );
        } catch (InterruptedException e) {
            throw new RuntimeException( e );
        }

        ksession.halt();
        ksession.dispose();
    }
}
