/**
 * Creates a TreeNode of BTree with child pointers and parent pointer.
 * 
 * @author Sajia Zafreen
 *
 */
public class TreeNode {
    private int parentPointer; // parent Location
    private int[] childPointers; // list of child location
    private TreeNode[] childNodes;
    private TreeObject[] treeObjects; // list of children
    private int currentNumKeys; // number of keys currently stored
    private boolean leaf;
    private int location; // own location in the file
    private int degree;
    // keys = binary format in long
    // treeObject = keys+ frequency

    /**
     * Constructor of the TreeNode
     * 
     * @param degree is the degree of the BTree
     */
    public TreeNode(int degree) { // objects when adding
	this.setDegree(degree);
	this.parentPointer = 0; // 1 parent pointer
	this.childPointers = new int[2 * degree];// 2t childPointers for full node
	this.childNodes = new TreeNode[2 * degree];
	this.treeObjects = new TreeObject[2 * degree - 1];
	for (int i = 0; i < treeObjects.length; i++) {
	    treeObjects[i] = new TreeObject((long) (0));
	}
	this.currentNumKeys = 0;
	this.leaf = true; // when starts it is a leaf node
	this.location = 0;
    }

    /**
     * Sets the TreeObject
     * 
     * @param index      is the index of the TreeObject
     * @param treeObject is the treeObject to be set
     */
    public void setTreeObject(int index, TreeObject treeObject) {
	this.treeObjects[index] = treeObject;
    }

    /**
     * Returns the treeObject
     * 
     * @param index is the index in the tree node
     * @return the treeObject
     */
    public TreeObject getTreeObject(int index) {
	return this.treeObjects[index];
    }

    /**
     * Returns the parent pointer of the treeNode
     * 
     * @return the parent pointer
     */
    public int getParentPointer() {
	return parentPointer;
    }

    /**
     * Sets the parent pointer of the TreeNode
     * 
     * @param parentPointer
     */
    public void setParentPointer(int parentPointer) {
	this.parentPointer = parentPointer;
    }

    /**
     * Returns the child pointer at the index
     * 
     * @param index is the index int he tree node
     * @return the child pointer
     */
    public int getChildPointer(int index) {
	return this.childPointers[index];
    }

    /**
     * Set the child pointer at the index in the tree node
     * 
     * @param index         is the index in the tree node
     * @param childLocation of the child pointer
     */
    public void setChildPointer(int index, int childLocation) {
	this.childPointers[index] = childLocation; // the ith child is at the childLocation
    }

    /**
     * Return the current keys of the tree node
     * 
     * @return the current keys is the number of keys to be set
     */
    public int getCurrentNumKeys() {
	return currentNumKeys;
    }

    /**
     * Sets the current keys of the tree node
     * 
     * @param currentNumKeys is the current keys to be set
     */
    public void setCurrentNumKeys(int currentNumKeys) {
	this.currentNumKeys = currentNumKeys;
    }

    /**
     * Increment the number of current keys by one
     */
    public void incrementCurrentNumKeys() {
	currentNumKeys++;
    }

    /**
     * Returns the node is leaf or not
     * 
     * @return if the node is leaf or not
     */
    public boolean isLeaf() {
	return leaf;
    }

    /**
     * Sets the leaf of the tree node
     * 
     * @param leaf to be set of the tree node
     */
    public void setLeaf(boolean leaf) {
	this.leaf = leaf;
    }

    /**
     * Returns the leaf of the tree node
     * 
     * @return the leaf of the tree node
     */
    public boolean getLeaf() {
	return this.leaf;
    }

    /**
     * Returns the tree node location in the file
     * 
     * @return the location of the node in the file
     */
    public int getLocation() {
	return location;
    }

    /**
     * Sets node location of the tree node
     * 
     * @param location is to be set for the tree node
     */
    public void setLocation(int location) {
	this.location = location;
    }

    /**
     * Sets the child of the tree node
     * 
     * @param index     is the index in the node
     * @param childNode is the node to be set
     */
    public void setChild(int index, TreeNode childNode) {
	this.childNodes[index] = childNode;
    }

    /**
     * Returns the child at the index location of the tree node
     * 
     * @param index is the index of the child's position in the tree node
     * @return the child at the index location of the tree node
     */
    public TreeNode getChild(int index) {
	return this.childNodes[index];
    }

    /**
     * Returns the degree of the tree node
     * 
     * @return the degree of the tree node
     */
    public int getDegree() {
	return degree;
    }

    /**
     * Sets the degree of the tree node
     * 
     * @param degree is the degree to be set
     */
    public void setDegree(int degree) {
	this.degree = degree;
    }

    /**
     * Sets the tree node key at the index
     * 
     * @param index is the index in the tree node
     * @param key   is the key to be set
     */
    public void setTreeKey(int index, long key) {
	this.treeObjects[index].setBinaryKey(key);
    }

    /**
     * Sets the tree node frequency of the tree object located at the index
     * 
     * @param index     is the index of the key in the node
     * @param frequency is the frequency of the tree object at the index
     */
    public void setTreefrequency(int index, int frequency) {
	this.treeObjects[index].setFrequency(frequency);
    }

    /**
     * Returns the tree object frequency at the index
     * 
     * @param index is the index of the key in the node
     * @return the tree object frequency at the index
     */
    public int getTreefrequency(int index) {
	return this.treeObjects[index].getFrequency();
    }

    /**
     * Sets the node key condition of the tree object at the index
     * 
     * @param index     is the index of the key object whose key condition is to be
     *                  set
     * @param condition is the current condition (NIL, DELETED OR OCCUPIED) of the
     *                  tree object at the index
     */
    public void setNodeKeyCondition(int index, int condition) {
	this.treeObjects[index].setKeyCondition(condition);
    }

    /**
     * Sets the node key condition of the tree object at the index
     * 
     * @param index is the index of the key object whose key condition is to be
     *              returned
     * @return the current key condition of the tree object in the index location
     */
    public int getNodeKeyCondition(int index) {
	return this.treeObjects[index].getKeyCondition();
    }

    @Override
    public String toString() {
	String nodeString = "";
	for (int i = 0; i < currentNumKeys; i++) {
	    nodeString = nodeString + treeObjects[i].toString();
	}
	return nodeString;
    }

}
