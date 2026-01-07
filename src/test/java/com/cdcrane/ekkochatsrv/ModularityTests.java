package com.cdcrane.ekkochatsrv;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ModularityTests {

    private final ApplicationModules modules = ApplicationModules.of(EkkochatsrvApplication.class);

    @Test
    public void testModules() {

        modules.verify();

        System.out.println(modules);

    }
}
