package com.company.app;

import com.google.common.base.Optional;
import org.nuiton.topia.persistence.TopiaEntity;
import org.nuiton.topia.persistence.internal.AbstractTopiaDao;

/**
 * A class to show that we can create methods that are available on all DAO.
 *
 * @param <E> all subclasses are DAO and each DAO is typed for the entity class E.
 */
public abstract class AbstractMyLibraryTopiaDao<E extends TopiaEntity> extends AbstractTopiaDao<E> {

    public Optional<E> findLastlyCreatedEntity() {
        return newQueryBuilder().setOrderByArguments(TopiaEntity.PROPERTY_TOPIA_CREATE_DATE).tryFindFirst();
    }

}
