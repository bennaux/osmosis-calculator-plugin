/* TODO Benno Javadoc */
package net.bennokue.java.osmosis;

import java.util.logging.Logger;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkSourceManager;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;

public class CalculatorPlugin_factory extends TaskManagerFactory {

    private static final Logger log = Logger.getLogger(CalculatorPlugin_factory.class.getName());
    private static final String ARG_INPUT_ATTRIBUTES = "inputAttributes";
    private static final String DEFAULT_INPUT_ATTRIBUTES = "";
    private static final String ARG_OUTPUT_ATTRIBUTE = "outputAttribute";
    private static final String DEFAULT_OUTPUT_ATTRIBUTE ="demoAttr";
    private static final String ARG_CALCULATION_FORMULA = "calculation";
    private static final String DEFAULT_CALCULATION_FORMULA = "42";
    private static final String ARG_REMOVE_ATTRIBUTES = "removeAttributes";
    private static final String DEFAULT_REMOVE_ATTRIBUTES = "";
    
    @Override

    protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
        // Get command line arguments
        String inputAttributesString = getStringArgument(taskConfig, ARG_INPUT_ATTRIBUTES, DEFAULT_INPUT_ATTRIBUTES);
        String outputAttribute = getStringArgument(taskConfig, ARG_OUTPUT_ATTRIBUTE, DEFAULT_OUTPUT_ATTRIBUTE);
        String calculation = getStringArgument(taskConfig, ARG_CALCULATION_FORMULA, DEFAULT_CALCULATION_FORMULA);
        String attributesToRemove = getStringArgument(taskConfig, ARG_REMOVE_ATTRIBUTES, DEFAULT_REMOVE_ATTRIBUTES);
        
        SinkSource task = new CalculatorPlugin_task(inputAttributesString, outputAttribute, calculation, attributesToRemove);

        return new SinkSourceManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
    }
}
