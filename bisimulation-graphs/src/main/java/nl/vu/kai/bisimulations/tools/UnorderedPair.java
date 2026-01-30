package nl.vu.kai.bisimulations.tools;

public class UnorderedPair<T> extends Pair<T,T> {
    public UnorderedPair(T a, T b){
        super(a,b);
    }

    @Override
    public boolean equals(Object o) {
        if(this==o)
            return true;
        if(o==null || !(o instanceof  UnorderedPair))
            return false;
        UnorderedPair other = (UnorderedPair) o;
        return (getKey().equals(other.getKey()) && getValue().equals(other.getValue())) ||
                (getKey().equals(other.getValue()) && getValue().equals(other.getKey()));
    }

    @Override
    public int hashCode(){
        return getKey().hashCode()+getValue().hashCode();
    }
}
