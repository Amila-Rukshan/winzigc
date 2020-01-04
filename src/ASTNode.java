import java.util.ArrayList;
import java.util.List;

// to represent nodes in AST
public class ASTNode{

    private String node_label;
    private List<ASTNode> childNodes;

    private ASTNode parent;

    ASTNode(String node_label){
        this.childNodes = new ArrayList<>();
        this.node_label = node_label;
    }

    public List<ASTNode> getChildNodes() {
        return childNodes;
    }

    int getChildNodesCount() {
        return childNodes.size();
    }

    public void setParentNode(ASTNode parent){
        this.parent = parent;
    }

    public void addChildNode(ASTNode child){
        this.childNodes.add(child);
        child.setParentNode(this);
    }

    ASTNode deleteASTNode(int index){
        return childNodes.remove(index);
    }

    void addChildAtIndex(int index, ASTNode child){
        childNodes.add(index, child);
        child.setParentNode(this);
    }

    public ASTNode getParent(){
        return parent;
    }

    public boolean isLeafNode(){
        return this.childNodes.size() == 0;
    }

    // Do a Depth First Traverse to print all the nodes
    public void DFTraverse(int depth){
        for(int i = 0 ; i < depth; i++ ) System.out.print(". ");
        System.out.print(this.node_label);
        System.out.println("("+ this.childNodes.size() +")");
        if(!isLeafNode()){
            for(ASTNode node: childNodes){
                node.DFTraverse(depth+1);
            }
        }
    }

    public String getNode_label() {
        return node_label;
    }
}