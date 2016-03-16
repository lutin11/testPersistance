package com.company.app;

import java.util.Map;
import java.util.Properties;

/**
 * A class to add some methods on this project's {@link org.nuiton.topia.persistence.TopiaApplicationContext}.
 */
public class MyLibraryTopiaApplicationContext extends AbstractMyLibraryTopiaApplicationContext {

    public MyLibraryTopiaApplicationContext(Properties properties) {
        super(properties);
    }

    public MyLibraryTopiaApplicationContext(Map<String, String> configuration) {
        super(configuration);
    }

    public void doSomethingOnThisApplicationContext() {
        MyLibraryTopiaPersistenceContext myLibraryTopiaPersistenceContext = newPersistenceContext();
        myLibraryTopiaPersistenceContext.doSomethingOnThisPersistenceContext();
        myLibraryTopiaPersistenceContext.close();
    }

}
