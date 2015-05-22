package net.bennokue.java.osmosis;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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

/**
 * CalculatorPlugin class that supports generating, removing or adjusting
 * numeric Node tags via calculation. The user can specify the calculation, the
 * tags that should serve as variables (including lat/lon attributes), the tag
 * where the calculation result should be stored to and the tags that should be
 * removed <em>after</em> the calculation.
 * <p>
 * It is possible to calculate/remove only or to do both at the same time.</p>
 * <p>
 * <strong>Note</strong>: All the result values will be {@code double}s.
 *
 * @author bennokue
 */
public class CalculatorPlugin_task implements SinkSource, EntityProcessor {

    private static final Logger log = Logger.getLogger(CalculatorPlugin_task.class.getName());
    /**
     * The next stage of the OSMOSIS pipeline.
     */
    private Sink sink;
    /**
     * The tags that should serve as variables for the calculation. If none are
     * needed, this HashSet is empty but not null.
     */
    private final HashSet<String> inputTags;
    /**
     * The name of the tag where the output value will be stored at.
     */
    private final String outputTag;
    /**
     * The calculation given by the user, already built as exp4j
     * {@link Expression}.
     */
    private final Expression calculation;
    /**
     * The tags that should be removed after the calculation. If none are
     * supposed to be removed, this HashSet is empty but not null.
     */
    private final HashSet<String> tagsToBeRemoved;

    /**
     * Create new CalculatorPlugin and perform some sanity checks, that might
     * result in an IllegalArgumentException.
     *
     * @param inputTagsString The names of the tags that will be used as
     * variables at the calculation. Separate them with commas.
     * @param outputTagString The tag name where the result value will be
     * stored.
     * @param calculationString The calculation that should be performed. You
     * can use every possible exp4j expression.
     * @param tagsToBeRemovedString The names of the tags that will be removed
     * after the calculation. Separate them with commas.
     */
    public CalculatorPlugin_task(String inputTagsString, String outputTagString, String calculationString, String tagsToBeRemovedString) {
        this.inputTags = new HashSet<>(Arrays.asList(inputTagsString.replace(" ", "").split(",")));
        this.tagsToBeRemoved = new HashSet<>(Arrays.asList(tagsToBeRemovedString.replace(" ", "").split(",")));

        // What do we have to do?
        // Nothing to do
        if (calculationString.isEmpty() && tagsToBeRemovedString.isEmpty()) {
            throw new IllegalArgumentException("Neither calculation nor deletion specified!");
        } // Just calculate
        else if (!calculationString.isEmpty() && tagsToBeRemovedString.isEmpty()) {
            if (outputTagString.equals("")) {
                throw new IllegalArgumentException("Output attribute name must not be empty!");
            }
            this.calculation = this.createExpression(calculationString);
            this.outputTag = outputTagString;
        } // Just delete
        else if (calculationString.isEmpty() && !tagsToBeRemovedString.isEmpty()) {
            this.calculation = null;
            this.outputTag = null; // Prevents removing the tag given as outputTag.
        } // Calculate AND delete
        else {
            if (outputTagString.equals("")) {
                throw new IllegalArgumentException("Output attribute name must not be empty!");
            }
            this.calculation = this.createExpression(calculationString);
            this.outputTag = outputTagString;
        }
    }

    /**
     * Create an exp4j Expression from the user-given String.
     *
     * @param calculationString The calculation to be performed as
     * human-readable String.
     * @return The Expression, ready to fill with variables and evaluate.
     */
    private Expression createExpression(String calculationString) {
        Expression expression
                = new ExpressionBuilder(calculationString)
                .variables(this.inputTags)
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
        // Backup existing node entity
        Node node = container.getEntity();
        // Backup lat and lon of node entity
        double lat = node.getLatitude();
        double lon = node.getLongitude();

        // Get all the tags from the node
        Collection<Tag> nodeTags = node.getTags();

        HashMap<String, Double> nodeTagValues = null;
        if (this.calculation != null) {
            // If we want to calculate, we copy the attributes for later
            nodeTagValues = tagCollectionToHashMap(nodeTags);
        }

        /*
         * Remove the output attribute and all attributesToBeRemoved. We need
         * two loops to prevent ConcurrentModificationExceptions.
         */
        HashSet<Tag> tagObjectsToBeRemoved = new HashSet<>();
        for (Tag tag : nodeTags) {
            if (tag.getKey().equalsIgnoreCase(this.outputTag)) {
                tagObjectsToBeRemoved.add(tag);
            } else if (this.tagsToBeRemoved.contains(tag.getKey())) {
                tagObjectsToBeRemoved.add(tag);
            }
            /*
             * Here we could insert a counting break condition, but there
             * shouldn't be so many attributes.
             */
        }
        for (Tag removeTag : tagObjectsToBeRemoved) {
            nodeTags.remove(removeTag);
        }

        // Do the calculation if needed
        if (this.calculation != null && nodeTagValues != null) {
            // Also add lat, lon
            nodeTagValues.put("lat", lat);
            nodeTagValues.put("lon", lon);

            // Calculate the output value 
            double resultValue = calculateOutputValue(nodeTagValues, node.getId());

            // Add new output tag
            nodeTags.add(new Tag(this.outputTag, Double.toString(resultValue)));
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

    /**
     * Perform the calculation on a single Node-Tag-set.
     *
     * @param nodeTagValues The values of all tags of the Node to perform the
     * calculation on.
     * @param nodeId The OSM id of the Node to perform the calculation on. Will
     * be used for logging only.
     * @return The calculation result.
     */
    private double calculateOutputValue(HashMap<String, Double> nodeTagValues, long nodeId) {
        // Iterate over all input variables and set them at the expression
        for (String inputTag : this.inputTags) {
            if (inputTag.isEmpty()) {
                continue;
            } else if (nodeTagValues.containsKey(inputTag)) {
                this.calculation.setVariable(inputTag, nodeTagValues.get(inputTag));
            } else {
                log.log(Level.INFO, "Warning! Node {0} has no attribute called {1}", new Object[]{nodeId, inputTag});
                this.calculation.setVariable(inputTag, Double.NaN);
            }
        }
        return this.calculation.evaluate();
    }

    /**
     * Convert a Collection of Tag objects to a HashMap, where
     * {@code key = Tag.getKey()} and {@code value = Tag.getValue()}.
     *
     * @param collection The Collection&lt;Tag&gt; to be converted.
     * @return The generated HashMap&lt;String, Double&gt;.
     */
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
