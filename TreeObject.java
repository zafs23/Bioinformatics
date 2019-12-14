
/**
 * TreeObject creates a TreeObject from the binary key object
 * 
 * @author Sajia Zafreen
 *
 */
public class TreeObject implements Comparable<TreeObject> {
    // treeObject = keys+ frequency
    private long binaryKey; // keys = binary format in long
    private int frequency;
    private int keyCondition; // condition 0= NIL, 1= OCCUPIED, 2= DELETED

    /**
     * Constructor of TreeObject
     * 
     * @param binaryKey is the converted string to binary format key
     */
    public TreeObject(long binaryKey) {
	this.setBinaryKey(binaryKey);
	this.frequency = 1;
	this.keyCondition = 0;
    }

    /**
     * Returns the binary key
     * 
     * @return the binary key
     */
    public long getBinaryKey() {
	return binaryKey;
    }

    /**
     * Set the binary key
     * 
     * @param binaryKey is the binary key to be set
     */
    public void setBinaryKey(long binaryKey) {
	this.binaryKey = binaryKey;
    }

    /**
     * Get the frequency
     * 
     * @return the frequency
     */
    public int getFrequency() {
	return frequency;
    }

    /**
     * Set the frequency
     * 
     * @param frequency is the frequency to be set
     */
    public void setFrequency(int frequency) {
	this.frequency = frequency;
    }

    /**
     * Increment frequency by 1
     */
    public void incrementFrequency() { // use this when duplicate keys are encountered
	this.frequency++;
    }

    /**
     * Returns the key condition of this TreeObject
     * 
     * @return the TreeObject keyCondition
     */
    public int getKeyCondition() {
	return keyCondition;
    }
    
    /**
     * Sets the keyCondition
     * @param keyCondition to be set
     */
    public void setKeyCondition(int keyCondition) {
	this.keyCondition = keyCondition;
    }
    

    @Override
    public String toString() {
	return this.binaryKey + ": " + frequency;
    }

    @Override
    public int compareTo(TreeObject treeObject) {
	if (this.binaryKey > treeObject.getBinaryKey()) {
	    return 1;
	} else if (this.binaryKey < treeObject.getBinaryKey()) {
	    return -1;
	} else {
	    return 0;
	}
    }
}
