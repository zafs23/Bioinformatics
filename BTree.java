import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * BTree class creates a BTree consists of TreeNode which is made of TreeObjects
 * 
 * @author Sajia Zafreen
 *
 */

public class BTree {
    private int degree;
    private TreeNode rootNode; // always written at the end in its particular location selected when this node
			       // was created
    private int rootLocation;
    private TreeNode parent;
    private TreeNode firstChild;
    private TreeNode secondChild;// the node created after splitting
    private RandomAccessFile randomFile;
    private int lastAccessed;
    private final int METADATA_Offset = 12; // write from this offset
    private int treeNumNodes;
    private BTreeCache bTreeCache = null;
    private int cacheSize;
    // keys = binary format in long
    // treeObject = keys+ frequency

    /**
     * Constructor for the BTree when the BTree is created
     * 
     * @param degree    is the Degree of the BTree
     * @param fileName  is the fileName where the BTree will be written
     * @param cacheSize is the implemented Cache Size
     * @throws IOException
     */
    public BTree(int degree, String fileName, int cacheSize) throws IOException {
	this.degree = optimalDegree(degree);
	this.lastAccessed = METADATA_Offset;
	this.rootNode = new TreeNode(this.degree);
	this.parent = null;
	this.firstChild = null;
	this.secondChild = null;
	this.setTreeNumNodes(0);// counting the root node
	this.randomFile = new RandomAccessFile(fileName, "rw");
	allocateNode(rootNode);
	rootLocation = lastAccessed;
	this.cacheSize = cacheSize;
	if (cacheSize > 0) {
	    this.bTreeCache = new BTreeCache(cacheSize);
	}
    }

    /**
     * Constructor for the BTree when the BTree is searched
     * 
     * @param randomFile is the randomFile from where the BTree will be searched
     * @param cacheSize  is cacheSize implemented
     */
    public BTree(RandomAccessFile randomFile, int cacheSize) {
	this.randomFile = randomFile;
	this.degree = 0;
	this.rootNode = null;
	this.rootLocation = 0;
	this.treeNumNodes = 0;
	if (cacheSize > 0) {
	    bTreeCache = new BTreeCache(cacheSize);
	}
    }

    /**
     * Returns the degree it is equal to or greater than 2 , otherwise returns
     * optimal degree
     * 
     * @param degree is the user input degree
     * @return the optimal dgree for this simualtion
     */
    private int optimalDegree(int degree) {
	if (degree >= 2) {
	    return degree;
	} else { // calculate optimal degree
	    int keyByte = 8 + 4 + 4;// 8 for long subString, 4 for int frequency, 4 for NIL/DELETED/OCCUPIED
	    int pointer = 4;//
	    int metaData = 4 + 4 + 1; // how many elements, location, leaf or not
	    int optimalDegree = (4096 + keyByte - pointer - metaData) / 40;// 16(2t-1)+4(2t+1)+ 9 <=4096,
									   // t=optimalDegree
	    return optimalDegree;
	}
    }

    /**
     * BTree insert method
     * 
     * @param treeObject is the treeObject to be inserted
     */
    public void bTree_insert(TreeObject treeObject) { // Insert condition: 0= NIL, 1= OCCUPIED, 2= DELETED
	TreeNode root = rootNode;
	if (root.getCurrentNumKeys() == (2 * degree - 1)) {// full node
	    TreeNode split = new TreeNode(degree);// this location n
	    allocateNode(split);

	    // root's parent pointer is never set. thus default to zero
	    rootNode = split;
	    setRootLocation(split.getLocation());
	    parent = split;
	    firstChild = root; // this one is full

	    split.setLeaf(false);
	    split.setCurrentNumKeys(0); // no movement yet

	    split.setChildPointer(0, root.getLocation());// or root.getLocation
	    split.setChild(0, root);
	    root.setParentPointer(split.getLocation());// just allocated

	    bTree_Split_Child(split, 0, root);

	    bTree_Insert_Nonfull(split, treeObject);// will start from the root, and split is the new root
	} else {
	    bTree_Insert_Nonfull(root, treeObject);
	}
    }

    /**
     * Helper method for the BTree insert. It inserts treeObject when the node is
     * non full
     * 
     * @param parentNode is the parentNode
     * @param treeObject is the treeObject
     */
    private void bTree_Insert_Nonfull(TreeNode parentNode, TreeObject treeObject) {
	boolean contains = false;
	int containIndex = 0;
	if (parentNode.isLeaf()) {
	    for (int i = 0; i < parentNode.getCurrentNumKeys(); i++) {
		if (treeObject.compareTo(parentNode.getTreeObject(i)) == 0) {
		    contains = true;
		    containIndex = i;
		}
	    }

	    if (contains && parentNode.getTreeObject(containIndex).getKeyCondition() != 0) {
		parentNode.getTreeObject(containIndex).incrementFrequency();
	    } else if (contains && parentNode.getTreeObject(containIndex).getKeyCondition() == 0) {
		parentNode.setTreeObject(containIndex, treeObject);
		parentNode.incrementCurrentNumKeys();
		parentNode.getTreeObject(containIndex).setKeyCondition(1);
	    } else {
		int index = parentNode.getCurrentNumKeys();
		while (index >= 1 && treeObject.compareTo(parentNode.getTreeObject(index - 1)) < 0) {
		    parentNode.setTreeObject(index, parentNode.getTreeObject(index - 1));
		    index--;
		}
		parentNode.setTreeObject(index, treeObject);
		parentNode.incrementCurrentNumKeys();
		parentNode.getTreeObject(index).setKeyCondition(1);
	    }

	    write_Node(parentNode);
	} else {// if not leaf only the rootNode will descend and have parent,
		// firstChild<-descend to this child
	    for (int i = 0; i < parentNode.getCurrentNumKeys(); i++) {
		if (treeObject.compareTo(parentNode.getTreeObject(i)) == 0) {
		    contains = true;
		    containIndex = i;
		}
	    }
	    if (contains && parentNode.getTreeObject(containIndex).getKeyCondition() != 0) {
		parentNode.getTreeObject(containIndex).incrementFrequency();
		write_Node(parentNode);
	    } else {
		int index = parentNode.getCurrentNumKeys();
		while (index >= 1 && treeObject.compareTo(parentNode.getTreeObject(index - 1)) < 0) {
		    index--;
		}
		TreeNode childNode = bTree_Read(parentNode.getChildPointer(index));
		parent = parentNode;
		firstChild = childNode;
		if (childNode.getCurrentNumKeys() == (2 * degree - 1)) {
		    bTree_Split_Child(parentNode, index, childNode);
		    for (int i = 0; i < parentNode.getCurrentNumKeys(); i++) {
			if (treeObject.compareTo(parentNode.getTreeObject(i)) == 0) {
			    contains = true;
			    containIndex = i;
			}
		    }
		    if (contains && parentNode.getTreeObject(containIndex).getKeyCondition() != 0) {
			parentNode.getTreeObject(containIndex).incrementFrequency();
			write_Node(parentNode);
		    } else {

			if (treeObject.compareTo(parentNode.getTreeObject(index)) > 0) {
			    bTree_Insert_Nonfull(secondChild, treeObject);
			} else {
			    bTree_Insert_Nonfull(childNode, treeObject);
			}
		    }
		} else {
		    bTree_Insert_Nonfull(childNode, treeObject);// something is wrong here
		}
	    }
	}

    }

    // split_child(X, i, Y) meaning X's ith child is Y, where Y is full (otherwise
    // no need of splitting)
    // this method creates a new node for the root and then split it in half
    /**
     * Helper method of the BTree insert method. It splits the firstChildNode to two
     * nodes and move a key to the parentNode.
     * 
     * @param parentNode     is the parent node of the firstChildNode
     * @param index          is the index of the firstChildNode
     * @param firstChildNode is the firstChildNode
     */
    private void bTree_Split_Child(TreeNode parentNode, int index, TreeNode firstChildNode) {
	TreeNode secondChildNode = new TreeNode(degree); // is the z

	allocateNode(secondChildNode);

	parentNode.setChild(index, firstChildNode);// first child will be here, , second child will be in (index+1)
	parentNode.setChildPointer(index, firstChildNode.getLocation());

	secondChildNode.setLeaf(firstChildNode.getLeaf());
	secondChildNode.setCurrentNumKeys(degree - 1); // both node now has t-1 children

	// setting keys, frequency, key type
	for (int j = 0; j < (degree - 1); j++) {
	    // largest treeObjects from y will go to z
	    secondChildNode.setTreeObject(j, firstChildNode.getTreeObject(j + degree));
	    secondChildNode.setTreefrequency(j, firstChildNode.getTreefrequency(j + degree));
	    secondChildNode.setNodeKeyCondition(j, firstChildNode.getNodeKeyCondition(j + degree));
	}

	if (!firstChildNode.isLeaf()) { // if leaf there is no children
	    for (int j = 0; j < degree; j++) {
		secondChildNode.setChildPointer(j, firstChildNode.getChildPointer(j + degree));
	    }
	}
	firstChildNode.setCurrentNumKeys(degree - 1);// rest of the nodes will be deleted, when re-write this

	// current node was set to something number of keys already
	// but we are not using current.getCurrentNumKeys()+1, as we have an index
	// starting from 0
	for (int j = parentNode.getCurrentNumKeys(); j > index; j--) {// as we are relocating childPointer, they are +1
								      // from the childKeys
	    parentNode.setChildPointer(j + 1, parentNode.getChildPointer(j));
	}

	parentNode.setChildPointer(index + 1, secondChildNode.getLocation());
	parentNode.setChild(index + 1, secondChildNode);
	secondChildNode.setParentPointer(parentNode.getLocation());

	for (int j = parentNode.getCurrentNumKeys() - 1; j > index - 1; j--) {
	    parentNode.setTreeObject(j + 1, parentNode.getTreeObject(j));
	}
	parentNode.setTreeObject(index, firstChildNode.getTreeObject(degree - 1));
	parentNode.getTreeObject(index).setKeyCondition(firstChildNode.getTreeObject(degree - 1).getKeyCondition());
	parentNode.incrementCurrentNumKeys();

	this.firstChild = firstChildNode;
	this.secondChild = secondChildNode;
	this.parent = parentNode;

	write_Node(firstChildNode);
	write_Node(secondChildNode);
	write_Node(parentNode);
    }

    /**
     * Writes the nodes to the file if the cache is not implemented, otherwise
     * writes in the Cache
     * 
     * @param treeNode is the treeNode to be written
     */
    private void write_Node(TreeNode treeNode) {
	if (cacheSize > 0) {
	    if ((bTreeCache.removeContains(treeNode.getLocation())) != null) {
		bTreeCache.addToFrontCache(treeNode);
	    } else {
		if (bTreeCache.cacheFull()) {
		    disk_Write(bTreeCache.removeLastCache());
		}
		bTreeCache.addToFrontCache(treeNode);
	    }
	} else {
	    disk_Write(treeNode);
	}
    }

    /**
     * Writes the treeNode to the disk file at the node's allocated location
     * 
     * @param treeNode is the treeNode to be written
     */
    private void disk_Write(TreeNode treeNode) {
	int location = treeNode.getLocation();
	try {
	    randomFile.seek(location); // set pointer to the start of the location
	    ByteBuffer finalBuffer = ByteBuffer.allocate(nodeLength());// final Buffer

	    // adjusting node meta data
	    byte[] boolArr = new byte[] { (byte) (treeNode.getLeaf() ? 1 : 0) };
	    finalBuffer = finalBuffer.put(boolArr);

	    int[] metaArr = { treeNode.getCurrentNumKeys(), treeNode.getLocation() };
	    for (int i = 0; i < 2; i++) {
		ByteBuffer intBufferM = ByteBuffer.allocate(4);
		byte[] intArrM = intBufferM.putInt(metaArr[i]).array();
		finalBuffer = finalBuffer.put(intArrM);
	    }

	    // byte [] vOut = new byte[]{(byte) (vIn?1:0)};
	    // adjusting keys
	    for (int i = 0; i < treeNode.getCurrentNumKeys(); i++) {
		ByteBuffer longBuffer = ByteBuffer.allocate(8);
		byte[] longArr = longBuffer.putLong(treeNode.getTreeObject(i).getBinaryKey()).array();
		finalBuffer = finalBuffer.put(longArr);

		ByteBuffer intBuffer = ByteBuffer.allocate(4);
		byte[] intArr = intBuffer.putInt(treeNode.getTreeObject(i).getFrequency()).array();
		finalBuffer = finalBuffer.put(intArr);

		ByteBuffer intKBuffer = ByteBuffer.allocate(4);
		byte[] intKArr = intKBuffer.putInt(treeNode.getNodeKeyCondition(i)).array();
		finalBuffer = finalBuffer.put(intKArr);
	    }

	    // adjusting child locations
	    for (int i = 0; i < treeNode.getCurrentNumKeys() + 1; i++) {
		ByteBuffer intBufferC = ByteBuffer.allocate(4);
		byte[] intArrC = intBufferC.putInt(treeNode.getChildPointer(i)).array();
		finalBuffer = finalBuffer.put(intArrC);
	    }

	    // adjusting parent location
	    ByteBuffer intBufferP = ByteBuffer.allocate(4);
	    byte[] intArrP = intBufferP.putInt(treeNode.getParentPointer()).array();
	    finalBuffer = finalBuffer.put(intArrP);

	    byte[] finalBufferArr = finalBuffer.array();
	    randomFile.write(finalBufferArr, 0, finalBufferArr.length);

	} catch (IOException e) {
	    System.err.println("Cannot write to file");
	    e.printStackTrace();
	}

    }

    /**
     * Reads the BTree from the Cache if Cache is implemented, if Cache doesn't have
     * the node reads from the file. Otherwise reads from the BTree file.
     * 
     * @param location is the location on the file of the BTree node to be read
     * @return the TreeNode
     */
    public TreeNode bTree_Read(int location) {
	if (cacheSize > 0) {
	    TreeNode readNode;
	    if ((readNode = bTreeCache.removeContains(location)) != null) {
		bTreeCache.addToFrontCache(readNode);
		return readNode;
	    } else {
		readNode = disk_Read(location);
		if (bTreeCache.cacheFull()) {
		    disk_Write(bTreeCache.removeLastCache());
		}
		bTreeCache.addToFrontCache(readNode);
		return readNode;
	    }
	} else {
	    return disk_Read(location);
	}
    }

    /**
     * Reads the node at the location from the BTree file.
     * 
     * @param location of the BTree Node
     * @return the BTree node
     */
    private TreeNode disk_Read(int location) {
	TreeNode readNode = new TreeNode(degree);
	try {
	    randomFile.seek(location);
	    byte[] readBuffer = new byte[4096];

	    // reading boolean
	    randomFile.read(readBuffer, 0, 1);
	    readNode.setLeaf(readBuffer[0] != 0);
	    // System.out.print("disk read.. leaf:" + readNode.getLeaf());

	    // read next metaData , two Ints
	    randomFile.read(readBuffer, 0, 8);
	    ByteBuffer numBuf = ByteBuffer.wrap(readBuffer);
	    int numKeys = numBuf.getInt();
	    readNode.setCurrentNumKeys(numKeys);
	    readNode.setLocation(numBuf.getInt());

	    // already pointer is changed to location position of the node
	    randomFile.read(readBuffer, 0, nodeByteLength(numKeys));
	    ByteBuffer finalBuff = ByteBuffer.wrap(readBuffer);

	    // set keys
	    for (int i = 0; i < numKeys; i++) {
		readNode.setTreeKey(i, finalBuff.getLong());
		readNode.setTreefrequency(i, finalBuff.getInt());
		readNode.setNodeKeyCondition(i, finalBuff.getInt());
	    }

	    // set child location
	    for (int i = 0; i < numKeys + 1; i++) {
		readNode.setChildPointer(i, finalBuff.getInt());
	    }

	    // set parent location
	    readNode.setParentPointer(finalBuff.getInt());

	} catch (IOException e) {
	    System.err.println("Cannot Read file");
	    e.printStackTrace();
	}
	return readNode;
    }

    /**
     * Searches the BTree if it consists the treeLong object. The node should be the
     * root node to start.
     * 
     * @param node     is supposed to be the root node to search the entire tree.
     * @param treeLong is the object to be matched in the BTree
     * @return the node if found otherwise returns null
     */
    public TreeObject bTree_Search(TreeNode node, long treeLong) {
	int index = 0;
	TreeObject nullTreeObject = null;
	TreeObject treeObject = new TreeObject(treeLong);
	while (index < node.getCurrentNumKeys() && treeObject.compareTo(node.getTreeObject(index)) > 0) {
	    index++;
	}
	if (index < node.getCurrentNumKeys() && treeObject.compareTo(node.getTreeObject(index)) == 0) {
	    return node.getTreeObject(index);
	} else if (node.isLeaf()) {
	    return nullTreeObject;
	} else {
	    TreeNode childNode = bTree_Read(node.getChildPointer(index));
	    return bTree_Search(childNode, treeLong);
	}
    }

    // return this nodes location after allocating
    /**
     * Allocate location of the TreeNode in the BTree file
     * 
     * @param treeNode is the node whose location will be allocated
     */
    private void allocateNode(TreeNode treeNode) {
	treeNode.setLocation(lastAccessed);
	lastAccessed = lastAccessed + nodeLength();
	incrementTreeNumNodes();
    }

    /**
     * Returns the Byte length of only the node depending on the keys in the node
     * 
     * @param numKeys is the number of keys in the node
     * @return the Byte length of the node
     */
    private int nodeByteLength(int numKeys) {// except the node meta data
	int key = 8 + 4 + 4;
	int child = 4;
	int parent = 4;
	return numKeys * key + (numKeys + 1) * child + parent;
    }

    /**
     * Calculated the general node length of each node including the metadata of the
     * tree node
     * 
     * @return
     */
    private int nodeLength() {// meta data included
	int metaData = 9;
	int child = 4;
	int parent = 4;
	int treeObject = 16;
	return metaData + (2 * degree - 1) * (treeObject) + 2 * degree * child + parent;
    }

    /**
     * Writes the BTree meta_data on the file. If cache was implemented then first
     * empties the cache to the file. Then writes the BTree meta_data.
     */
    public void write_Tree_MetaData() {
	// if cache implemented at last writing tree data from cache to file
	write_Cache_ToFile(); // safe, as checked if cache is implemented

	// write tree meta_data
	int[] arr = { rootLocation, treeNumNodes, degree };
	try {
	    ByteBuffer metaDataFinal = ByteBuffer.allocate(METADATA_Offset);
	    randomFile.seek(0);
	    for (int i = 0; i < arr.length; i++) {
		ByteBuffer metaData = ByteBuffer.allocate(4);
		byte[] metaDataBuff = metaData.putInt(arr[i]).array();
		metaDataFinal = metaDataFinal.put(metaDataBuff);
	    }

	    byte[] byteArr = metaDataFinal.array();
	    randomFile.write(byteArr, 0, METADATA_Offset);

	    // at the last this meta_Data will be written
	    randomFile.close();
	} catch (IOException e) {
	    System.err.println("Cannot write BTree metaData");
	    e.printStackTrace();
	}
    }

    /**
     * Writes the Cache nodes to the file.
     */
    public void write_Cache_ToFile() {
	if (cacheSize > 0) {
	    for (TreeNode cacheNode : bTreeCache.getCacheLinkedList()) {
		disk_Write(cacheNode);
	    }

	    bTreeCache.clearCache();
	}
    }

    /**
     * Reads the BTree meta_data
     */
    public void read_Tree_MetaData() {
	byte[] metaByte = new byte[100];
	try {
	    randomFile.seek(0);
	    randomFile.read(metaByte, 0, METADATA_Offset);
	    ByteBuffer metaDataBuff = ByteBuffer.wrap(metaByte);
	    rootLocation = metaDataBuff.getInt();
	    treeNumNodes = metaDataBuff.getInt();
	    degree = metaDataBuff.getInt();
	    rootNode = bTree_Read(rootLocation);
	} catch (IOException e) {
	    System.err.println("Cannot read Tree MetaData");
	    e.printStackTrace();
	}
    }

    /**
     * Prints the whole BTree in the standard output stream.
     * 
     * @param node   is supposed to be the root node
     * @param length is the length of the DNA sequence
     */
    public void treeTraverseInOrder(TreeNode node, int length) {
	if (node.getLeaf()) {
	    for (int i = 0; i < node.getCurrentNumKeys(); ++i) {
		System.out.print(BinaryFormat.longBinaryString(node.getTreeObject(i).getBinaryKey(), length));
		System.out.println(": " + node.getTreeObject(i).getFrequency());
	    }
	    return;
	}
	for (int i = 0; i < node.getCurrentNumKeys() + 1; i++) {
	    TreeNode currNode = new TreeNode(degree);
	    currNode = bTree_Read(node.getChildPointer(i));
	    treeTraverseInOrder(currNode, length);
	    if (i < node.getCurrentNumKeys()) {
		System.out.print(BinaryFormat.longBinaryString(node.getTreeObject(i).getBinaryKey(), length));
		System.out.println(": " + node.getTreeObject(i).getFrequency());
	    }
	}
    }

    /**
     * Write tree nodes in order to the file
     * 
     * @param node   is the root node
     * @param length is the length of DNA sequence
     * @param writer
     * @throws IOException
     */
    public void writeTreeInOderToFile(TreeNode node, int length, PrintWriter writer) throws IOException {
	// fileName = fileName + ".BTree.dump." + length;
	// ***** must must must use close writer after this method *****

	if (node.getLeaf()) {
	    for (int i = 0; i < node.getCurrentNumKeys(); ++i) {
		writer.print(BinaryFormat.longBinaryString(node.getTreeObject(i).getBinaryKey(), length));
		writer.print(": " + node.getTreeObject(i).getFrequency());
		writer.print("\n");
	    }
	    return;
	}
	for (int i = 0; i < node.getCurrentNumKeys() + 1; i++) {
	    TreeNode currNode = new TreeNode(degree);
	    currNode = bTree_Read(node.getChildPointer(i));
	    writeTreeInOderToFile(currNode, length, writer);
	    if (i < node.getCurrentNumKeys()) {
		writer.print(BinaryFormat.longBinaryString(node.getTreeObject(i).getBinaryKey(), length));
		writer.println(": " + node.getTreeObject(i).getFrequency());
	    }
	}
    }

    /**
     * Returns the BTree degree
     * 
     * @return the BTree degree
     */
    public int getDegree() {
	return degree;
    }

    /**
     * Sets the BTree degree
     * 
     * @param degree is the degree to be set
     */
    public void setDegree(int degree) {
	this.degree = degree;
    }

    /**
     * Returns the root node of the BTree
     * 
     * @return the root node
     */
    public TreeNode getRootNode() {
	return rootNode;
    }

    /**
     * Sets the root node
     * 
     * @param rootNode is the root node to be set
     */
    public void setRootNode(TreeNode rootNode) {
	this.rootNode = rootNode;
    }

    /**
     * Returns the second child of the node
     * 
     * @return
     */
    public TreeNode getSecondChildNode() {
	return secondChild;
    }

    /**
     * Sets the current child node
     * 
     * @param currentChildNode is to be set
     */
    public void setSecondChildNode(TreeNode currentChildNode) {
	this.secondChild = currentChildNode;
    }

    /**
     * Returns the first child of the node
     * 
     * @return
     */
    public TreeNode getFirstChildNode() {
	return firstChild;
    }

    /**
     * Sets the first child of the node
     * 
     * @param firstChildNode
     */
    public void setFirstChildNode(TreeNode firstChildNode) {
	this.firstChild = firstChildNode;
    }

    /**
     * Returns the parent node of the tree node
     * 
     * @return the parent node of the tree node
     */
    public TreeNode getParentNode() {
	return parent;
    }

    /**
     * Sets the parent node of the tree node
     * 
     * @param parentNode is the node to be set
     */
    public void setParentNode(TreeNode parentNode) {
	this.parent = parentNode;
    }

    /**
     * Returns the file location of the root
     * 
     * @return the file location of the root
     */
    public int getRootLocation() {
	return rootLocation;
    }

    /**
     * Sets the file location of the root
     * 
     * @param rootLocation is the location to be set
     */
    public void setRootLocation(int rootLocation) {
	this.rootLocation = rootLocation;
    }

    /**
     * Increment number of current keys by one of the Node
     */
    public void incrementTreeNumNodes() {
	this.treeNumNodes++;
    }

    /**
     * Returns the number of current keys in the node
     * 
     * @return the number of current keys in the node
     */
    public int getTreeNumNodes() {
	return treeNumNodes;
    }

    /**
     * Sets the number of keys of the node
     * 
     * @param numNodes is the number to be set as the keys of the node
     */
    public void setTreeNumNodes(int numNodes) {
	this.treeNumNodes = numNodes;
    }

}
