package nl.vu.kai.bisimulations.clustering;

import nl.vu.kai.bisimulations.BisimulationGraph;
import nl.vu.kai.bisimulations.BisimulationGraphEvaluator;
import nl.vu.kai.bisimulations.BisimulationNode;

import java.util.Collection;

public interface ClusteringExtractor {
    Collection<BisimulationNode> extractClustering(BisimulationGraph graph, BisimulationGraphEvaluator evaluator);
}
