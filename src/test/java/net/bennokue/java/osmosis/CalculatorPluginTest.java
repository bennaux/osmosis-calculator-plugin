/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/* TODO Benno Javadoc */
package net.bennokue.java.osmosis;

import java.io.File;
import java.net.URI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bennokue
 */
public class CalculatorPluginTest {

    /**
     * If two doubles are compared with
     * {@link #compareStringDoubles(java.lang.String, java.lang.String)}, they
     * will be declared equal if the difference is smaller than this value.
     */
    private static final double doubleEqualityThreshold = 0.00001;
    private static final boolean deleteTemporaryFiles = true;

    public CalculatorPluginTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testForConstant() throws Exception {
        // This test sets for each node the tag "constantTest" to 13
        String[] expectedResult = new String[]{
            "13.0", "13.0", "13.0", "13.0", "13.0", "13.0", "13.0", "13.0", "13.0", "13.0",
            "13.0", "13.0", "13.0", "13.0", "13.0", "13.0", "13.0", "13.0", "13.0", "13.0"
        };
        conductTest("/munich_lmu_original.osm", "", "constantTest", "13", "", expectedResult, "/osm/node/tag[@k=\"constantTest\"]/@v");
    }

    @Test
    public void testForVariable() throws Exception {
        // This test sets for each node the tag "varTest" to the node's lat
        String[] expectedResult = new String[]{
            "48.1465401", "48.1464602", "48.1453829", "48.1452688", "48.1482958",
            "48.1497317", "48.1504719", "48.1454584", "48.1456819", "48.1459761",
            "48.1463792", "48.1465865", "48.138752", "48.1411664", "48.1440136",
            "48.1472222", "48.1489251", "48.1511364", "48.1441256", "48.1471246"
        };
        conductTest("/munich_lmu_original.osm", "lat", "simpleVarTest", "lat", "", expectedResult, "/osm/node/tag[@k=\"simpleVarTest\"]/@v");
    }

    @Test
    public void testForVarPlus7() throws Exception {
        // This test sets for each node the tag "varTest" to the node's lat + 7
        String[] expectedResult = new String[]{
            "55.1465401", "55.1464602", "55.1453829", "55.1452688", "55.1482958",
            "55.1497317", "55.1504719", "55.1454584", "55.1456819", "55.1459761",
            "55.1463792", "55.1465865", "55.138752", "55.1411664", "55.1440136",
            "55.1472222", "55.1489251", "55.1511364", "55.1441256", "55.1471246"
        };
        conductTest("/munich_lmu_original.osm", "lat", "varPlusSeven", "lat+7", "", expectedResult, "/osm/node/tag[@k=\"varPlusSeven\"]/@v");
    }

    @Test
    public void testForVarMinusVar() throws Exception {
        // This test sets the new tag "minusTest" for each node to lat-lon
        String[] expectedResult = new String[]{
            "36.5533125", "36.5507093", "36.5517451", "36.5497828", "36.5519087",
            "36.5520508", "36.5552412", "36.5520546", "36.5523447", "36.5527044",
            "36.5531589", "36.5537575", "36.5454958", "36.5461742", "36.5479561",
            "36.5502672", "36.5505548", "36.556474", "36.5594617", "36.5553138"
        };
        conductTest("/munich_lmu_original.osm", "lat,lon", "latMinusLon", "lat-lon", "", expectedResult, "/osm/node/tag[@k=\"latMinusLon\"]/@v");
    }

    @Test
    public void testForAverageVar() throws Exception {
        /*
         * This test sets the new tag "averagePosition" for each node to the
         * average of lat and lon.
         */
        String[] expectedResult = new String[]{
            "29.86988385", "29.87110555", "29.86951035", "29.8703774", "29.87234145",
            "29.8737063", "29.8728513", "29.8694311", "29.86950955", "29.8696239",
            "29.86979975", "29.86970775", "29.8660041", "29.8680793", "29.87003555",
            "29.8720886", "29.8736477", "29.8728994", "29.86439475", "29.8694677"
        };
        conductTest("/munich_lmu_original.osm", "lat,lon", "nonsenseAverage", "(lat+lon)/2", "", expectedResult, "/osm/node/tag[@k=\"nonsenseAverage\"]/@v");
    }

    @Test
    public void testForTagDeletion() throws Exception {
        // This test deletes the ele tag
        // First check if there actually are ele tags
        String[] expectedResult = new String[]{
            "514.1383389311949", "512.048113676798", "516.1678756128005", "513.443895040002",
            "510.4370899808015", "507.71302892319864", "508.5420710447981", "516.0465147648008",
            "516.0037968192005", "515.4608216544053", "514.4606992512045", "513.470759520002",
            "514.5032885759945", "509.4365640191671", "513.2391497600042", "506.9101774399858",
            "506.4287427695972", "510.6214659583985", "513.6794580479987", "512.2548700415952"
        };
        conductTest("/munich_lmu_srtm.osm", "ele", "ele", "ele", "", expectedResult, "/osm/node/tag[@k=\"ele\"]/@v");
        // Now delete them
        expectedResult = new String[]{};
        conductTest("/munich_lmu_srtm.osm", "", "", "", "ele", expectedResult, "/osm/node/tag[@k=\"ele\"]/@v");
    }

    @Test
    public void testForCalculationAndDeletion() throws Exception {
        /*
         * Takes the ele tag, saves it, converted to foot, in the tag "foot",
         * and deleted the ele tag.
         */
        String[] expectedFoot = new String[]{
            "1686.805574", "1679.94788", "1693.464159", "1684.527215", "1674.662369",
            "1665.725161", "1668.445115", "1693.065993", "1692.925843", "1691.144428",
            "1687.863187", "1684.615353", "1688.002915", "1671.379803", "1683.855478",
            "1663.091133", "1661.511623", "1675.267277", "1685.300059", "1680.626214"
        };
        String[] expectedEle = new String[]{};
        conductTest("/munich_lmu_srtm.osm",
                "ele",
                "foot",
                "ele/0.3048",
                "ele",
                new String[][]{expectedFoot, expectedEle},
                new String[]{"/osm/node/tag[@k=\"foot\"]/@v", "/osm/node/tag[@k=\"ele\"]/@v"});
    }

    private static void conductTest(String inputFileString, String inputAttributes, String outputattribute, String calculation, String removeAttributes, String[] expectedResult, String xpathQuery) throws Exception {
        conductTest(inputFileString, inputAttributes, outputattribute, calculation, removeAttributes, new String[][]{expectedResult}, new String[]{xpathQuery});
    }

    private static void conductTest(String inputFileString, String inputAttributes, String outputattribute, String calculation, String removeAttributes, String[][] expectedResults, String[] xpathQueries) throws Exception {
        File inputFile = new File(new URI(CalculatorPluginTest.class.getResource(inputFileString).toString()).getSchemeSpecificPart());
        File outputFile = java.io.File.createTempFile("osmosiscalctest", null, null);
        if (deleteTemporaryFiles) {
            outputFile.deleteOnExit();
        }
        OsmosisRunner runner = new OsmosisRunner(inputFile, outputFile, inputAttributes, outputattribute, calculation, removeAttributes);
        runner.runOsmosis();
        if (expectedResults.length != xpathQueries.length) {
            throw new IllegalArgumentException("Please give as many results as tests!");
        }
        for (int i = 0; i < expectedResults.length; i++) {
            String[] result = new XMLFlattener(outputFile).getXPathAsArray(xpathQueries[i]);
            compareArraySubsets(expectedResults[i], result);
        }
    }

    private static void compareArraySubsets(String[] arrayExpected, String[] arrayGiven) {
        assertTrue("Tested array should be >= " + arrayExpected.length + " but has length " + arrayGiven.length, arrayGiven.length >= arrayExpected.length);

        for (int i = 0; i < arrayExpected.length; i++) {
            assertTrue("Expected <" + arrayExpected[i] + "> but was <" + arrayGiven[i] + ">.", compareStringDoubles(arrayExpected[i], arrayGiven[i]));
        }
    }

    private static boolean compareStringDoubles(String expectedString, String givenString) {
        if (null == expectedString && null == givenString) {
            return true;
        }
        try {
            double expected = Double.parseDouble(expectedString);
            double given = Double.parseDouble(givenString);
            return Math.abs(expected - given) <= doubleEqualityThreshold;
        } catch (NumberFormatException e) {
            return expectedString.equals(givenString);
        }
    }
}
