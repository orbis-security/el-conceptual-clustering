package nl.vu.kai.bisimulations;

import org.semanticweb.owlapi.model.*;

import java.util.*;

public class ToOWLConverter {

    private final static boolean UNFOLD_ALL=true;

    private final OWLOntologyManager manager;
    private final OWLDataFactory factory;

    private Map<BisimulationNode, String> shortNames = new HashMap<>();

    private int maxDepth=-1;

    private int count = 0;

    public ToOWLConverter(OWLOntologyManager manager){
        this.manager=manager;
        this.factory= manager.getOWLDataFactory();
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public OWLOntology convert(BisimulationGraph graph) throws OWLOntologyCreationException {
        return convert(graph, manager.createOntology());
    }

    public OWLOntology convert(BisimulationGraph graph, OWLOntology basisOntology) throws OWLOntologyCreationException {
        return convert(graph.nodes(), basisOntology);
    }

    public OWLOntology convert(Collection<BisimulationNode> nodes, OWLOntology basisOntology) throws OWLOntologyCreationException {
        OWLOntology result = basisOntology.getOWLOntologyManager().createOntology();
        result.addAxioms(basisOntology.axioms());

        nodes.forEach(node -> {
            OWLClass clazz = clazz(node);
            OWLClassExpression exp = convert(node);
            result.add(factory.getOWLEquivalentClassesAxiom(clazz,exp));
            node.refines()
                    .stream()
                    .filter(nodes::contains)
                    .forEach(b2 ->
                            result.add(factory.getOWLSubClassOfAxiom(clazz, clazz(b2))));
        });

        //HierarchyEvaluator evaluator = new HierarchyEvaluator(result);

        return result;
    }


    public OWLClassExpression convert(BisimulationNode node){
        if(!shortNames.containsKey(node)) {
            shortNames.put(node,"C"+count);
            count++;
        }
        if(maxDepth>=0){
            return convert(node,maxDepth);
        }
        else if(UNFOLD_ALL)
            return convert(node,Collections.EMPTY_SET);
        else {
            List<OWLClassExpression> conjuncts = new LinkedList<>();
            conjuncts.addAll(node.classes());
            node.successors()
                    .stream()
                    .map(
                            pair ->
                                    factory.getOWLObjectSomeValuesFrom(
                                            pair.getKey(),
                                            clazz(pair.getValue())))
                    .forEach(conjuncts::add);

            if (conjuncts.isEmpty())
                return factory.getOWLThing();
            else if (conjuncts.size() == 1)
                return conjuncts.get(0);
            else
                return factory.getOWLObjectIntersectionOf(conjuncts);
        }
    }

    public OWLClassExpression convert(BisimulationNode node, int maxDepth){

        List<OWLClassExpression> conjuncts = new LinkedList<>();
        conjuncts.addAll(node.classes());
        if(maxDepth!=0) {
            node.successors()
                    .stream()
                    .map(
                            pair ->
                                    factory.getOWLObjectSomeValuesFrom(
                                            pair.getKey(),
                                            convert(pair.getValue(),maxDepth-1)))
                    .forEach(conjuncts::add);
        }
        if (conjuncts.isEmpty())
            return factory.getOWLThing();
        else if (conjuncts.size() == 1)
            return conjuncts.get(0);
        else
            return factory.getOWLObjectIntersectionOf(conjuncts);
    }

    public OWLClassExpression convert(BisimulationNode node, Set<BisimulationNode> visited) {
        if(visited.contains(node))
            return clazz(node);
        else {
            Set<BisimulationNode> newVisited = new HashSet<>(visited);
            newVisited.add(node);

            List<OWLClassExpression> conjuncts = new LinkedList<>();
            conjuncts.addAll(node.classes());
            node.successors()
                    .stream()
                    .map(
                            pair ->
                                    factory.getOWLObjectSomeValuesFrom(
                                            pair.getKey(),
                                            convert(pair.getValue(), newVisited)))
                    .forEach(conjuncts::add);

            if(conjuncts.isEmpty())
                return factory.getOWLThing();
            else if(conjuncts.size()==1)
                return conjuncts.get(0);
            else
                return factory.getOWLObjectIntersectionOf(conjuncts);
        }
    }

    public OWLClass clazz(BisimulationNode node) {
        if(node.removed()){
            return factory.getOWLThing();
        }
        if(!shortNames.containsKey(node)) {
            shortNames.put(node,"C"+count);
            count++;
        }
        return factory.getOWLClass(
                IRI.create(
                        shortNames.get(node)+"_"
                                +node.level()
                                //+"_"+node.size()
                )
        );
    }

    public void addUtility2Label(OWLOntology ontology, BisimulationGraph graph, BisimulationGraphEvaluator evaluator) {

        graph.nodes().forEach(node -> {
            long size = evaluator.size(node);
            double utility = evaluator.utility(node);
            OWLClass clazz = clazz(node);
            ontology.addAxiom(
                    factory.getOWLAnnotationAssertionAxiom(
                            clazz.getIRI(),
                            factory.getRDFSLabel(clazz.getIRI().getShortForm()+" - "+size+" - "+utility)));

        });

    }
}
