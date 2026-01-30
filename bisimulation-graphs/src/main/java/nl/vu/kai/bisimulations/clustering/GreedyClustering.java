package nl.vu.kai.bisimulations.clustering;

import nl.vu.kai.bisimulations.BisimulationGraph;
import nl.vu.kai.bisimulations.BisimulationGraphEvaluator;
import nl.vu.kai.bisimulations.BisimulationNode;
import nl.vu.kai.bisimulations.tools.Pair;

import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
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

        System.out.println(formattedTime() + "- Initial clustering size: " + currentClustering.size());
        if (numberOfClusters.isPresent() && numberOfClusters.get() >= currentClustering.size()) {
            System.out.print(formattedTime() + "- Nothing to do, at most " + numberOfClusters.get() + " clusters already");
            return currentClustering;
        }

        System.out.print(formattedTime() + "- Building the relative utility matrix");
        System.out.flush();
        Map<BisimulationNode, SortedSet<Pair<BisimulationNode,Double>>> comparisonMatrix = buildMatrix(currentClustering,evaluator);
        System.out.println(" done");

        System.out.print(formattedTime() + "- Computing ranking and removing the lowest relative utility clusters");

        SortedSet<Pair<BisimulationNode,Double>> currentRanking = extractRanking(comparisonMatrix,currentClustering);
        BisimulationNode lastRemoved = null;

        double currentValue = evaluate(currentRanking,currentClustering);

        //System.out.println("Previous value: "+previousValue);
        //System.out.println("Current value: "+currentValue);

        while((numberOfClusters.isEmpty() && currentValue>=previousValue) ||
              (numberOfClusters.isPresent() && numberOfClusters.get()<=currentClustering.size())) {

            previousValue=currentValue;

            if (currentRanking.isEmpty()) {
                // The next statement will throw, this probably should not happen -- JKl
                System.out.println();
                System.out.println(formattedTime() + "- BUG: " + currentClustering.size() +
                        " clusters remain, but the current ranking is empty");
            }

            BisimulationNode weakest = currentRanking.first().getKey();

            //System.out.println("Remove now: "+weakest);
            //System.out.println("Worst relative utlity: "+currentRanking.first().getValue());
            System.out.print(" " + currentValue);

            currentClustering.remove(weakest);
            lastRemoved=weakest;
            currentRanking = extractRanking(comparisonMatrix,currentClustering);

            currentValue=evaluate(currentRanking,currentClustering);
            //System.out.println("Current value: "+currentValue);
        }

        // Last node made things better, so we put it back in
        currentClustering.add(lastRemoved);
        System.out.println();
        System.out.println(formattedTime() + "- Done removing the lowest utility clusters");

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
            System.out.print(".");
            System.out.flush();
        });
        return matrix;
    }

    private static String formattedTime() {
        return "[" + ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + "] ";
    }

}
