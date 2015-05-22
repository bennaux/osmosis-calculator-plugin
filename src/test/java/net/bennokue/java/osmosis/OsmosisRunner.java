/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/* TODO Benno Javadoc */
package net.bennokue.java.osmosis;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.nashorn.internal.runtime.logging.Loggable;
import org.openstreetmap.osmosis.core.task.v0_6.Source;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;
import org.openstreetmap.osmosis.xml.v0_6.XmlWriter;

/**
 *
 * @author bennokue
 */
public class OsmosisRunner {
    private static final Logger logger = Logger.getLogger(OsmosisRunner.class.getName());
    
    protected final File inputFile;
    protected final File outputFile;
    protected final CalculatorPlugin_task calculator;
    
    private boolean everything_ok = false;
    
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
    
    public void runOsmosis() throws Exception {
        if (!this.everything_ok) {
            logger.log(Level.SEVERE, "Not everything OK, canceling");
        }
        try (
                BufferedWriter outputWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outputFile), "UTF-8"))
                )
        {
            XmlReader xmlReader = new XmlReader(this.inputFile, false, CompressionMethod.None);
            XmlWriter xmlWriter = new XmlWriter(outputWriter);
            
            // Chain the pieces together: XMLLoader -- Calculator -- XMLWriter
            xmlReader.setSink(this.calculator);
            this.calculator.setSink(xmlWriter);

            // RUN
            xmlReader.run();
        }
        catch (Exception e) {
            throw e;
        }
    }
}
