package com.granpa.pumpselector;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;

public class PumpSelectorTest {
    private PumpRecord pump(String id, String model, double hp, double kw, String phase,
                            String normalizedCategory, boolean selectable, String status,
                            double[][] curve) {
        PumpRecord r = new PumpRecord();
        r.id = id;
        r.model = model;
        r.brand = "TEST";
        r.category = normalizedCategory;
        r.normalizedCategory = normalizedCategory;
        r.hp = hp;
        r.kw = kw;
        r.phase = phase;
        r.selectable = selectable;
        r.dataStatus = status;
        r.curve = curve;
        return r;
    }

    @Test public void unitConversionsRoundTrip() {
        assertEquals(6000d, PumpSelector.toLPH(100d, "LPM"), 1e-9);
        assertEquals(3600d, PumpSelector.toLPH(1d, "LPS"), 1e-9);
        assertEquals(12000d, PumpSelector.toLPH(12d, "M3H"), 1e-9);
        assertEquals(100d, PumpSelector.fromLPH(6000d, "LPM"), 1e-9);
        assertEquals(12d, PumpSelector.fromLPH(12000d, "M3H"), 1e-9);
    }

    @Test public void interpolationAveragesDuplicateHeadPoints() {
        PumpRecord r = pump("p", "P", 1, .75, "S", "JET_PUMP", true,
                "SOURCE_CONFIRMED", new double[][]{{10, 1200}, {10, 1000}, {20, 500}});
        assertEquals(1100d, PumpSelector.flowAt(r, 10d), 1e-9);
        assertEquals(800d, PumpSelector.flowAt(r, 15d), 1e-9);
    }

    @Test public void invalidInvertedCurveIsRejectedEverywhere() {
        PumpRecord r = pump("bad", "Bad", 1, .75, "S", "JET_PUMP", false,
                "SOURCE_ANOMALY", new double[][]{{10, 1000}, {20, 1300}});
        assertFalse(CurveUtils.isValid(r.curve));
        assertNull(PumpSelector.flowAt(r, 15d));
        assertFalse(CurveUtils.canShare(r));
    }

    @Test public void categoryMappingDoesNotMixJetAndMonoblock() {
        PumpRecord jet = pump("j", "Jet", 1, .75, "S", "JET_PUMP", true,
                "SOURCE_CONFIRMED", new double[][]{{10, 1000}, {20, 500}});
        PumpRecord mono = pump("m", "Mono", 1, .75, "S", "SURFACE_MONOBLOCK", true,
                "SOURCE_CONFIRMED", new double[][]{{10, 1000}, {20, 500}});
        assertTrue(PumpSelector.cat(jet, "jet_all"));
        assertFalse(PumpSelector.cat(jet, "monoblock_all"));
        assertTrue(PumpSelector.cat(mono, "monoblock_all"));
        assertFalse(PumpSelector.cat(mono, "jet_all"));
    }

    @Test public void phaseIsFailSafeAndDoesNotMixElectricalSupply() {
        PumpRecord single = pump("s", "Single", 1, .75, "S", "JET_PUMP", true,
                "SOURCE_CONFIRMED", new double[][]{{10, 1000}, {20, 500}});
        PumpRecord three = pump("t", "Three", 1, .75, "T", "JET_PUMP", true,
                "SOURCE_CONFIRMED", new double[][]{{10, 1000}, {20, 500}});
        assertTrue(PumpSelector.phase(single, "S"));
        assertFalse(PumpSelector.phase(single, "T"));
        assertTrue(PumpSelector.phase(three, "T"));
        assertFalse(PumpSelector.phase(three, "S"));
        assertFalse(PumpSelector.phase(single, ""));

        PumpRecord fullSingle = pump("fs", "Full Single", 1, .75, "Single Phase", "JET_PUMP", true,
                "SOURCE_CONFIRMED", new double[][]{{10, 1000}, {20, 500}});
        PumpRecord fullThree = pump("ft", "Full Three", 1, .75, "Three Phase", "JET_PUMP", true,
                "SOURCE_CONFIRMED", new double[][]{{10, 1000}, {20, 500}});
        assertTrue(PumpSelector.phase(fullSingle, "S"));
        assertFalse(PumpSelector.phase(fullSingle, "T"));
        assertTrue(PumpSelector.phase(fullThree, "T"));
        assertFalse(PumpSelector.phase(fullThree, "S"));
        assertTrue(PumpSelector.phase("Single Phase", "S"));
        assertFalse(PumpSelector.phase("Single Phase", "T"));
        assertTrue(PumpSelector.phase("Three Phase", "T"));
        assertFalse(PumpSelector.phase("Three Phase", "S"));
    }

    @Test public void mainAndComparisonUseSameQualityFirstRanking() {
        PumpRecord lowerHpButWorse = pump("a", "A", 1, .75, "S", "JET_PUMP", true,
                "SOURCE_CONFIRMED", new double[][]{{10, 1200}, {20, 600}});
        PumpRecord higherHpButBetter = pump("b", "B", 2, 1.5, "S", "JET_PUMP", true,
                "SOURCE_CONFIRMED", new double[][]{{10, 1050}, {20, 500}});
        ArrayList<PumpRecord> rows = new ArrayList<>(Arrays.asList(lowerHpButWorse, higherHpButBetter));
        PumpSelector.Req req = PumpSelector.req(false, 1000, 1000, "LPH");

        ArrayList<PumpSelector.Result> main = PumpSelector.select(rows, 10, req, "jet_all", "S", "");
        ArrayList<PumpSelector.Result> compare = PumpSelector.selectForCompare(rows, 10, req, "jet_all", "S");

        assertEquals("B", main.get(0).r.model);
        assertEquals("B", compare.get(0).r.model);
    }

    @Test public void unverifiedAndCatalogueOnlyRecordsCannotBeSharedOrRecommended() {
        PumpRecord anomaly = pump("a", "A", 1, .75, "S", "JET_PUMP", false,
                "SOURCE_ANOMALY", new double[][]{{10, 1000}, {20, 500}});
        PumpRecord catalogueOnly = pump("b", "B", 1, .75, "", "SURFACE_MONOBLOCK", false,
                "SOURCE_CONFIRMED_CATALOGUE_ONLY", new double[][]{{10, 1000}, {20, 500}});
        PumpSelector.Req req = PumpSelector.req(false, 1000, 1000, "LPH");
        assertFalse(CurveUtils.canShare(anomaly));
        assertFalse(CurveUtils.canShare(catalogueOnly));
        assertTrue(PumpSelector.select(Arrays.asList(anomaly), 10, req, "jet_all", "S", "").isEmpty());
    }

    @Test public void validSourceConfirmedRecordCanBeShared() {
        PumpRecord valid = pump("v", "Valid", 1, .75, "S", "JET_PUMP", true,
                "SOURCE_CONFIRMED", new double[][]{{10, 1000}, {20, 500}});
        assertTrue(CurveUtils.canShare(valid));
    }
}
