package net.bennokue.java.osmosis;

import java.util.logging.Logger;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkSourceManager;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;

/**
 * Factory class for Calculator plugin.
 *
 * @author bennokue
 */
public class CalculatorPlugin_factory extends TaskManagerFactory {

    private static final Logger log = Logger.getLogger(CalculatorPlugin_factory.class.getName());
    /**
     * CLI argument name for the comma separated list of input attributes.
     */
    private static final String ARG_INPUT_TAGS = "inputTags";
    /**
     * Default value for {@link #ARG_INPUT_TAGS}.
     */
    private static final String DEFAULT_INPUT_TAGS = "";
    /**
     * CLI argument name for the output attribute.
     */
    private static final String ARG_OUTPUT_TAGS = "outputTag";
    /**
     * Default value for {@link #ARG_OUTPUT_TAGS}.
     */
    private static final String DEFAULT_OUTPUT_TAG = "";
    /**
     * CLI argument for the calculation, that should be performed. Will be
     * passed to exp4j, so every calculation that is supported by that, should
     * work. Information about exp4j:
     * <a href="http://www.objecthunter.net/exp4j/">http://www.objecthunter.net/exp4j/</a>.
     */
    private static final String ARG_CALCULATION_FORMULA = "calculation";
    /**
     * Default value for {@link #ARG_CALCULATION_FORMULA}.
     */
    private static final String DEFAULT_CALCULATION_FORMULA = "";
    /**
     * CLI argument for the node tags that should be removed. If there is also a
     * calculation, the tags will be removed afterwards.
     */
    private static final String ARG_REMOVE_TAGS = "removeTags";
    /**
     * Default value for {@link #ARG_REMOVE_TAGS}.
     */
    private static final String DEFAULT_REMOVE_TAGS = "";

    @Override
    protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
        // Get command line arguments
        String inputTagsString = getStringArgument(taskConfig, ARG_INPUT_TAGS, DEFAULT_INPUT_TAGS);
        String outputTag = getStringArgument(taskConfig, ARG_OUTPUT_TAGS, DEFAULT_OUTPUT_TAG);
        String calculation = getStringArgument(taskConfig, ARG_CALCULATION_FORMULA, DEFAULT_CALCULATION_FORMULA);
        String tagsToRemove = getStringArgument(taskConfig, ARG_REMOVE_TAGS, DEFAULT_REMOVE_TAGS);

        SinkSource task = new CalculatorPlugin_task(inputTagsString, outputTag, calculation, tagsToRemove);

        return new SinkSourceManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
    }
}
