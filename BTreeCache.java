import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * BTreeCache class implements a cache for BTree. It is made of BTree TreeNodes.
 * 
 * @author Sajia Zafreen
 *
 */

public class BTreeCache implements Iterable<TreeNode>{

    private LinkedList<TreeNode> cacheLinkedList;
    private int size;

    /**
     * Constructor: creates a Cache of BTree
     */
    public BTreeCache(int size) {
	cacheLinkedList = new LinkedList<TreeNode>();
	this.size = size;
    }

    /**
     * Returns Cache Linked List
     * 
     * @return Cache Linked List
     */
    public LinkedList<TreeNode> getCacheLinkedList() {
	return cacheLinkedList;
    }

    /**
     * Sets Cache Linked List
     * 
     * @param cacheLinkedList
     */
    public void setCacheLinkedList(LinkedList<TreeNode> cacheLinkedList) {
	this.cacheLinkedList = cacheLinkedList;
    }

    /**
     * Add to the fronto of the Cache
     * 
     * @param treeNode
     */
    public void addToFrontCache(TreeNode treeNode) {
	this.cacheLinkedList.addFirst(treeNode);
    }

    /**
     * Removes the last element in the Cache
     */
    public TreeNode removeLastCache() {
	return cacheLinkedList.removeLast();
    }

    /**
     * Removes the element in the index of the Cache
     * 
     * @param index is the index of the element to be removed
     */
    public void removeFromCache(int index) {
	cacheLinkedList.remove(index);
    }

    /**
     * Removes the element from the Cache
     * 
     * @param element to be removed from the Cache
     */
    public void removeFromCache(TreeNode element) {
	cacheLinkedList.remove(element);
    }

    /**
     * Returns the last element of the Cache
     * 
     * @return the last element of the cache
     */
    public TreeNode getLastCache() {
	return cacheLinkedList.getLast();
    }

    /**
     * Empties the Cache
     */
    public void clearCache() {
	cacheLinkedList.clear();
    }

    /**
     * Returns true if the Cache is full
     * 
     * @return true if Cache is full
     */
    public boolean cacheFull() {
	return (cacheLinkedList.size() == size);
    }

    /**
     * Returns true if the Cache is empty
     * 
     * @return true if the Cache is empty
     */
    public boolean emptyCache() {
	return (cacheLinkedList.size() == 0);
    }

    /**
     * Returns the Cache size
     * 
     * @return the Cache size
     */
    public int cacheSize() {
	return cacheLinkedList.size();
    }

    /**
     * Returns the TreeNode of given location
     * 
     * @param location of the TreeNode in the file
     * @return the TreeNode if it is in the Cache, else returns null
     */
    public TreeNode getCacheNode(int location) {
	for (TreeNode cacheNode : cacheLinkedList) {
	    if (cacheNode.getLocation() == location) {
		return cacheNode;
	    }
	}
	return null;
    }
    
    /**
     * Removes the node with location , if the list doesn't have it returns null.
     * @param location is the location of the tree node to be removed
     * @return the node removed
     */
    public TreeNode removeContains(int location) {
        TreeNode returnNode = null;
        TreeNode currNode;
        for (Iterator<TreeNode> it = this.iterator(); it.hasNext();) {
            currNode = it.next();
            if (currNode.getLocation() == location) {
                returnNode = currNode;
                it.remove();
                break;
            }
        }
        return returnNode;
    }

    @Override
    public String toString() {
	return Arrays.deepToString(cacheLinkedList.toArray());
    }
    
    @Override
    public Iterator<TreeNode> iterator(){
        return cacheLinkedList.iterator();
    }
}
