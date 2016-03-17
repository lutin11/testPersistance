package com.codelutin;

import com.codelutin.app.TestTopiaPersistanceTopiaApplicationContext;
import com.codelutin.app.TestTopiaPersistanceTopiaPersistenceContext;
import com.codelutin.app.entities.HarvestingAction;
import com.codelutin.app.entities.HarvestingActionImpl;
import com.codelutin.app.entities.HarvestingActionTopiaDao;
import com.codelutin.app.entities.HarvestingActionValorisation;
import com.codelutin.app.entities.HarvestingActionValorisationTopiaDao;
import com.codelutin.app.entities.HarvestingYealdTopiaDao;
import com.codelutin.app.entities.QualityCriteriaAgre;
import com.codelutin.app.entities.QualityCriteriaAgreTopiaDao;
import com.codelutin.app.entities.QualityCriteriaAsso;
import com.codelutin.app.entities.QualityCriteriaAssoTopiaDao;
import com.codelutin.app.entities.QualityCriteriaManualAgre;
import com.codelutin.app.entities.QualityCriteriaManualAgreTopiaDao;
import com.codelutin.app.entities.QualityCriteriaToHAV;
import com.codelutin.app.entities.QualityCriteriaToHAVTopiaDao;
import com.codelutin.app.entities.RefDestination;
import com.codelutin.app.entities.RefDestinationTopiaDao;
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
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class SampleTest {

    protected static TestTopiaPersistanceTopiaApplicationContext applicationContext;

    protected TestTopiaPersistanceTopiaPersistenceContext persistenceContext;

    protected HarvestingActionTopiaDao harvestingActionDao;

    protected HarvestingActionValorisationTopiaDao harvestingActionValorisationDao;

    protected RefQualityCriteriaTopiaDao refQualityCriteriaDao;

    protected QualityCriteriaAgreTopiaDao qualityCriteriaAgreDao;

    protected QualityCriteriaAssoTopiaDao qualityCriteriaAssoDao;

    protected QualityCriteriaToHAVTopiaDao qualityCriteriaToHAVDao;

    protected QualityCriteriaManualAgreTopiaDao qualityCriteriaManualDao;

    protected RefDestinationTopiaDao refDestinationDao;

    protected HarvestingYealdTopiaDao harvestingYealdDao;

    public static void createApplicationContext() {
        Properties properties = new Properties();
        properties.setProperty(Environment.DIALECT, H2Dialect.class.getName());
        properties.setProperty(Environment.DRIVER, Driver.class.getName());

        String base = "tests_" + System.currentTimeMillis();
        String result = String.format("%s", base);
        String jdbcUrl = "jdbc:h2:mem:" + result + ";DB_CLOSE_DELAY=-1";
        properties.setProperty(Environment.SHOW_SQL, "true");
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
        createApplicationContext();
        persistenceContext = applicationContext.newPersistenceContext();
        harvestingActionDao = persistenceContext.getHarvestingActionDao();
        harvestingActionValorisationDao = persistenceContext.getHarvestingActionValorisationDao();
        refQualityCriteriaDao = persistenceContext.getRefQualityCriteriaDao();
        qualityCriteriaAgreDao = persistenceContext.getQualityCriteriaAgreDao();
        qualityCriteriaAssoDao = persistenceContext.getQualityCriteriaAssoDao();
        qualityCriteriaToHAVDao = persistenceContext.getQualityCriteriaToHAVDao();
        refDestinationDao = persistenceContext.getRefDestinationDao();
        harvestingYealdDao = persistenceContext.getHarvestingYealdDao();
        qualityCriteriaManualDao = persistenceContext.getQualityCriteriaManualAgreDao();

        refDestinationDao.create(
                RefDestination.PROPERTY_ACTIVE, true,
                RefDestination.PROPERTY_ESPECE, "espece0"
        );

        refDestinationDao.create(
                RefDestination.PROPERTY_ACTIVE, true,
                RefDestination.PROPERTY_ESPECE, "espece1"
        );
    }

    @After
    public void clonePersistenceContext() {
        persistenceContext.close();
    }


    protected void createQualityCriteriaAssoValorisations(
            HarvestingAction persistedHarvestingAction,
            Collection<QualityCriteriaAsso> qualityCriteriaAssos) {

        // create valorisations
        Collection<HarvestingActionValorisation> valorisations =
                createValorisations(persistedHarvestingAction);

        // add quality criteria
        for (HarvestingActionValorisation valorisation : valorisations) {
            if (valorisation.isMain()) {
                valorisation.setQualityCriteriaAsso(qualityCriteriaAssos);
            }
        }
    }

    @Test
    public void testNoQualityCriteriaPersistance() {

        HarvestingAction harvestingAction = createHarvestingAction();
        createValorisations(harvestingAction);

        persistenceContext.commit();

        Collection<HarvestingAction> harvestingActions = harvestingActionDao.findAll();
        harvestingActionDao.deleteAll(harvestingActions);

        persistenceContext.commit();

        Assert.assertEquals(0, harvestingActionDao.count());
        Assert.assertEquals(0, harvestingActionValorisationDao.count());

    }

    @Test
    public void testAssociationQualityCriteriaPersistance() {

        HarvestingAction harvestingAction = createHarvestingAction();

        List<QualityCriteriaAsso> qualityCriteriaAssos = getQualityCriteriaAssos();

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

        Assert.assertEquals(0, harvestingActionDao.count());
        Assert.assertEquals(0, harvestingActionValorisationDao.count());
        Assert.assertEquals(1, refQualityCriteriaDao.count());
        Assert.assertEquals(0, qualityCriteriaAssoDao.count());
    }

    @Test
    public void testManualAgreQualityCriteriaPersistance() {

        HarvestingAction harvestingAction = createHarvestingAction();

        List<QualityCriteriaManualAgre> qualityCriteriaManualAgres = getQualityCriteriaManualAgres();

        createQualityCriteriaManualAgreValorisations(harvestingAction, qualityCriteriaManualAgres);

        persistenceContext.commit();

        List<QualityCriteriaManualAgre> allPersistedQualityCriteria = new ArrayList<>();
        List<HarvestingAction> persistedHarvestingActions = harvestingActionDao.findAll();

        // check every things has been persisted
        Assert.assertEquals(1, persistedHarvestingActions.size());
        HarvestingAction action = persistedHarvestingActions.get(0);
        Collection<HarvestingActionValorisation> valorisations = action.getValorisations();
        Assert.assertTrue(CollectionUtils.isNotEmpty(valorisations));
        for (HarvestingActionValorisation valorisation : valorisations) {
            if (valorisation.isMain()) {
                Collection<QualityCriteriaManualAgre> qualityCriteriaManualAgre1 = valorisation.getQualityCriteriaManualAgre();
                Assert.assertTrue(CollectionUtils.isNotEmpty(qualityCriteriaManualAgre1));
                allPersistedQualityCriteria.addAll(qualityCriteriaManualAgre1);
            }
        }

        harvestingActionDao.deleteAll(persistedHarvestingActions);

        persistenceContext.commit();

        Assert.assertEquals(0, harvestingActionDao.count());
        Assert.assertEquals(0, harvestingActionValorisationDao.count());
        Assert.assertEquals(1, refQualityCriteriaDao.count());
        Assert.assertEquals(0, qualityCriteriaManualDao.count());
    }


    @Ignore
    @Test
    public void testAgregateQualityCriteriaPersistance() {

        HarvestingAction harvestingAction = createHarvestingAction();

        createQualityCriteriaAgreValorisations(harvestingAction);

        persistenceContext.commit();

        List<HarvestingAction> harvestingActions = harvestingActionDao.findAll();

        Assert.assertEquals(1, harvestingActions.size());
        HarvestingAction action = harvestingActions.get(0);

        List<HarvestingActionValorisation> actionValorisations = action.getValorisations();
        Assert.assertTrue(CollectionUtils.isNotEmpty(actionValorisations));
        for (HarvestingActionValorisation valorisation : actionValorisations) {
            if (valorisation.isMain()) {
                Collection<QualityCriteriaAgre> valorisationQualityCriteriaAgres = valorisation.getRefQualityCriteriaQualityCriteriaAgre();
                Assert.assertTrue(CollectionUtils.isNotEmpty(valorisationQualityCriteriaAgres));
            }
        }

        harvestingActionDao.deleteAll(harvestingActions);

        persistenceContext.commit();

        Assert.assertEquals(0, harvestingActionDao.count());
        Assert.assertEquals(0, harvestingActionValorisationDao.count());
        Assert.assertEquals(1, refQualityCriteriaDao.count());
        Assert.assertEquals(0, qualityCriteriaAgreDao.count());

    }

    @Test
    public void testQualityCriteriaToHAVPersistance() {

        HarvestingAction harvestingAction = createHarvestingAction();

        List<QualityCriteriaToHAV> qualityCriteriaToHAVs = getQualityCriteriaToHAVs();

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

        persistenceContext.commit();

        harvestingActionDao.deleteAll(harvestingActions);

        persistenceContext.commit();

        Assert.assertEquals(0, harvestingActionDao.count());
        Assert.assertEquals(0, harvestingActionValorisationDao.count());
        Assert.assertEquals(1, refQualityCriteriaDao.count());
        Assert.assertEquals(0, qualityCriteriaToHAVDao.count());
    }

    protected void createQualityCriteriaManualAgreValorisations(
            HarvestingAction persistedHarvestingAction,
            Collection<QualityCriteriaManualAgre> qualityCriteriaManualAgres) {

        // create valorisations
        Collection<HarvestingActionValorisation> valorisations =
                createValorisations(persistedHarvestingAction);

        // add quality criteria
        for (HarvestingActionValorisation valorisation : valorisations) {
            if (valorisation.isMain()) {
                valorisation.setQualityCriteriaManualAgre(qualityCriteriaManualAgres);
            }
        }
    }

    protected Collection<HarvestingActionValorisation> createQualityCriteriaAgreValorisations(
            HarvestingAction persistedHarvestingAction) {


        // create valorisations
        Collection<HarvestingActionValorisation> valorisations =
                createValorisations(persistedHarvestingAction);

        // add quality criteria
        RefQualityCriteria refQualityCriteria = createRefQualityCriteria();
        for (HarvestingActionValorisation valorisation : valorisations) {
            if (valorisation.isMain()) {
                QualityCriteriaAgre binaryQualityCriteriaAgre = qualityCriteriaAgreDao.newInstance();
                binaryQualityCriteriaAgre.setBinaryValue(true);
                binaryQualityCriteriaAgre.setRefQualityCriteria(refQualityCriteria);
                binaryQualityCriteriaAgre.setHarvestingActionValorisation(valorisation);
                valorisation.addRefQualityCriteriaQualityCriteriaAgre(binaryQualityCriteriaAgre);
            }
        }

        return valorisations;
    }

    protected void createQualityCriteriaToHAV_Valorisations(
            HarvestingAction persistedHarvestingAction,
            Collection<QualityCriteriaToHAV> qualityCriteriaToHAVs) {

        // create valorisations
        Collection<HarvestingActionValorisation> valorisations = createValorisations(persistedHarvestingAction);

        // add quality criteria
        for (HarvestingActionValorisation valorisation : valorisations) {
            if (valorisation.isMain()) {
                for (QualityCriteriaToHAV qualityCriteriaToHAV : qualityCriteriaToHAVs) {
                    qualityCriteriaToHAV.setHarvestingActionValorisation(valorisation);
                }
            }
        }
    }

    protected List<QualityCriteriaManualAgre> getQualityCriteriaManualAgres() {
        RefQualityCriteria refQualityCriteria = createRefQualityCriteria();

        QualityCriteriaManualAgre qualityCriteriaManualAgre = qualityCriteriaManualDao.newInstance();
        qualityCriteriaManualAgre.setBinaryValue(true);
        qualityCriteriaManualAgre.setRefQualityCriteria(refQualityCriteria);

        qualityCriteriaManualAgre = qualityCriteriaManualDao.create(qualityCriteriaManualAgre);

        QualityCriteriaManualAgre quantitativeQualityCriteriaAsso = qualityCriteriaManualDao.newInstance();
        quantitativeQualityCriteriaAsso.setQuantitativeValue(20.5);
        quantitativeQualityCriteriaAsso.setRefQualityCriteria(refQualityCriteria);

        quantitativeQualityCriteriaAsso = qualityCriteriaManualDao.create(quantitativeQualityCriteriaAsso);

        List<QualityCriteriaManualAgre> qualityCriteriaManualAgres = new ArrayList<>();
        qualityCriteriaManualAgres.add(qualityCriteriaManualAgre);
        qualityCriteriaManualAgres.add(quantitativeQualityCriteriaAsso);
        return qualityCriteriaManualAgres;
    }

    protected List<QualityCriteriaAsso> getQualityCriteriaAssos() {
        RefQualityCriteria refQualityCriteria = createRefQualityCriteria();

        QualityCriteriaAsso binaryQualityCriteriaAsso = qualityCriteriaAssoDao.newInstance();
        binaryQualityCriteriaAsso.setBinaryValue(true);
        binaryQualityCriteriaAsso.setRefQualityCriteria(refQualityCriteria);

        binaryQualityCriteriaAsso = qualityCriteriaAssoDao.create(binaryQualityCriteriaAsso);

        QualityCriteriaAsso quantitativeQualityCriteriaAsso = qualityCriteriaAssoDao.newInstance();
        quantitativeQualityCriteriaAsso.setQuantitativeValue(20.5);
        quantitativeQualityCriteriaAsso.setRefQualityCriteria(refQualityCriteria);

        quantitativeQualityCriteriaAsso = qualityCriteriaAssoDao.create(quantitativeQualityCriteriaAsso);

        List<QualityCriteriaAsso> qualityCriteriaAssos = new ArrayList<>();
        qualityCriteriaAssos.add(binaryQualityCriteriaAsso);
        qualityCriteriaAssos.add(quantitativeQualityCriteriaAsso);
        return qualityCriteriaAssos;
    }

    protected List<QualityCriteriaToHAV> getQualityCriteriaToHAVs() {
        RefQualityCriteria refQualityCriteria = createRefQualityCriteria();
        QualityCriteriaToHAV binaryQualityCriteriaHAV = qualityCriteriaToHAVDao.newInstance();
        binaryQualityCriteriaHAV.setBinaryValue(true);
        binaryQualityCriteriaHAV.setRefQualityCriteria(refQualityCriteria);
        binaryQualityCriteriaHAV = qualityCriteriaToHAVDao.create(binaryQualityCriteriaHAV);

        QualityCriteriaToHAV quantitativeQualityCriteriaHAV = qualityCriteriaToHAVDao.newInstance();
        quantitativeQualityCriteriaHAV.setQuantitativeValue(20.5);
        quantitativeQualityCriteriaHAV.setRefQualityCriteria(refQualityCriteria);
        quantitativeQualityCriteriaHAV = qualityCriteriaToHAVDao.create(quantitativeQualityCriteriaHAV);

        List<QualityCriteriaToHAV> qualityCriteriaToHAVs = new ArrayList<>();
        qualityCriteriaToHAVs.add(binaryQualityCriteriaHAV);
        qualityCriteriaToHAVs.add(quantitativeQualityCriteriaHAV);
        return qualityCriteriaToHAVs;
    }

    protected RefQualityCriteria createRefQualityCriteria() {
        RefQualityCriteria refQualityCriteria = refQualityCriteriaDao.newInstance();
        refQualityCriteria.setQualityCriteriaLabel("Valeur de référence");
        refQualityCriteria = refQualityCriteriaDao.create();
        return refQualityCriteria;
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
            HarvestingAction persistedHarvestingAction) {

        List<RefDestination> destinations = refDestinationDao.findAll();

        Collection<HarvestingActionValorisation> valorisations = persistedHarvestingAction.getValorisations();
        HarvestingActionValorisation mainVal = harvestingActionValorisationDao.newInstance();
        mainVal.setMain(true);
        mainVal.setSpeciesCode(UUID.randomUUID().toString());
        mainVal.setDestination(destinations.get(0));

        mainVal = harvestingActionValorisationDao.create(mainVal);

        valorisations.add(mainVal);

        HarvestingActionValorisation speciesVal = harvestingActionValorisationDao.newInstance();
        speciesVal.setMain(false);
        speciesVal.setSpeciesCode(UUID.randomUUID().toString());
        speciesVal = harvestingActionValorisationDao.create(speciesVal);

        valorisations.add(speciesVal);
        return valorisations;
    }

}