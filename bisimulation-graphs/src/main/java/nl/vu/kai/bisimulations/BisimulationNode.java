package nl.vu.kai.bisimulations;

import nl.vu.kai.bisimulations.tools.MultiMap;
import nl.vu.kai.bisimulations.tools.Pair;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLProperty;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * TODO add name as key for hashcode and equals
 */

public class BisimulationNode {
    private int size;
    private int level;
    private Set<OWLClass> classes = new HashSet<>();
    private Set<BisimulationNode> refines = new HashSet<>();
    private MultiMap<OWLObjectProperty,BisimulationNode> successors = new MultiMap<>();

    private boolean removed = false;

    private final String id;

    public BisimulationNode(String id) {
        this.id=id;
    }

    public void setRemoved(){
        this.removed=true;
    }

    public boolean removed(){
        return removed;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setLevel(int level) {
        this.level=level;
    }

    public void addRefines(BisimulationNode node){
        if(node == null)
            throw new IllegalArgumentException();
        refines.add(node);
    }

    public void addSuccessor(OWLObjectProperty property, BisimulationNode successor) {
        successors.add(property,successor);
    }

    public void addClass(OWLClass clazz){
        this.classes.add(clazz);
    }

    public void addClasses(Set<OWLClass> classes){
        this.classes.addAll(classes);
    }

    public int size(){
        return size;
    }

    public int level(){
        return level;
    }

    public Collection<BisimulationNode> refines() {
        return Collections.unmodifiableCollection(refines);
    }

    public boolean refines(BisimulationNode other) {
        return refines.contains(other);
    }

    public Collection<OWLClass> classes() {
        return Collections.unmodifiableCollection(classes);
    }

    public Collection<OWLObjectProperty> properties() {
        return successors.keys();
    }

    public Collection<Pair<OWLObjectProperty,BisimulationNode>> successors() {
        return successors.keys()
                .stream()
                .flatMap(key ->
                        successors.get(key)
                                .stream()
                                .map(value -> new Pair<OWLObjectProperty,BisimulationNode>(key,value)))
                .collect(Collectors.toSet());
    }

    public Collection<BisimulationNode> successors(OWLObjectProperty property) {
        return successors.get(property);
    }

    public boolean deepRefines(BisimulationNode other) {
        if(equals(other))
            return true;
        if(!classes.containsAll(other.classes))
            return false;
        return other.successors
                .keys()
                .stream()
                .allMatch(prp -> {
            Collection<BisimulationNode> suc = successors(prp);
            return suc!=null &&
                    suc.containsAll(other.successors.get(prp)) ||
                    other.successors.get(prp).stream().allMatch(otherS ->suc.stream().anyMatch(x->x.refines(otherS)));
        });
    }

    public boolean deepEquals(BisimulationNode other) {
        return deepRefines(other) && other.deepRefines(this);
    }

    @Override
    public boolean equals(Object other){
        if(other==null || !(other instanceof BisimulationNode))
            return false;
        else
            return ((BisimulationNode)other).id.equals(id);
    }

    public int hashcode() {
        return id.hashCode();
    }

    public String getID() {
        return id;
    }

    public void removeSuccessors(OWLObjectProperty property, Collection<BisimulationNode> toRemove) {
        successors.removeAll(property,toRemove);
    }

    @Override
    public String toString() {
        return classes.stream()
                .map(x -> x.getIRI().getShortForm()).
                collect(Collectors.joining(", "))
                + ", "+successors().stream()
                .map(pair -> pair.getKey().getIRI().getShortForm()+"."+pair.getValue().getID())
                .collect(Collectors.joining(", "));
    }
}
