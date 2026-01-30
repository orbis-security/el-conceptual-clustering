import com.google.common.annotations.VisibleForTesting;
import nl.vu.kai.bisimulations.BisimulationGraph;
import nl.vu.kai.bisimulations.BisimulationGraphParser;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.io.IOException;

public class TestParsing {



    @Test
    public void testParsing() throws IOException, OWLOntologyCreationException {
        BisimulationGraphParser parser = new BisimulationGraphParser(OWLManager.createOWLOntologyManager());
        BisimulationGraph graph = parser.parseGraph(new File( "family-benchmark"));
    }
}
