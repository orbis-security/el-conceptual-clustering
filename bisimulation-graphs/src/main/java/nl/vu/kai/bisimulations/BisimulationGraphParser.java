package nl.vu.kai.bisimulations;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BisimulationGraphParser {

    public static final String FOLDER = "rdf_summary_graph";
    public static final String PREFIX = "http://cs.vu.nl/clustering#hash_block-";
    public static final String LEVEL = "http://cs.vu.nl/clustering#startLevel";

    private final OWLDataFactory factory;
    private final OWLOntology referenceOntology;

    public BisimulationGraphParser(OWLOntologyManager manager) throws OWLOntologyCreationException {
        this.factory=manager.getOWLDataFactory();
        this.referenceOntology=manager.createOntology();
    }

    public BisimulationGraphParser(OWLDataFactory factory, OWLOntology referenceOntology) {
        this.factory=factory;
        this.referenceOntology=referenceOntology;
    }

    public BisimulationGraph parseGraph(File folder) throws IOException {
        return parseGraph(folder, -1);
    }

    public BisimulationGraph parseGraph(File folder, int maxLevel) throws IOException {

        File sizesFile = new File(folder.getAbsolutePath() + File.separator +  FOLDER+File.separator+"sizes.nt");
        File dataFile = new File(folder.getAbsolutePath() + File.separator +  FOLDER+File.separator+"data.nt");
        File refinesFile = new File(folder.getAbsolutePath() + File.separator +  FOLDER+File.separator+"refines.nt");
        File intervalsFile = new File(folder.getAbsolutePath() + File.separator +  FOLDER+File.separator+"intervals.nt");

        BisimulationGraph result = new BisimulationGraph();

        BufferedReader reader = new BufferedReader(new FileReader(sizesFile));
        for (String line; (line = reader.readLine()) != null; ) {
            List<String> components = Arrays
                    .stream(line
                            .substring(0, line.length()-2)
                            .split(" "))
                    .map(x -> x.substring(1, x.length()-1))
                    .collect(Collectors.toList());
            BisimulationNode node = new BisimulationNode(components.get(0));
            node.setSize(Integer.parseInt(components.get(2)));
            result.addNode(components.get(0), node);
        }

        reader = new BufferedReader(new FileReader(intervalsFile));
        for (String line; (line = reader.readLine()) != null; ) {
            List<String> components = Arrays
                    .stream(line
                            .substring(0, line.length() - 2)
                            .split(" "))
                    .map(x -> x.substring(1, x.length() - 1))
                    .collect(Collectors.toList());
            if(components.get(1).equals(LEVEL)) {
                BisimulationNode node = result.get(components.get(0));
                node.setLevel(Integer.parseInt(components.get(2)));
                if(maxLevel>-1 && node.level()>maxLevel) {
                    result.removeNode(node);
                }
                if(node.level()==0)
                    result.setTopNode(node);
            }
        }

        reader = new BufferedReader(new FileReader(dataFile));
        for (String line; (line = reader.readLine()) != null; ) {
            List<String> components = Arrays
                    .stream(line
                            .substring(0, line.length()-2)
                            .split(" "))
                    .map(x -> x.substring(1, x.length()-1))
                    .collect(Collectors.toList());

            BisimulationNode node = result.get(components.get(0));
            OWLClass owlClass = factory.getOWLClass(IRI.create(components.get(1)));
            if(referenceOntology.getClassesInSignature(Imports.INCLUDED).contains(owlClass)){
                node.addClass(owlClass);
            } else {
                OWLObjectProperty property = factory.getOWLObjectProperty(IRI.create(components.get(1)));
                BisimulationNode successor = result.get(components.get(2));
                if(!successor.removed())
                    node.addSuccessor(property, successor);
            }
        }
        reader = new BufferedReader(new FileReader(refinesFile));
        for (String line; (line = reader.readLine()) != null; ) {
            List<String> components = Arrays
                    .stream(line
                            .substring(0, line.length() - 2)
                            .split(" "))
                    .map(x -> x.substring(1, x.length() - 1))
                    .collect(Collectors.toList());
            BisimulationNode node = result.get(components.get(0));
            node.addRefines(result.get(components.get(2)));
        }


        Set<BisimulationNode> duplicates = new HashSet<>();

        identifyTopNode(result);

        result.nodes().forEach(n1 ->
                result.nodes()
                        .stream()
                        .filter(x -> !x.equals(n1))
                        .filter(n1::deepEquals)
                        .filter( x-> !duplicates.contains(x))
                        .forEach(x-> duplicates.add(n1)));

        System.out.println("Duplicate nodes: "+duplicates);
        if(duplicates.size()>0)
            System.exit(1);
        // duplicate cannot be removed without causing complications due references
        // TODO safe way would be to identify them

        //result.removeNodes(toRemove);

        //result.nodes().forEach(BisimulationNodeOptimizer::optimizeNode);

        return result;
    }

    /**
     * Workaround needed since result sometimes contains a node without edges twice
     */
    private void identifyTopNode(BisimulationGraph graph) {
        Set<BisimulationNode> duplicateTopNodes = graph.nodes()
                .stream()
                .filter(x -> x.classes().isEmpty() && x.successors().isEmpty())
                .collect(Collectors.toSet());
        duplicateTopNodes.remove(graph.getTopNode());
        if(duplicateTopNodes.isEmpty())
            return;
        else {
            graph.nodes().forEach(node -> {
                List<OWLObjectProperty> ps = node.properties()
                        .stream()
                        .filter(p -> node.successors(p)
                                .stream()
                                .anyMatch(duplicateTopNodes::contains))
                        .collect(Collectors.toUnmodifiableList());
                ps.forEach(p -> {
                    node.addSuccessor(p, graph.getTopNode());
                    node.removeSuccessors(p, duplicateTopNodes);
                });
            });
            graph.removeNodes(duplicateTopNodes);
        }

    }
}
