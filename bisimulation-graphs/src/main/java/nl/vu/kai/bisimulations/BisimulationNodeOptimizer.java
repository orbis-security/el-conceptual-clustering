package nl.vu.kai.bisimulations;

import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLProperty;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class BisimulationNodeOptimizer {
    public static void optimizeNode(BisimulationNode node){
        for(OWLObjectProperty property:node.properties()){
            List<BisimulationNode> toRemove = new LinkedList<>();
            node.successors(property)
                    .stream()
                    .filter(p1 ->
                            node.successors(property)
                                    .stream()
                                    .filter(p2 -> !p1.equals(p2))
                                    .anyMatch(p2 -> !toRemove.contains(p2) && (p1.removed() || p2.refines(p1))))
                    .forEach(toRemove::add);
            assert node.successors(property).size()!=toRemove.size();
            node.removeSuccessors(property,toRemove);
        }
    }
}
