package com.codelutin;

import com.codelutin.app.TestTopiaPersistanceTopiaApplicationContext;
import com.codelutin.app.TestTopiaPersistanceTopiaPersistenceContext;
import com.codelutin.app.entities.HarvestingAction;
import com.codelutin.app.entities.HarvestingActionImpl;
import com.codelutin.app.entities.HarvestingActionTopiaDao;
import com.codelutin.app.entities.HarvestingActionValorisation;
import com.codelutin.app.entities.HarvestingActionValorisationTopiaDao;
import com.codelutin.app.entities.QualityCriteriaAgre;
import com.codelutin.app.entities.QualityCriteriaAgreTopiaDao;
import com.codelutin.app.entities.QualityCriteriaAsso;
import com.codelutin.app.entities.QualityCriteriaAssoTopiaDao;
import com.codelutin.app.entities.QualityCriteriaToHAV;
import com.codelutin.app.entities.QualityCriteriaToHAVTopiaDao;
import com.codelutin.app.entities.RefQualityCriteria;
import com.codelutin.app.entities.RefQualityCriteriaTopiaDao;
import org.apache.commons.collections.CollectionUtils;
import org.h2.Driver;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.H2Dialect;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class SampleTest {

    protected static TestTopiaPersistanceTopiaApplicationContext applicationContext;

    protected static final Long TEST_RUN_ID = System.currentTimeMillis();

    protected TestTopiaPersistanceTopiaPersistenceContext persistenceContext;

    protected HarvestingActionTopiaDao harvestingActionDao;

    protected HarvestingActionValorisationTopiaDao harvestingActionValorisationDao;

    protected RefQualityCriteriaTopiaDao refQualityCriteriaDao;

    protected QualityCriteriaAgreTopiaDao qualityCriteriaAgreDao;

    protected QualityCriteriaAssoTopiaDao qualityCriteriaAssoDao;

    protected QualityCriteriaToHAVTopiaDao qualityCriteriaToHAVDao;

    @BeforeClass
    public static void createApplicationContext() {
        Properties properties = new Properties();
        properties.setProperty(Environment.DIALECT, H2Dialect.class.getName());
        properties.setProperty(Environment.DRIVER, Driver.class.getName());

        String base = "tests_" + TEST_RUN_ID;
        String result = String.format("%s", base);
        String jdbcUrl = "jdbc:h2:mem:" + result + ";DB_CLOSE_DELAY=-1";

        properties.setProperty(Environment.URL, jdbcUrl);
        properties.setProperty(Environment.USER, "sa");
        properties.setProperty(Environment.PASS, "");
        properties.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");

        properties.setProperty("topia.persistence.validateSchema", "false");

        applicationContext = new TestTopiaPersistanceTopiaApplicationContext(properties);
    }

    @AfterClass
    public static void closeApplicationContext() {
        applicationContext.close();
    }

    @Before
    public void createPersistenceContextAndDaos() {
        persistenceContext = applicationContext.newPersistenceContext();
        harvestingActionDao = persistenceContext.getHarvestingActionDao();
        harvestingActionValorisationDao = persistenceContext.getHarvestingActionValorisationDao();
        refQualityCriteriaDao = persistenceContext.getRefQualityCriteriaDao();
        qualityCriteriaAgreDao = persistenceContext.getQualityCriteriaAgreDao();
        qualityCriteriaAssoDao = persistenceContext.getQualityCriteriaAssoDao();
        qualityCriteriaToHAVDao = persistenceContext.getQualityCriteriaToHAVDao();
    }

    @After
    public void clonePersistenceContext() {
        persistenceContext.close();
    }

    protected HarvestingAction createHarvestingAction() {
        HarvestingAction harvestingAction = createActionValorisations();
        return harvestingActionDao.create(harvestingAction);
    }

    protected HarvestingAction createActionValorisations() {
        HarvestingAction harvestingAction = new HarvestingActionImpl();
        harvestingAction.setMoisturePercent(20.5);

        List<HarvestingActionValorisation> valorisations = new ArrayList<>();
        harvestingAction.setValorisations(valorisations);

        return  harvestingAction;
    }

    protected Collection<HarvestingActionValorisation> createValorisations(
            HarvestingAction persistedHarvestingAction,
            Collection<QualityCriteriaAsso> qualityCriteriaAssos,
            Collection<QualityCriteriaAgre> qualityCriteriaAgres,
            Collection<QualityCriteriaToHAV> qualityCriteriaToHAVs) {

        Collection<HarvestingActionValorisation> valorisations = persistedHarvestingAction.getValorisations();
        HarvestingActionValorisation mainVal = harvestingActionValorisationDao.newInstance();
        mainVal.setMain(true);
        mainVal.setSpeciesCode(UUID.randomUUID().toString());

        mainVal.setQualityCriteriaAsso(qualityCriteriaAssos);

        mainVal = harvestingActionValorisationDao.create(mainVal);

        if (qualityCriteriaToHAVs != null) {
            for (QualityCriteriaToHAV qualityCriteriaToHAV : qualityCriteriaToHAVs) {
                qualityCriteriaToHAV.setHarvestingActionValorisation(mainVal);
            }
        }

        valorisations.add(mainVal);

        HarvestingActionValorisation speciesVal = harvestingActionValorisationDao.newInstance();
        speciesVal.setMain(false);
        speciesVal.setSpeciesCode(UUID.randomUUID().toString());
        speciesVal = harvestingActionValorisationDao.create(speciesVal);

        valorisations.add(speciesVal);
        return valorisations;
    }


    protected void createNoQualityCriteriaValorisations(
            HarvestingAction persistedHarvestingAction) {
        createValorisations(persistedHarvestingAction, null, null, null);
    }

    protected void createQualityCriteriaAssoValorisations(
            HarvestingAction persistedHarvestingAction,
            Collection<QualityCriteriaAsso> qualityCriteriaAssos) {
        createValorisations(persistedHarvestingAction, qualityCriteriaAssos, null, null);
    }

    protected Collection<HarvestingActionValorisation> createQualityCriteriaAgreValorisations(
            HarvestingAction persistedHarvestingAction) {
        Collection<HarvestingActionValorisation> valorisations = createValorisations(persistedHarvestingAction, null, null, null);
        return valorisations;
    }

    protected void createQualityCriteriaToHAV_Valorisations(
            HarvestingAction persistedHarvestingAction,
            Collection<QualityCriteriaToHAV> qualityCriteriaToHAVs) {
        createValorisations(persistedHarvestingAction, null, null, qualityCriteriaToHAVs);

    }

    protected RefQualityCriteria createRefQualityCriteria() {
        RefQualityCriteria refQualityCriteria = refQualityCriteriaDao.newInstance();
        refQualityCriteria.setQualityCriteriaLabel("Valeur de référence");
        refQualityCriteria = refQualityCriteriaDao.create();
        return refQualityCriteria;
    }


    @Test
    public void testNoQualityCriteriaPersistance() {

        HarvestingAction harvestingAction = createHarvestingAction();
        createNoQualityCriteriaValorisations(harvestingAction);

        persistenceContext.commit();

        Collection<HarvestingAction> harvestingActions = harvestingActionDao.findAll();
        harvestingActionDao.deleteAll(harvestingActions);

        persistenceContext.commit();

    }

    @Test
    public void testAssociationQualityCriteriaPersistance() {

        HarvestingAction harvestingAction = createHarvestingAction();

        RefQualityCriteria qualityCriteria0 = createRefQualityCriteria();

        QualityCriteriaAsso binaryQualityCriteriaAsso = qualityCriteriaAssoDao.newInstance();
        binaryQualityCriteriaAsso.setBinaryValue(true);
        binaryQualityCriteriaAsso.setRefQualityCriteria(qualityCriteria0);

        binaryQualityCriteriaAsso = qualityCriteriaAssoDao.create(binaryQualityCriteriaAsso);

        QualityCriteriaAsso quantitativeQualityCriteriaAsso = qualityCriteriaAssoDao.newInstance();
        quantitativeQualityCriteriaAsso.setQuantitativeValue(20.5);
        quantitativeQualityCriteriaAsso.setRefQualityCriteria(qualityCriteria0);

        quantitativeQualityCriteriaAsso = qualityCriteriaAssoDao.create(quantitativeQualityCriteriaAsso);

        List<QualityCriteriaAsso> qualityCriteriaAssos = new ArrayList<>();
        qualityCriteriaAssos.add(binaryQualityCriteriaAsso);
        qualityCriteriaAssos.add(quantitativeQualityCriteriaAsso);

        createQualityCriteriaAssoValorisations(harvestingAction, qualityCriteriaAssos);

        persistenceContext.commit();

        List<QualityCriteriaAsso> allQualityCriteriaAsso = new ArrayList<>();

        List<HarvestingAction> harvestingActions = harvestingActionDao.findAll();

        // check every things has been persisted
        Assert.assertEquals(1, harvestingActions.size());
        HarvestingAction action = harvestingActions.get(0);
        Collection<HarvestingActionValorisation> valorisations = action.getValorisations();
        Assert.assertTrue(CollectionUtils.isNotEmpty(valorisations));
        for (HarvestingActionValorisation valorisation : valorisations) {
            if (valorisation.isMain()) {
                Collection<QualityCriteriaAsso> allQualityCriteriaAssos = valorisation.getQualityCriteriaAsso();
                Assert.assertTrue(CollectionUtils.isNotEmpty(allQualityCriteriaAssos));
                allQualityCriteriaAsso.addAll(allQualityCriteriaAssos);
            }
        }

        harvestingActionDao.deleteAll(harvestingActions);

        // as relation is association, it needs to be manually removed
        qualityCriteriaAssoDao.deleteAll(allQualityCriteriaAsso);

        persistenceContext.commit();

        Assert.assertEquals(0, qualityCriteriaAssoDao.count());
    }

    @Test
    public void testAgregateQualityCriteriaPersistance() {

        HarvestingAction harvestingAction = createHarvestingAction();

        RefQualityCriteria qualityCriteria0 = createRefQualityCriteria();

        Collection<HarvestingActionValorisation> valorisations = createQualityCriteriaAgreValorisations(harvestingAction);;
        for (HarvestingActionValorisation valorisation : valorisations) {
            if (valorisation.isMain()) {
                QualityCriteriaAgre binaryQualityCriteriaAgre = qualityCriteriaAgreDao.newInstance();
                binaryQualityCriteriaAgre.setBinaryValue(true);
                binaryQualityCriteriaAgre.setRefQualityCriteria(qualityCriteria0);
                binaryQualityCriteriaAgre.setHarvestingActionValorisation(valorisation);
                valorisation.addRefQualityCriteriaQualityCriteriaAgre(binaryQualityCriteriaAgre);
            }
        }

        persistenceContext.commit();

        List<HarvestingAction> harvestingActions = harvestingActionDao.findAll();

        Assert.assertEquals(1, harvestingActions.size());
        HarvestingAction action = harvestingActions.get(0);

        Collection<HarvestingActionValorisation> actionValorisations = action.getValorisations();
        Assert.assertTrue(CollectionUtils.isNotEmpty(actionValorisations));
        for (HarvestingActionValorisation valorisation : actionValorisations) {
            if (valorisation.isMain()) {
                Collection<QualityCriteriaAgre> valorisationQualityCriteriaAgres = valorisation.getRefQualityCriteriaQualityCriteriaAgre();
                Assert.assertTrue(CollectionUtils.isNotEmpty(valorisationQualityCriteriaAgres));
            }
        }

        harvestingActionDao.deleteAll(harvestingActions);

        persistenceContext.commit();
    }

    @Test
    public void testQualityCriteriaToHAVPersistance() {

        HarvestingAction harvestingAction = createHarvestingAction();

        QualityCriteriaToHAV binaryQualityCriteriaHAV = qualityCriteriaToHAVDao.newInstance();
        binaryQualityCriteriaHAV.setBinaryValue(true);
        binaryQualityCriteriaHAV = qualityCriteriaToHAVDao.create(binaryQualityCriteriaHAV);

        QualityCriteriaToHAV quantitativeQualityCriteriaHAV = qualityCriteriaToHAVDao.newInstance();
        quantitativeQualityCriteriaHAV.setQuantitativeValue(20.5);
        quantitativeQualityCriteriaHAV = qualityCriteriaToHAVDao.create(quantitativeQualityCriteriaHAV);

        List<QualityCriteriaToHAV> qualityCriteriaToHAVs = new ArrayList<>();
        qualityCriteriaToHAVs.add(binaryQualityCriteriaHAV);
        qualityCriteriaToHAVs.add(quantitativeQualityCriteriaHAV);

        createQualityCriteriaToHAV_Valorisations(harvestingAction, qualityCriteriaToHAVs);

        persistenceContext.commit();

        List<HarvestingAction> harvestingActions = harvestingActionDao.findAll();

        List<QualityCriteriaToHAV> qualityCriteriaToRemove = new ArrayList<>();
        // check every things has been persisted
        Assert.assertEquals(1, harvestingActions.size());
        HarvestingAction action = harvestingActions.get(0);
        Collection<HarvestingActionValorisation> valorisations = action.getValorisations();
        Assert.assertTrue(CollectionUtils.isNotEmpty(valorisations));
        for (HarvestingActionValorisation valorisation : valorisations) {
            if (valorisation.isMain()) {
                List<QualityCriteriaToHAV> qualityCriteriaToHAVs1 = qualityCriteriaToHAVDao.forHarvestingActionValorisationEquals(valorisation).findAll();
                Assert.assertTrue(CollectionUtils.isNotEmpty(qualityCriteriaToHAVs1));
                qualityCriteriaToRemove.addAll(qualityCriteriaToHAVs1);
            }
        }
        qualityCriteriaToHAVDao.deleteAll(qualityCriteriaToRemove);

        harvestingActionDao.deleteAll(harvestingActions);

        persistenceContext.commit();

    }

}