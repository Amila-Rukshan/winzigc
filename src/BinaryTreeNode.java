

// binary tree node to represent nodes in AST
public class BinaryTreeNode{

    private String node_label;

    private BinaryTreeNode left;

    private BinaryTreeNode right;

    private int childCount;

    BinaryTreeNode(String node_label){
        this.node_label = node_label;
    }

    public void setLeftChild(BinaryTreeNode node) {
        left = node;
    }

    public void setRightChild(BinaryTreeNode node) {
        right = node;
    }

    public void setChildCount(int c) {
        childCount = c;
    }

    // Pre Order Traverse with indented printing
    public void PreOrderTraverse(int indentSize){
        for(int i = 0 ; i < indentSize; i++ ) System.out.print(". ");
        System.out.print(this.node_label);
        System.out.println("("+ childCount +")");

        if(this.left != null){
            left.PreOrderTraverse(indentSize + 1);
        }

        if(this.right != null){
            right.PreOrderTraverse(indentSize);
        }
    }
}
