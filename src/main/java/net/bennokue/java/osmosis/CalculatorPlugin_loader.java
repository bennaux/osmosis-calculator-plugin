package net.bennokue.java.osmosis;

import java.util.HashMap;
import java.util.Map;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;

/**
 * Loads the CalculatorPlugin during OSMOSIS initialization.
 * @author bennokue
 */
public class CalculatorPlugin_loader implements PluginLoader {
    /**
     * The CLI argument that tells OSMOSIS to run the CalculatorPlugin.
     */
    public static final String taskName = "calculate-node-tag";

    @Override
    public Map<String, TaskManagerFactory> loadTaskFactories() {
        Map<String, TaskManagerFactory> factoryMap = new HashMap<>();
        CalculatorPlugin_factory calculatorPlugin = new CalculatorPlugin_factory();

        factoryMap.put(taskName, calculatorPlugin);

        return factoryMap;
    }
}