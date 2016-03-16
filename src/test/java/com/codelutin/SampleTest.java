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
import org.h2.Driver;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.H2Dialect;
import org.junit.After;
import org.junit.AfterClass;
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

    protected void createValorisations(
            HarvestingAction persistedHarvestingAction,
            Collection<QualityCriteriaAsso> qualityCriteriaAssos,
            Collection<QualityCriteriaAgre> qualityCriteriaAgres,
            Collection<QualityCriteriaToHAV> qualityCriteriaToHAVs) {

        Collection<HarvestingActionValorisation> valorisations = persistedHarvestingAction.getValorisations();
        HarvestingActionValorisation mainVal = harvestingActionValorisationDao.newInstance();
        mainVal.setMain(true);
        mainVal.setSpeciesCode(UUID.randomUUID().toString());

        mainVal.setQualityCriteriaAsso(qualityCriteriaAssos);
        mainVal.setRefQualityCriteriaQualityCriteriaAgre(qualityCriteriaAgres);

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

    protected void createQualityCriteriaAgreValorisations(
            HarvestingAction persistedHarvestingAction,
            Collection<QualityCriteriaAgre> qualityCriteriaAgres) {
        createValorisations(persistedHarvestingAction, null, qualityCriteriaAgres, null);

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

        HarvestingAction persistedHarvestingAction = createHarvestingAction();
        createNoQualityCriteriaValorisations(persistedHarvestingAction);

        persistenceContext.commit();

        Collection<HarvestingActionValorisation> harvestingActionValorisations = harvestingActionValorisationDao.findAll();
        harvestingActionValorisationDao.deleteAll(harvestingActionValorisations);

    }

    @Test
    public void testAssociationQualityCriteriaPersistance() {

        HarvestingAction persistedHarvestingAction = createHarvestingAction();

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

        createQualityCriteriaAssoValorisations(persistedHarvestingAction, qualityCriteriaAssos);

        persistenceContext.commit();

        Collection<HarvestingActionValorisation> harvestingActionValorisations = harvestingActionValorisationDao.findAll();
        harvestingActionValorisationDao.deleteAll(harvestingActionValorisations);

        persistenceContext.commit();
    }

    @Test
    public void testAgregateQualityCriteriaPersistance() {

        HarvestingAction persistedHarvestingAction = createHarvestingAction();

        RefQualityCriteria qualityCriteria0 = createRefQualityCriteria();

        QualityCriteriaAgre binaryQualityCriteriaAgre = qualityCriteriaAgreDao.newInstance();
        binaryQualityCriteriaAgre.setBinaryValue(true);
        binaryQualityCriteriaAgre.setRefQualityCriteria(qualityCriteria0);

        binaryQualityCriteriaAgre = qualityCriteriaAgreDao.create(binaryQualityCriteriaAgre);

        QualityCriteriaAgre quantitativeQualityCriteriaAgre = qualityCriteriaAgreDao.newInstance();
        quantitativeQualityCriteriaAgre.setQuantitativeValue(20.5);
        quantitativeQualityCriteriaAgre.setRefQualityCriteria(qualityCriteria0);

        quantitativeQualityCriteriaAgre = qualityCriteriaAgreDao.create(quantitativeQualityCriteriaAgre);

        List<QualityCriteriaAgre> qualityCriteriaAgres = new ArrayList<>();
        qualityCriteriaAgres.add(binaryQualityCriteriaAgre);
        qualityCriteriaAgres.add(quantitativeQualityCriteriaAgre);

        createQualityCriteriaAgreValorisations(persistedHarvestingAction, qualityCriteriaAgres);

        persistenceContext.commit();

        Collection<HarvestingActionValorisation> harvestingActionValorisations = harvestingActionValorisationDao.findAll();
        harvestingActionValorisationDao.deleteAll(harvestingActionValorisations);

        persistenceContext.commit();
    }

    @Test
    public void testQualityCriteriaToHAVPersistance() {

        HarvestingAction persistedHarvestingAction = createHarvestingAction();

        QualityCriteriaToHAV binaryQualityCriteriaHAV = qualityCriteriaToHAVDao.newInstance();
        binaryQualityCriteriaHAV.setBinaryValue(true);
        binaryQualityCriteriaHAV = qualityCriteriaToHAVDao.create(binaryQualityCriteriaHAV);

        QualityCriteriaToHAV quantitativeQualityCriteriaHAV = qualityCriteriaToHAVDao.newInstance();
        quantitativeQualityCriteriaHAV.setQuantitativeValue(20.5);
        quantitativeQualityCriteriaHAV = qualityCriteriaToHAVDao.create(quantitativeQualityCriteriaHAV);

        List<QualityCriteriaToHAV> qualityCriteriaToHAVs = new ArrayList<>();
        qualityCriteriaToHAVs.add(binaryQualityCriteriaHAV);
        qualityCriteriaToHAVs.add(quantitativeQualityCriteriaHAV);

        createQualityCriteriaToHAV_Valorisations(persistedHarvestingAction, qualityCriteriaToHAVs);

        persistenceContext.commit();

        Collection<HarvestingActionValorisation> harvestingActionValorisations = harvestingActionValorisationDao.findAll();
        harvestingActionValorisationDao.deleteAll(harvestingActionValorisations);

        persistenceContext.commit();

    }

}