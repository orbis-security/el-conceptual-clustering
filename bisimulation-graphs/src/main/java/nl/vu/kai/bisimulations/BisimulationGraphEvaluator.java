package nl.vu.kai.bisimulations;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BisimulationGraphEvaluator {
    private final BisimulationGraph graph;
    private final OWLOntology ontology;
    private final ToOWLConverter converter;
    private final OWLDataFactory factory;
    private final Map<OWLClassExpression,Set<OWLNamedIndividual>> features;
    private final Map<OWLClass,Set<OWLNamedIndividual>> instanceCache;

    private final OWLReasoner reasoner;

    public BisimulationGraphEvaluator(BisimulationGraph graph, OWLOntology referenceOntology) throws OWLOntologyCreationException {
        this.graph=graph;
        this.converter=new ToOWLConverter(referenceOntology.getOWLOntologyManager());
        this.factory=referenceOntology.getOWLOntologyManager().getOWLDataFactory();
        this.ontology = converter.convert(graph,referenceOntology);
        this.reasoner = new ElkReasonerFactory().createReasoner(ontology);
        reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
        this.features = new HashMap<>();
        this.instanceCache = new HashMap<>();
        extractFeatures();
    }

    public double utility(BisimulationNode node){
        Set<OWLNamedIndividual> superCategory=ontology.getIndividualsInSignature(Imports.INCLUDED);
        Set<OWLNamedIndividual> specificCategory=individuals(node);
        return relativeUtility(superCategory,specificCategory);
    }

    public Set<OWLNamedIndividual> individuals(BisimulationNode node){
        // return reasoner.getInstances(converter.clazz(node)).getFlattened();
        // use features if available?
        OWLClass clazz = converter.clazz(node);
        //if (features.containsKey(clazz))
        //    return features.get(clazz);
        // cache this somewhere else if not found in features?
        Set<OWLNamedIndividual> instances = instanceCache.get(clazz);
        if (instances != null) {
            return instances;
        }
        instances = reasoner.getInstances(clazz).getFlattened();
        instanceCache.put(clazz, instances);
        return instances; // */
    }

    public boolean intersect(BisimulationNode n1, BisimulationNode n2){
        Set<OWLNamedIndividual> s1 = individuals(n1);
        Set<OWLNamedIndividual> s2 = individuals(n2);
        return s1.stream().anyMatch(s2::contains);
    }

    public double relativeUtility(BisimulationNode superNode, BisimulationNode subNode){
         // new HashSet<>(): only needed for s1 since it is modified; always
         // copying the set in individuals() would diminish/negate caching benefits
        Set<OWLNamedIndividual> s1 = new HashSet<>(individuals(superNode));
        Set<OWLNamedIndividual> s2 = individuals(subNode);
        s1.addAll(s2);
        return relativeUtility(s1,s2);
    }

    public double relativeUtility(Set<OWLNamedIndividual> superCategory, Set<OWLNamedIndividual> specificCategory) {
        double superSize = superCategory.size();
        double specificSize = specificCategory.size();
        if(specificSize==0)
            return 0;
        double probability = specificSize/superSize;
        double utility = features.keySet()
                .stream()
                .mapToDouble(feature -> {
                    Set<OWLNamedIndividual> superInst = new HashSet<>(features.get(feature));
                    superInst.retainAll(superCategory);
                    Set<OWLNamedIndividual> specificInst = new HashSet<>(features.get(feature));
                    specificInst.retainAll(specificCategory);
                    double superProbability = superInst.size()/superSize;
                    double specificProbability = specificInst.size()/specificSize;
                    return specificProbability*specificProbability - superProbability*superProbability;
        }).sum();

        return probability*utility;
    }

    private void extractFeatures() {
        System.out.println("Extracting features...");
        graph.nodes()
                .stream()
                .filter(x -> !x.removed())
                .flatMap(node -> {
                                System.out.println("- Successors of " + node.getID());
                                return node.successors()
                                        .stream()
                                        .map(pair ->
                                                factory.getOWLObjectSomeValuesFrom(
                                                        pair.getKey(),
                                                        converter.clazz(pair.getValue())
                                                )
                                        );
                })
                .filter(feature -> !features.containsKey(feature))
                .forEach(feature -> {
                    Set<OWLNamedIndividual> instances = reasoner.getInstances(feature).getFlattened();
                    System.out.println("- - "+feature+" - "+instances.size());
                    features.put(feature, instances);
                });


        ontology.classesInSignature(Imports.INCLUDED)
                .forEach(cl -> {
                    Set<OWLNamedIndividual> instances = reasoner.getInstances(cl).getFlattened();
		    instanceCache.put(cl, instances);
                    if(!instances.isEmpty()) {
                        System.out.println("- Class "+cl+" - "+instances.size());
                        features.put(cl,instances);
                    }
                });
    }

    public int size(BisimulationNode node) {
        return individuals(node).size();
    }

    /*public void transferSizes() {
        graph.nodes().forEach(node -> {
            node.setSize(
                    (int) reasoner.getInstances(converter.clazz(node))
                            .nodes()
                            .count());
        });
    }*/
}
