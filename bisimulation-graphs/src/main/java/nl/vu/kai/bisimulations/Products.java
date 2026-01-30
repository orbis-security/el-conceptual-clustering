package nl.vu.kai.bisimulations;

import nl.vu.kai.bisimulations.tools.Pair;
import nl.vu.kai.bisimulations.tools.UnorderedPair;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Products {

    private Set<BisimulationNode> knownNodes = new HashSet<>();

    private final Map<UnorderedPair<BisimulationNode>,BisimulationNode> cache = new HashMap<>();

    int count = 0;



    /**
     *
     * @param graph
     * @param maxIterations put to -1 for unlimited iterations
     */
    public void productsFixpoint(BisimulationGraph graph, int maxIterations){
        int lastSize = 0;
        int iterations = 0;
        while(graph.nodes().size()!=lastSize && maxIterations!=0){
            maxIterations--;
            iterations++;
            lastSize=graph.nodes().size();
            addAllProducts(graph);
            System.out.println("Nodes added: "+(graph.nodes().size()-lastSize));
        }
        if(maxIterations!=0){
            System.out.println("ALL PRODUCTS FOUND!");
        }
        System.out.println("ITERATIONS: "+iterations);

    }

    public void addAllProducts(BisimulationGraph graph){
        knownNodes.addAll(graph.nodes());
        Set<BisimulationNode> previouslyKnown = new HashSet<>(knownNodes);
        Set<BisimulationNode> newNodes = new HashSet<>();
        for(BisimulationNode n1:graph.nodes()) {
            for (BisimulationNode n2 : graph.nodes()) {
                BisimulationNode product = product(n1, n2);
                //if (!knownNodes.stream().anyMatch(product::deepEquals)) {
                //    newNodes.add(product);
                //    knownNodes.add(product);
                    //System.out.println(newNodes.size());
                //}
            }
        }
        knownNodes.removeAll(previouslyKnown);
        knownNodes.forEach(n -> graph.addNode(n.getID(), n));
    }

    public BisimulationNode product(BisimulationNode n1, BisimulationNode n2) {
        UnorderedPair<BisimulationNode> pair = new UnorderedPair<>(n1,n2);
        if(cache.containsKey(pair))
            return cache.get(pair);
        else if(n1.equals(n2))
            return n1;
        else if(n1.refines(n2)|| n1.deepRefines(n2))
            return n2;
        else if(n2.refines(n1) || n2.deepRefines(n1))
            return n1;
        else if(n1.deepEquals(n2)) {
            //n1.addRefines(n2);
            //n2.addRefines(n1);
            return n1;
        }else {
            BisimulationNode resultCandidate = new BisimulationNode("Product"+(count++));
            cache.put(pair,resultCandidate);
            resultCandidate.setLevel(Math.min(n1.level(),n2.level()));
            Set<OWLClass> clazzes = new HashSet<>(n1.classes());
            clazzes.retainAll(n2.classes());
            resultCandidate.addClasses(clazzes);

            for(Pair<OWLObjectProperty,BisimulationNode> sucPair: n1.successors()){
                for(BisimulationNode suc2: n2.successors(sucPair.getKey())) {
                    resultCandidate.addSuccessor(
                            sucPair.getKey(),
                            product(sucPair.getValue(), suc2));
                }
            }

            BisimulationNodeOptimizer.optimizeNode(resultCandidate);

            BisimulationNode result = knownNodes.stream()
                    .filter(resultCandidate::deepEquals)
                    .findAny()
                    .orElse(resultCandidate);

            cache.put(pair,result);

            if(!n1.equals(result)) {
                if(n1.deepEquals(result)){
                    n2.addRefines(n1);
                    return n1;
                }
                n1.addRefines(result);
            }
            if(!n2.equals(result)) {
                if(n2.deepEquals(result)){
                    n1.addRefines(n2);
                    return n2;
                }
                n2.addRefines(result);
            }

            knownNodes.add(result);

            return result;
        }
    }
}
