package nl.vu.kai.bisimulations.cmd;

import nl.vu.kai.bisimulations.*;
import nl.vu.kai.bisimulations.clustering.ClusteringExtractor;
import nl.vu.kai.bisimulations.clustering.GreedyClustering;
import nl.vu.kai.bisimulations.clustering.TopNClusters;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxFrameRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.type.LogicalType.Collection;

public class ExtractHierarchy {
    public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        if(args.length!=4){
            System.out.println("Usage:");
            System.out.println(ExtractHierarchy.class.getName()+" SUMMARY_PATH ONTOLOGY MAX_DEPTH ITERATIONS");
            System.exit(0);
        }

        String summaryPath = args[0];
        String ontologyPath = args[1];
        int maxDepth = Integer.parseInt(args[2]);
        int iterations = Integer.parseInt(args[3]);

        System.out.println("Summary path: "+summaryPath);
        System.out.println("Ontology path: "+ontologyPath);
        System.out.println("Max depth: "+maxDepth);
        System.out.println("Iterations: "+iterations);

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(ontologyPath));

        Set<OWLAxiom> toRemove = ontology.aboxAxioms(Imports.INCLUDED).filter(x -> {
            if(x instanceof OWLClassAssertionAxiom) {
                OWLClassAssertionAxiom ax = (OWLClassAssertionAxiom) x;
                return !(ax.getClassExpression().isNamed() && ax.getIndividual().isNamed());
            }
            else if(x instanceof OWLObjectPropertyAssertionAxiom) {
                OWLObjectPropertyAssertionAxiom ax = (OWLObjectPropertyAssertionAxiom) x;
                return !(ax.getSubject().isNamed() && ax.getObject().isNamed() && ax.getProperty().isNamed());
            } else
                return true;
        }).collect(Collectors.toSet());

        ontology.removeAxioms(toRemove);

        System.out.println("ABOX SIZE: "+ontology.getABoxAxioms(Imports.INCLUDED).size());
        System.out.println("INDIVIDUALS: "+ontology.getIndividualsInSignature(Imports.INCLUDED).size());

        BisimulationGraphParser parser =
                new BisimulationGraphParser(
                        manager.getOWLDataFactory(),
                        ontology);
        BisimulationGraph graph = parser.parseGraph(new File(summaryPath), maxDepth);

        //graph.restrictToLevel(maxDepth);
        //graph.restrictToMinSize(10);
        System.out.println("BOUNDED DEPTH NODES: "+graph.nodes().size());

        System.out.println(formattedTime() + "Computing products");
        Products products = new Products();
        /*products.addAllProducts(graph);
        System.out.println("nodes now: "+graph.nodes().size());
        products = new Products();
        products.addAllProducts(graph);
        System.out.println("nodes now: "+graph.nodes().size());
        products = new Products();
        products.addAllProducts(graph);
        System.out.println("nodes now: "+graph.nodes().size());
        */
        products.productsFixpoint(graph,iterations);
        System.out.println(formattedTime() + "Done with the products");
        System.out.println("PRODUCTS: "+graph.nodes().size());
        ToOWLConverter converter = new ToOWLConverter(manager);
        converter.setMaxDepth(maxDepth);
        OWLOntology ont2 = converter.convert(graph,ontology);

        System.out.println(formattedTime() + "Initializing simulation graph evaluator");
        BisimulationGraphEvaluator evaluator = new BisimulationGraphEvaluator(graph,ont2);
        System.out.println(formattedTime() + "Adding utilities to nodes");
        converter.addUtility2Label(ont2, graph, evaluator);
        //ontology.addAxioms(ont2.axioms());

        System.out.println(formattedTime() + "Saving the simulation graph with utilities");
        manager.saveOntology(ont2, new FileOutputStream(new File("output.owl")));

        ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();

        System.out.println(formattedTime() + "Extracting clustering");
        GreedyClustering extractor = new GreedyClustering(); //new TopNClusters(5);
        extractor.setTargetSize(10);
        //ClusteringExtractor extractor = new TopNClusters(10);
        Collection<BisimulationNode> clustering = extractor.extractClustering(graph,evaluator);
        clustering.remove(null);
        System.out.println(formattedTime() + "Number of clusters: "+clustering.size());
        /*clustering.stream()
                .map(converter::convert)
                .map(renderer::render)
                .forEach(System.out::println);*/
        OWLOntology clusteringResult = converter.convert(clustering,ontology);
        converter.addUtility2Label(clusteringResult, graph, evaluator);
        System.out.println(formattedTime() + "Saving the clustering");
        manager.saveOntology(clusteringResult, new FileOutputStream(new File("clustering-result.owl")));

    }

    private static String formattedTime() {
        return "[" + ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + "] ";
    }
}
