package linkedgraph;
import java.util.HashSet;
import java.util.Set;
public class Node{
    private Node REFERENCE;
    private int ID;
    public Node(int i){
        this.ID = i;
        this.REFERENCE = this;
    }
    public Node(Node n){
        this(n.ID);
    }
    public void cleanReference(){
        this.setReference(this);
    }
    public Node getReference(){
        if(this.REFERENCE==this)return this;
        return this.REFERENCE.getReference();
    }
    public void setReference(Node n){
        this.REFERENCE = n.getReference();
    }
    public int getId(){
        if(this.REFERENCE==this) return this.ID;
        return this.REFERENCE.getId();
    }
    public String toString(){
        if(this.REFERENCE==this){
            return "("+this.ID+":"+Integer.toHexString(this.hashCode())+")";
        }else{
            return "("+
                    this.ID+":"+
                    Integer.toHexString(this.hashCode())+"->"+
                    this.REFERENCE.toString()+
                    ")";
        }
    }
}
