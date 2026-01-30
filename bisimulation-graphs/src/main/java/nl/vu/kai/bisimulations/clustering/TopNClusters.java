package nl.vu.kai.bisimulations.clustering;

import nl.vu.kai.bisimulations.BisimulationGraph;
import nl.vu.kai.bisimulations.BisimulationGraphEvaluator;
import nl.vu.kai.bisimulations.BisimulationNode;
import nl.vu.kai.bisimulations.tools.Pair;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.util.*;
import java.util.stream.Collectors;

public class TopNClusters implements ClusteringExtractor {

    private int n;

    public TopNClusters(int n){
        this.n=n;
    }

    public Collection<BisimulationNode> removeDuplicates(Collection<BisimulationNode> nodes, BisimulationGraphEvaluator evaluator) {
        List<BisimulationNode> filtered = new LinkedList<>();
        for(BisimulationNode node:nodes) {
            Set<OWLNamedIndividual> i1 = evaluator.individuals(node);
            if(filtered.stream().allMatch(f -> {
                Set<OWLNamedIndividual> i2 = evaluator.individuals(f);
                return !i1.containsAll(i2) || !i2.containsAll(i1);
            }))
                filtered.add(node);
        }
        return filtered;
    }

    @Override
    public Collection<BisimulationNode> extractClustering(BisimulationGraph graph, BisimulationGraphEvaluator evaluator) {
        Collection<BisimulationNode> nodes = removeDuplicates(graph.nodes(), evaluator);
        List<Pair<Double,BisimulationNode>> list = nodes
                .stream()
                .map(n -> new Pair<Double,BisimulationNode>(evaluator.utility(n),n))
                .collect(Collectors.toList());
        Collections.sort(list, ((a,b) -> -a.getKey().compareTo(b.getKey())));

        list.subList(0,n)
                .forEach(System.out::println);

        return list.subList(0,n)
                .stream()
                .map(Pair::getValue)
                .collect(Collectors.toUnmodifiableList());
    }
}
