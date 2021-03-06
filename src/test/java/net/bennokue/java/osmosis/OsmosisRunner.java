package net.bennokue.java.osmosis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;
import org.openstreetmap.osmosis.xml.v0_6.XmlWriter;

/**
 * Loads an OSM file with OSMOSIS, let the CalculatorPlugin do its work and save
 * an OSM file.
 *
 * @author bennokue
 */
public class OsmosisRunner {

    private static final Logger logger = Logger.getLogger(OsmosisRunner.class.getName());

    /**
     * The input OSM file.
     */
    protected final File inputFile;
    /**
     * The output OSM file.
     */
    protected final File outputFile;
    /**
     * The CalculatorPlugin to use.
     */
    protected final CalculatorPlugin_task calculator;
    /**
     * Did the sanity checks during init go well?
     */
    private boolean everything_ok = false;

    /**
     * Create an OsmosisRunner, initialize the CalculatorPlugin and do some
     * sanity checks.
     *
     * @param inputFile The OSM data file to read.
     * @param outputFile Where should the resulting OSM file be saved to?
     * @param inputAttributes See
     * {@link CalculatorPlugin_task#CalculatorPlugin_task(java.lang.String, java.lang.String, java.lang.String, java.lang.String) CalculatorPlugin_task(...)}.
     * @param outputAttributes See
     * {@link CalculatorPlugin_task#CalculatorPlugin_task(java.lang.String, java.lang.String, java.lang.String, java.lang.String) CalculatorPlugin_task(...)}.
     * @param calculation See
     * {@link CalculatorPlugin_task#CalculatorPlugin_task(java.lang.String, java.lang.String, java.lang.String, java.lang.String) CalculatorPlugin_task(...)}.
     * @param removeAttributes See
     * {@link CalculatorPlugin_task#CalculatorPlugin_task(java.lang.String, java.lang.String, java.lang.String, java.lang.String) CalculatorPlugin_task(...)}.
     * @throws IOException
     */
    public OsmosisRunner(File inputFile, File outputFile, String inputAttributes, String outputAttributes, String calculation, String removeAttributes) throws IOException {
        this.inputFile = inputFile;

        if (!this.inputFile.isFile() || !this.inputFile.canRead()) {
            throw new IllegalArgumentException(this.inputFile.toString() + " does not exist or is not readable!");
        }

        this.outputFile = outputFile;

        if (this.outputFile.exists()) {
            if (!this.outputFile.isFile() || !this.outputFile.canWrite()) {
                throw new IllegalArgumentException(this.outputFile.toString() + " is not a file or not writeable!");
            }
        } else {
            if (!this.outputFile.createNewFile() || !this.outputFile.canWrite()) {
                throw new IllegalArgumentException(this.outputFile.toString() + " can not be created or is not writeable!");
            }
        }

        this.calculator = new CalculatorPlugin_task(inputAttributes, outputAttributes, calculation, removeAttributes);

        this.everything_ok = true;
    }

    /**
     * Run the OSMOSIS pipeline.
     *
     * @throws Exception If anything goes wrong.
     */
    public void runOsmosis() throws Exception {
        if (!this.everything_ok) {
            logger.log(Level.SEVERE, "Not everything OK, canceling");
        }
        try (
                BufferedWriter outputWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outputFile), "UTF-8"))) {
            XmlReader xmlReader = new XmlReader(this.inputFile, false, CompressionMethod.None);
            XmlWriter xmlWriter = new XmlWriter(outputWriter);

            // Chain the pieces together: XMLLoader -- Calculator -- XMLWriter
            xmlReader.setSink(this.calculator);
            this.calculator.setSink(xmlWriter);

            // RUN
            xmlReader.run();
        } catch (Exception e) {
            throw e;
        }
    }
}
