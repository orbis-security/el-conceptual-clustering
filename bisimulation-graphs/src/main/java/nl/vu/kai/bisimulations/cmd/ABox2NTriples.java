package nl.vu.kai.bisimulations.cmd;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ABox2NTriples {
    public static void main(String[] args) throws OWLOntologyCreationException, IOException {
        if(args.length!=1){
            System.out.println("Extracts the ABox from the given owl ontology and saves it in as n-triples");
            System.out.println();
            System.out.println("Usage:");
            System.out.println(ABox2NTriples.class.getName()+" ONTOLOGY_NAME");
            System.out.println();
            System.out.println("Only supports class and object property assertions for which all arguments are named.");
            System.exit(0);
        }

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        File file = new File(args[0]);

        OWLOntology ontology = manager.loadOntology(IRI.create(file));

        PrintWriter writer = new PrintWriter( new FileWriter(new File(file.getName()+".nt")));

        ontology.aboxAxioms(Imports.INCLUDED).forEach(axiom -> {
            if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
                OWLObjectPropertyAssertionAxiom prp = (OWLObjectPropertyAssertionAxiom) axiom;
                if(prp.getSubject().isNamed() && prp.getObject().isNamed() && prp.getProperty().isNamed())
                writer.println(
                        prp.getSubject().asOWLNamedIndividual()+ " "+
                        prp.getProperty().asOWLObjectProperty()+ " "+
                        prp.getObject().asOWLNamedIndividual()+" .");
            } else if(axiom instanceof  OWLClassAssertionAxiom) {
                OWLClassAssertionAxiom cl = (OWLClassAssertionAxiom) axiom;
                if(cl.getIndividual().isNamed() && cl.getClassExpression().isNamed()){
                    if(!cl.getClassExpression().isOWLThing()) {
                        writer.println(
                                cl.getIndividual().asOWLNamedIndividual() + " " +
                                        "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " +
                                        cl.getClassExpression().asOWLClass() + " .");
                    }
                }
            }
        });
        writer.close();
    }
}
