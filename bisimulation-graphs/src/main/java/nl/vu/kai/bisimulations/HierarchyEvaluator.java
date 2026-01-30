package nl.vu.kai.bisimulations;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class HierarchyEvaluator {
    private final OWLOntology ontology;

    private final OWLReasoner reasoner;

    public HierarchyEvaluator(OWLOntology ontology){
        this.ontology=ontology;
        this.reasoner = new ElkReasonerFactory().createReasoner(ontology);
        reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
    }

    public long size(OWLClass clazz) {
        return reasoner.instances(clazz).count();
    }

}
