package com.zw;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class CustomRunner extends BlockJUnit4ClassRunner {
    public CustomRunner(Class<?> klass) throws InitializationError {
        super(klass);
        System.setProperty("log4j.configurationFile", "log4j2-test.xml");
    }
}
