package linkedgraph;

public class WrapperNode implements Comparable<WrapperNode>{
    public int index;
    public int distance;
    public WrapperNode(int i, int d){
        this.index = i;
        this.distance = d;
    }        
    public int compareTo(WrapperNode other){
        return Integer.compare(other.distance,this.distance);
    }
}
