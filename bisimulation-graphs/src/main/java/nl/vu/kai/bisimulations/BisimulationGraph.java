package nl.vu.kai.bisimulations;

import java.util.*;

public class BisimulationGraph {
    private final Set<BisimulationNode> nodes = new HashSet<>();
    private final Map<String,BisimulationNode> name2node = new HashMap<>();

    private final Map<BisimulationNode,BisimulationNode> equivalentOnes = new HashMap<>();

    private BisimulationNode topNode;

    public BisimulationNode getTopNode() {
        return topNode;
    }

    public void setTopNode(BisimulationNode topNode) {
        this.topNode = topNode;
    }

    public Collection<BisimulationNode> nodes(){
        return Collections.unmodifiableCollection(nodes);
    }

    public void removeNodes(Collection<BisimulationNode> toRemove){
        System.out.println("Removing "+toRemove.size());

        toRemove.forEach(BisimulationNode::setRemoved);
        nodes.removeAll(toRemove);
        nodes.forEach(BisimulationNodeOptimizer::optimizeNode);
    }

    public void removeNode(BisimulationNode node) {
        nodes.remove(node);
        node.setRemoved();
    }

    public void restrictToLevel(int maxLevel) {
        List<BisimulationNode> toRemove = new LinkedList<>();
        nodes.stream()
                .filter(n -> n.level()>maxLevel)
                .forEach(toRemove::add);
        System.out.println("Nodes: "+ nodes.size());

        removeNodes(toRemove);
    }

    public void restrictToMinSize(int minSize) {
        List<BisimulationNode> toRemove = new LinkedList<>();
        nodes.stream().filter(n -> n.size()<minSize).forEach(toRemove::add);
        removeNodes(toRemove);
    }

    public void addNode(String name, BisimulationNode node){
        System.out.println(name);
        //BisimulationNodeOptimizer.optimizeNode(node);
        name2node.put(name,node);
        nodes.add(node);
    }

    public BisimulationNode get(String name){
        return name2node.get(name);
    }

    public boolean contains(BisimulationNode bisimulationNode) {
        return nodes.contains(bisimulationNode);
    }

}
