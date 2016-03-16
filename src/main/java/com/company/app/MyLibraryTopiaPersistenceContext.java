package com.company.app;

import org.nuiton.topia.persistence.internal.AbstractTopiaPersistenceContextConstructorParameter;

/**
 * A class to add some methods on this project's {@link org.nuiton.topia.persistence.TopiaPersistenceContext}.
 */
public class MyLibraryTopiaPersistenceContext extends AbstractMyLibraryTopiaPersistenceContext {

    public MyLibraryTopiaPersistenceContext(AbstractTopiaPersistenceContextConstructorParameter parameter) {
        super(parameter);
    }

    public void doSomethingOnThisPersistenceContext() {
        commit();
    }
}
