/* TODO Benno Javadoc */
package net.bennokue.java.osmosis;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;

public class CalculatorPlugin_task implements SinkSource, EntityProcessor {

    private static final Logger log = Logger.getLogger(CalculatorPlugin_task.class.getName());
    private Sink sink;
    private final HashSet<String> inputAttributes;
    private final String outputAttribute;
    private final Expression calculation;
    private final HashSet<String> attributesToBeRemoved;

    public CalculatorPlugin_task(String inputAttributesString, String outputAttributeString, String calculationString, String attributesToBeRemovedString) {
        this.inputAttributes = new HashSet<>(Arrays.asList(inputAttributesString.split(",")));
        this.outputAttribute = outputAttributeString;
        this.attributesToBeRemoved = new HashSet<>(Arrays.asList(attributesToBeRemovedString.split(",")));

        // Nothing to do
        if (calculationString.isEmpty() && attributesToBeRemovedString.isEmpty()) {
            throw new IllegalArgumentException("Neither calculation nor deletion specified!");
        } // Just calculate
        else if (!calculationString.isEmpty() && attributesToBeRemovedString.isEmpty()) {
            if (outputAttributeString.equals("")) {
                throw new IllegalArgumentException("Output attribute name must not be empty!");
            }
            this.calculation = this.createExpression(calculationString);
        } // Just delete
        else if (calculationString.isEmpty() && !attributesToBeRemovedString.isEmpty()) {
            this.calculation = null;
        } // Calculate AND delete
        else {
            if (outputAttributeString.equals("")) {
                throw new IllegalArgumentException("Output attribute name must not be empty!");
            }
            this.calculation = this.createExpression(calculationString);
        }
    }

    private Expression createExpression(String calculationString) {
        Expression expression
                = new ExpressionBuilder(calculationString)
                .variables(this.inputAttributes)
                .build();
        return expression;
    }

    @Override
    public void process(EntityContainer entityContainer) {
        entityContainer.process(this);
    }

    @Override
    public void process(BoundContainer boundContainer) {
        sink.process(boundContainer);
    }

    @Override
    public void process(NodeContainer container) {
        //backup existing node entity
        Node node = container.getEntity();
        //backup lat and lon of node entity
        double lat = node.getLatitude();
        double lon = node.getLongitude();

        // Get all the tags from the node
        Collection<Tag> nodeTags = node.getTags();
        HashMap<String, Double> nodeAttributes = null;
        if (this.calculation != null) {
            // If we want to calculate, we copy the attributes for later
            nodeAttributes = tagCollectionToHashMap(nodeTags);
        }

        // Remove the output attribute and all attributesToBeRemoved
        HashSet<Tag> tagsToBeRemoved = new HashSet<>();
        for (Tag tag : nodeTags) {
            if (tag.getKey().equalsIgnoreCase(this.outputAttribute)) {
                tagsToBeRemoved.add(tag);
            } else if (this.attributesToBeRemoved.contains(tag.getKey())) {
                tagsToBeRemoved.add(tag);
            }
            /*
             * Here we could insert a counting break condition, but there
             * shouldn't be so many attributes.
             */
        }
        for (Tag removeTag : tagsToBeRemoved) {
            nodeTags.remove(removeTag);
        }

        // Do the calculation if needed
        if (this.calculation != null) {
            // Also add lat, lon
            nodeAttributes.put("lat", lat);
            nodeAttributes.put("lon", lon);

            // Calculate the output value 
            double resultValue = calculateOutputValue(nodeAttributes, node.getId());

            // Add new output tag
            nodeTags.add(new Tag(this.outputAttribute, Double.toString(resultValue)));
        }

        // Create new node entity with adjusted attributes
        CommonEntityData ced = new CommonEntityData(
                node.getId(),
                node.getVersion(),
                node.getTimestamp(),
                node.getUser(),
                node.getChangesetId(),
                nodeTags);

        // Distribute the new nodecontainer to the following sink
        sink.process(new NodeContainer(new Node(ced, lat, lon)));
    }

    private double calculateOutputValue(HashMap<String, Double> nodeAttributes, long nodeId) {
        // Iterate over all input variables and set them at the expression
        for (String inputAttribute : this.inputAttributes) {
            if (inputAttribute.isEmpty()) {
                continue;
            } else if (nodeAttributes.containsKey(inputAttribute)) {
                this.calculation.setVariable(inputAttribute, nodeAttributes.get(inputAttribute));
            } else {
                log.log(Level.INFO, "Warning! Node {0} has no attribute called {1}", new Object[]{nodeId, inputAttribute});
                this.calculation.setVariable(inputAttribute, Double.NaN);
            }
        }
        return this.calculation.evaluate();
    }

    private static HashMap<String, Double> tagCollectionToHashMap(Collection<Tag> collection) {
        HashMap<String, Double> hashMap = new HashMap<>();
        for (Tag tag : collection) {
            try {
                hashMap.put(tag.getKey(), Double.parseDouble(tag.getValue()));
            } catch (NumberFormatException e) {
                log.log(Level.FINEST, "NumberFormatException while parsing " + tag.getKey() + " value", e);
                // Ignore this attribute
            }
        }
        return hashMap;
    }

    @Override
    public void process(WayContainer container) {
        sink.process(container);
    }

    @Override
    public void process(RelationContainer container) {
        sink.process(container);
    }

    @Override
    public void complete() {
        sink.complete();
    }

    @Override
    public void release() {
        sink.release();
    }

    @Override
    public void setSink(Sink sink) {
        this.sink = sink;
    }

    @Override
    public void initialize(Map<String, Object> metaData) {
        // added in osmosis 0.41
    }
}
