package org.drools.jboss.integration;

import org.drools.jboss.integration.example.Greeter;
import org.drools.jboss.integration.example.PhraseBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.cdi.KSession;
import org.kie.api.runtime.KieSession;

import javax.inject.Inject;
import java.io.File;

import static junit.framework.TestCase.assertNotNull;

@RunWith(Arquillian.class)
public class DroolsTest {

    @Deployment
    public static WebArchive createDeployment() {
        File[] libs = Maven.resolver()
                           .loadPomFromFile("pom.xml").resolve("org.drools:drools-compiler")
                           .withTransitivity().asFile();

        WebArchive drools = ShrinkWrap.create(WebArchive.class)
                                    .addAsLibraries(libs)
                                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        System.out.println(drools.toString(true));
        return drools;
    }

    @Inject
    @KSession("basicKSession")
    KieSession basicKieSession;

    @Test
    public void test() {
        assertNotNull(basicKieSession);
    }

}
