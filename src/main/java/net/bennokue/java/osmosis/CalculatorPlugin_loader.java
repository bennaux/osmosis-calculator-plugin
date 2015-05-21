/* TODO Benno Javadoc */
package net.bennokue.java.osmosis;

import java.util.HashMap;
import java.util.Map;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;

public class CalculatorPlugin_loader implements PluginLoader {

    @Override
    public Map<String, TaskManagerFactory> loadTaskFactories() {
        Map<String, TaskManagerFactory> factoryMap = new HashMap<>();
        CalculatorPlugin_factory calculatorPlugin = new CalculatorPlugin_factory();

        factoryMap.put("calculate-node-attribute", calculatorPlugin);

        return factoryMap;
    }
}