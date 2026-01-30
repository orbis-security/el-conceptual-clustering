package nl.vu.kai.bisimulations.clustering;

import nl.vu.kai.bisimulations.BisimulationGraph;
import nl.vu.kai.bisimulations.BisimulationGraphEvaluator;
import nl.vu.kai.bisimulations.BisimulationNode;
import nl.vu.kai.bisimulations.tools.Pair;

import java.util.*;

public class GreedyClustering implements ClusteringExtractor{

    private static class PairComparator implements Comparator<Pair<BisimulationNode,Double>> {

        @Override
        public int compare(Pair<BisimulationNode, Double> o1, Pair<BisimulationNode, Double> o2) {
            if(o1.equals(o2))
                return 0;
            else if(o1.getValue()!=o2.getValue())
                return o1.getValue().compareTo(o2.getValue());
            else {
                int result = o1.getKey().size() - o2.getKey().size();
                if (result == 0) {
                    return o1.getKey().getID().compareTo(o2.getKey().getID());
                } else
                    return result;
            }
        }
    }

    private Optional<Integer> numberOfClusters = Optional.empty();

    public void setTargetSize(int numberOfClusters) {
        this.numberOfClusters = Optional.of(numberOfClusters);
    }

    @Override
    public Collection<BisimulationNode> extractClustering(BisimulationGraph graph, BisimulationGraphEvaluator evaluator) {
        Set<BisimulationNode> currentClustering = new HashSet<>(graph.nodes());

        double previousValue=-Double.MAX_VALUE;

        Map<BisimulationNode, SortedSet<Pair<BisimulationNode,Double>>> comparisonMatrix = buildMatrix(currentClustering,evaluator);
        SortedSet<Pair<BisimulationNode,Double>> currentRanking = extractRanking(comparisonMatrix,currentClustering);

        BisimulationNode lastRemoved = null;

        double currentValue = evaluate(currentRanking,currentClustering);

        //System.out.println("Previous value: "+previousValue);
        //System.out.println("Current value: "+currentValue);

        System.out.println(currentValue>previousValue);

        while((numberOfClusters.isEmpty() && currentValue>=previousValue) ||
                (numberOfClusters.isPresent() && numberOfClusters.get()<=currentClustering.size())) {

            previousValue=currentValue;

            BisimulationNode weakest = currentRanking.first().getKey();

            //System.out.println("Remove now: "+weakest);
            //System.out.println("Worst relative utlity: "+currentRanking.first().getValue());

            currentClustering.remove(weakest);
            lastRemoved=weakest;
            currentRanking = extractRanking(comparisonMatrix,currentClustering);

            currentValue=evaluate(currentRanking,currentClustering);
            //System.out.println("Current value: "+currentValue);
        }

        // Last node made things better, so we put it back in
        currentClustering.add(lastRemoved);

        return currentClustering;
    }

    public double evaluate(Collection<Pair<BisimulationNode,Double>> currentRanking, Set<BisimulationNode> currentClustering) {
        return currentRanking.stream().mapToDouble(Pair::getValue).sum()/ currentClustering.size();
    }

    private SortedSet<Pair<BisimulationNode, Double>> extractRanking(Map<BisimulationNode, SortedSet<Pair<BisimulationNode, Double>>> comparisonMatrix,
                                                                     Set<BisimulationNode> currentClustering) {

        SortedSet<Pair<BisimulationNode,Double>> result = new TreeSet<>(new PairComparator());

        if(currentClustering.isEmpty())
            return new TreeSet<>();

        comparisonMatrix.keySet()
                .forEach(x -> {
                    if(currentClustering.contains(x) && comparisonMatrix.containsKey(x) && !comparisonMatrix.get(x).isEmpty()) {
                        Pair<BisimulationNode, Double> worst = comparisonMatrix.get(x).first();
                        while (worst!=null && !currentClustering.contains(worst.getKey())) {
                            comparisonMatrix.get(x).remove(worst);
                            worst = comparisonMatrix.get(x).isEmpty()?null:comparisonMatrix.get(x).first();
                        }
                        if(worst!=null)
                            result.add(new Pair(x, worst.getValue()));
                    }
                });
        return result;
    }

    private Map<BisimulationNode, SortedSet<Pair<BisimulationNode, Double>>> buildMatrix(
            Collection<BisimulationNode> nodes,
            BisimulationGraphEvaluator evaluator) {
        Map<BisimulationNode,SortedSet<Pair<BisimulationNode,Double>>> matrix = new HashMap<>();
        nodes.forEach(node1 -> {
            SortedSet<Pair<BisimulationNode,Double>> current = new TreeSet<>(new PairComparator());
            nodes.forEach(node2 -> {
                //if(evaluator.intersect(node1,node2)) {
                if(!node2.equals(node1)){
                    //double value = Math.abs(evaluator.relativeUtility(node1, node2));
                    double value = evaluator.relativeUtility(node1, node2);
                    current.add(new Pair<>(node2,value));
                }
            });
            matrix.put(node1,current);
        });
        return matrix;
    }


}
