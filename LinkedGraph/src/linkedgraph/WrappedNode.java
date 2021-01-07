package linkedgraph;

public class WrappedNode implements Comparable<WrappedNode>{
    public int index;
    public double distance;
    public WrappedNode(int i, double d){
        this.index = i;
        this.distance = d;
    }        
    public int compareTo(WrappedNode other){
        return -Double.compare(other.distance,this.distance);
    }
}
