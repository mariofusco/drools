/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.compiler.integrationtests;

import org.drools.core.common.DefaultAgenda;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.conf.MultithreadEvaluationOption;
import org.kie.internal.utils.KieHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParallelEvaluationTest {

    @Test(timeout = 10000L)
    public void test() {
        StringBuilder sb = new StringBuilder( 400 );
        sb.append( "global java.util.List list;\n" );
        for (int i = 0; i < 10; i++) {
            sb.append( getRule( i, "" ) );
        }

        KieSession ksession = new KieHelper().addContent( sb.toString(), ResourceType.DRL )
                                             .build( MultithreadEvaluationOption.YES )
                                             .newKieSession();

        List<Integer> list = new DebugList<Integer>();
        ksession.setGlobal( "list", list );

        for (int i = 0; i < 10; i++) {
            ksession.insert( i );
            ksession.insert( "" + i );
        }

        ksession.fireAllRules();

        assertEquals(10, list.size());
    }

    @Test(timeout = 10000L)
    public void testWithInsertions() {
        StringBuilder sb = new StringBuilder( 4000 );
        sb.append( "global java.util.List list;\n" );
        int ruleNr = 200;

        for (int i = 0; i < ruleNr; i++) {
            sb.append( getRule( i, "insert( $i + 10 );\ninsert( \"\" + ($i + 10) );\n" ) );
        }

        KieSession ksession = new KieHelper().addContent( sb.toString(), ResourceType.DRL )
                                             .build( MultithreadEvaluationOption.YES )
                                             .newKieSession();

        List<Integer> list = new DebugList<Integer>();
        ksession.setGlobal( "list", list );

        for (int i = 0; i < 10; i++) {
            ksession.insert( i );
            ksession.insert( "" + i );
        }

        ksession.fireAllRules();

        assertEquals(ruleNr, list.size());
    }

    @Test(timeout = 10000L)
    public void testWithDeletes() {
        StringBuilder sb = new StringBuilder( 400 );
        sb.append( "global java.util.List list;\n" );
        for (int i = 1; i < 11; i++) {
            sb.append( getRule( i, "delete( $i );\n" ) );
        }
        for (int i = 1; i < 11; i++) {
            sb.append( getNotRule( i ) );
        }

        KieSession ksession = new KieHelper().addContent( sb.toString(), ResourceType.DRL )
                                             .build( MultithreadEvaluationOption.YES )
                                             .newKieSession();

        List<Integer> list = new DebugList<Integer>();
        ksession.setGlobal( "list", list );

        for (int i = 1; i < 11; i++) {
            ksession.insert( i );
            ksession.insert( "" + i );
        }

        ksession.fireAllRules();

        assertEquals(20, list.size());
    }

    @Test(timeout = 10000L)
    public void testWithAsyncInsertions() {
        StringBuilder sb = new StringBuilder( 4000 );
        sb.append( "global java.util.List list;\n" );
        int ruleNr = 200;

        for (int i = 0; i < ruleNr; i++) {
            sb.append( getRule( i, "insertAsync( $i + 10 );\ninsertAsync( \"\" + ($i + 10) );\n" ) );
        }

        KieSession ksession = new KieHelper().addContent( sb.toString(), ResourceType.DRL )
                                             .build( MultithreadEvaluationOption.YES )
                                             .newKieSession();

        StatefulKnowledgeSessionImpl session = (StatefulKnowledgeSessionImpl) ksession;

        List<Integer> list = new DebugList<Integer>();
        ksession.setGlobal( "list", list );

        for (int i = 0; i < 10; i++) {
            session.insertAsync( i );
            session.insertAsync( "" + i );
        }

        ksession.fireAllRules();

        assertEquals(ruleNr, list.size());
    }

    private String getRule(int i, String rhs) {
        return  "rule R" + i + " when\n" +
                "    $i : Integer( intValue == " + i + " )" +
                "    String( toString == $i.toString )\n" +
                "then\n" +
                "    list.add($i);\n" +
                rhs +
                "end\n";
    }

    private String getNotRule(int i) {
        return  "rule Rnot" + i + " when\n" +
                "    String( toString == \"" + i + "\" )\n" +
                "    not Integer( intValue == " + i + " )" +
                "then\n" +
                "    list.add(" + -i + ");\n" +
                "end\n";
    }

    public static class DebugList<T> extends ArrayList<T> {
        Consumer<DebugList<T>> onItemAdded;

        @Override
        public synchronized boolean add( T t ) {
            System.out.println( Thread.currentThread() + " adding " + t );
            boolean result = super.add( t );
            if (onItemAdded != null) {
                onItemAdded.accept( this );
            }
            return result;
        }
    }

    @Test(timeout = 10000L)
    public void testFireUntilHalt() {
        StringBuilder sb = new StringBuilder( 400 );
        sb.append( "global java.util.List list;\n" );
        for (int i = 0; i < 10; i++) {
            sb.append( getRule( i, "" ) );
        }

        KieSession ksession = new KieHelper().addContent( sb.toString(), ResourceType.DRL )
                                             .build( MultithreadEvaluationOption.YES )
                                             .newKieSession();

        CountDownLatch done = new CountDownLatch(1);

        DebugList<Integer> list = new DebugList<Integer>();
        list.onItemAdded = ( l -> { if (l.size() == 10) {
            ksession.halt();
            done.countDown();
        }} );
        ksession.setGlobal( "list", list );

        new Thread( () -> ksession.fireUntilHalt() ).start();

        for (int i = 0; i < 10; i++) {
            ksession.insert( i );
            ksession.insert( "" + i );
        }

        try {
            done.await();
        } catch (InterruptedException e) {
            throw new RuntimeException( e );
        }

        assertEquals(10, list.size());
    }

    @Test(timeout = 10000L)
    public void testFireUntilHalt2() {
        int rulesNr = 4;
        int factsNr = 1;
        int fireNr = rulesNr * factsNr;

        String drl = "import " + A.class.getCanonicalName() + ";\n" +
                     "import " + B.class.getCanonicalName() + ";\n" +
                     "global java.util.concurrent.atomic.AtomicInteger counter\n" +
                     "global java.util.concurrent.CountDownLatch done\n" +
                     "global java.util.List list;\n";

        for (int i = 0; i < rulesNr; i++) {
            drl += getFireUntilHaltRule(fireNr, i);
        }

        KieBase kbase = new KieHelper().addContent( drl, ResourceType.DRL )
                                       .build( MultithreadEvaluationOption.YES );

        for (int loop = 0; loop < 10; loop++) {
            System.out.println("Starting loop " + loop);
            KieSession ksession = kbase.newKieSession();

            CountDownLatch done = new CountDownLatch( 1 );
            ksession.setGlobal( "done", done );

            AtomicInteger counter = new AtomicInteger( 0 );
            ksession.setGlobal( "counter", counter );

            List<String> list = new DebugList<String>();
            ksession.setGlobal( "list", list );

            new Thread( () -> {
                ksession.fireUntilHalt();
            } ).start();

            A a = new A( rulesNr + 1 );
            ksession.insert( a );

            for ( int i = 0; i < factsNr; i++ ) {
                ksession.insert( new B( rulesNr + i + 3 ) );
            }

            try {
                done.await();
            } catch (InterruptedException e) {
                throw new RuntimeException( e );
            }

            assertEquals( fireNr, counter.get() );
            ksession.dispose();
            System.out.println("Loop " + loop + " terminated");
        }
    }

    private String getFireUntilHaltRule(int fireNr, int i) {
        return  "rule R" + i + " when\n" +
                "  A( $a : value > " + i + ")\n" +
                "  B( $b : value > $a )\n" +
                "then\n" +
                "  list.add( drools.getRule().getName() );" +
                "  if (counter.incrementAndGet() == " + fireNr + " ) {\n" +
                "    drools.halt();\n" +
                "    done.countDown();\n" +
                "  }\n" +
                "end\n";
    }

    public static class A {
        private int value;

        public A( int value ) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void setValue( int value ) {
            this.value = value;
        }
    }

    public static class B {
        private int value;

        public B( int value ) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void setValue( int value ) {
            this.value = value;
        }
    }

    @Test(timeout = 10000L)
    public void testFireUntilHaltWithAsyncInsert() {
        StringBuilder sb = new StringBuilder( 400 );
        sb.append( "global java.util.List list;\n" );
        for (int i = 0; i < 10; i++) {
            sb.append( getRule( i, "" ) );
        }

        KieSession ksession = new KieHelper().addContent( sb.toString(), ResourceType.DRL )
                                             .build( MultithreadEvaluationOption.YES )
                                             .newKieSession();

        StatefulKnowledgeSessionImpl session = (StatefulKnowledgeSessionImpl) ksession;

        CountDownLatch done = new CountDownLatch(1);

        DebugList<Integer> list = new DebugList<Integer>();
        list.onItemAdded = ( l -> { if (l.size() == 10) {
            ksession.halt();
            done.countDown();
        }} );
        ksession.setGlobal( "list", list );

        new Thread( () -> ksession.fireUntilHalt() ).start();

        for (int i = 0; i < 10; i++) {
            session.insertAsync( i );
            session.insertAsync( "" + i );
        }

        try {
            done.await();
        } catch (InterruptedException e) {
            throw new RuntimeException( e );
        }

        assertEquals(10, list.size());
    }

    @Test
    public void testDisableParallelismOnSinglePartition() {
        String drl =
                "rule R1 when\n" +
                "    $i : Integer( this == 4 )" +
                "    String( length > $i )\n" +
                "then end \n" +
                "rule R2 when\n" +
                "    $i : Integer( this == 4 )" +
                "    String( length == $i )\n" +
                "then end \n" +
                "rule R3 when\n" +
                "    $i : Integer( this == 4 )" +
                "    String( length < $i )\n" +
                "then end \n";

        KieSession ksession = new KieHelper().addContent( drl, ResourceType.DRL )
                                             .build( MultithreadEvaluationOption.YES )
                                             .newKieSession();

        StatefulKnowledgeSessionImpl session = (StatefulKnowledgeSessionImpl) ksession;

        // since there is only one partition the multithread evaluation should be disabled and run with the DefaultAgenda
        assertTrue( session.getAgenda() instanceof DefaultAgenda );
    }
}
