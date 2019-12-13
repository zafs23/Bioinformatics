import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Scanner;

/**
 * GeneBankCreateBTree creates a BTree from an user input "GeneBank" files of
 * Gene DNA data.
 * 
 * @author Sajia Zafreen
 */

public class GeneBankCreateBTree {
    /**
     * Driver method for creating BTree from GeneBank files
     * 
     * @param args
     */
    public static void main(String args[]) {
	if (args.length < 4 || args.length > 6) {
	    printUsage();
	    System.exit(1);
	}
	int cache;
	int degree;
	String fileName;
	int length;
	int debug = 0;
	int cacheSize = 0;
	try {
	    cache = Integer.parseInt(args[0]);
	    if (cache < 0 || cache > 1) {
		System.err.println("Cache entry is 0 for without cache, 1 for using Cache");
		printUsage();
		System.exit(1);
	    }
	    degree = Integer.parseInt(args[1]);
	    if (degree == 1) {
		System.err.println("Degree cannot be 1.");
		printUsage();
		System.exit(1);
	    } else if (degree < 0) {
		System.err.println("Degree cannot be negative");
		printUsage();
		System.exit(1);
	    }
	    fileName = args[2].toString();
	    length = Integer.parseInt(args[3]);
	    // checking length condition
	    if (length < 1 || length > 31) {
		System.err.println("Length should be between 1 and 31 inclusive");
		printUsage();
		System.exit(1);
	    }

	    if (cache == 0) {
		if (args.length == 5) {
		    try {
			debug = Integer.parseInt(args[4]);
			if (debug < 0 || debug > 1) {
			    System.err.println("Debug should be a 0 or 1");
			    printUsage();
			    System.exit(1);
			}
		    } catch (NumberFormatException e) {
			System.err.println("Debug should be a 0 or 1");
			printUsage();
			System.exit(1);
		    }
		} else if (args.length == 6) {
		    System.err.println("If cache is 0 then no cache will be created.");
		    printUsage();
		    System.exit(1);
		}
	    } else if (cache == 1) {
		if (args.length == 5) {
		    try {
			cacheSize = Integer.parseInt(args[4]);
			if (cacheSize < 2) {
			    System.err.println("Cache size should not be negative, 0 or 1");
			    printUsage();
			    System.exit(1);
			}
		    } catch (NumberFormatException e) {
			System.err.println("Cache size should be number");
			printUsage();
			System.exit(1);
		    }
		} else if (args.length == 6) {
		    try {
			cacheSize = Integer.parseInt(args[4]);
			if (cacheSize < 2) {
			    System.err.println("Cache size should not be negative, 0 or 1");
			    printUsage();
			    System.exit(1);
			}
			debug = Integer.parseInt(args[5]);
			if (debug < 0 || debug > 1) {
			    System.err.println("Debug should be a 0 or 1");
			    printUsage();
			    System.exit(1);
			}
		    } catch (NumberFormatException e) {
			System.err.println("Cache size should be number");
			printUsage();
			System.exit(1);
		    }
		} else {
		    System.err.println("If cache is 1, then you need to input Cache Size.");
		    printUsage();
		    System.exit(1);
		}
	    }

	    // *** writing BTree ***
	    try {
		createBTree(degree, fileName, length, cacheSize);
	    } catch (IOException e) {
		System.err.println("Cannot parsefile");
		printUsage();
		System.exit(1);
	    }

	    if (debug == 1) {
		PrintWriter writer = null;
		try {
		    debugPrint(fileName, degree, length, writer, cacheSize);
		    System.err.println("A dump file is created where BTree is printed in InOrder Traversal.");
		} catch (IOException e) {
		    System.err.println("File not found to print debug level 1");
		    printUsage();
		    e.printStackTrace();
		} finally {
		    if (writer != null) {
			writer.close();
		    }
		}
	    }
	} catch (NumberFormatException e) {
	    System.err.println("Cache, Degree, Length should be a number");
	    printUsage();
	    System.exit(1);
	}
    }

    /**
     * Prints BTree in a dump file if user selects debug level 1.
     * 
     * @param fileName  is the file name of the data file
     * @param degree    is the degree of the BTree
     * @param length    of the DNA sequence
     * @param writer    is the name of the writer file
     * @param cacheSize is the cache size
     * @throws IOException
     */
    private static void debugPrint(String fileName, int degree, int length, PrintWriter writer, int cacheSize)
	    throws IOException {
	String inputString = fileName + ".btree.data." + length + "." + degree;
	RandomAccessFile randomFile = new RandomAccessFile(inputString, "r");
	BTree bTree = new BTree(randomFile, cacheSize);
	bTree.read_Tree_MetaData();
	TreeNode rootNode = bTree.bTree_Read(bTree.getRootLocation());
	String output = fileName + ".bTree.dump." + length;
	writer = new PrintWriter(new FileWriter(output));
	bTree.writeTreeInOderToFile(rootNode, length, writer);
	writer.close();
    }

    /**
     * Creates a BTree and stores the data in a file, if cache is mentioned a cache
     * is implemented
     * 
     * @param degree    is the degree of the BTree
     * @param fileName  is the fileName where the BTree will be stored
     * @param length    is the length of the DNA sequence
     * @param cacheSize is the size of the cache
     * @throws IOException
     */
    private static void createBTree(int degree, String fileName, int length, int cacheSize) throws IOException {
	FileInputStream inputStream = null;
	Scanner scanFile = null;
	boolean nextPrint = false;
	String file = fileName + ".btree.data." + length + "." + degree;
	String inputString = "";
	BTree bTree = new BTree(degree, file, cacheSize);
	if (degree >= 2) {
	    System.err.println("A BTree of degree " + degree + " is created.");
	} else if (degree == 0) {
	    System.err.println("A BTree of optimal degree is created.");
	}

	if (cacheSize > 0) {
	    System.err.println("A BTree Cache is implemented.");
	}
	try {
	    inputStream = new FileInputStream(fileName);
	    scanFile = new Scanner(inputStream, "UTF-8");
	    String lastString = "";
	    while (scanFile.hasNextLine()) {// line loop
		String line = scanFile.nextLine();
		if (line.contains("//")) {
		    nextPrint = false;
		    lastString = "";
		}
		if (nextPrint) {
		    Scanner scanLine = new Scanner(line);
		    scanLine.useDelimiter("\\s+");
		    while (scanLine.hasNext()) { // each word parsing
			String word = scanLine.next();
			if (!word.matches("[0-9]+")) {
			    inputString = inputString + word;
			}
		    }

		    String newString = lastString + inputString;
		    // print inputString for given length
		    boolean lString = false;
		    for (int strIn = 0; strIn < newString.length(); strIn++) {
			String inputTree = "";
			int token = strIn;
			for (int i = 0; i < length && !lString; i++) {
			    inputTree = inputTree + newString.charAt(token++);
			}
			if (strIn == (newString.length() - length)) {
			    lastString = "";
			    lString = true;
			}
			if (lString && (strIn < newString.length() - 1)) {
			    lastString = lastString + newString.charAt(strIn + 1);
			}
			if (inputTree.length() > 0 && !((inputTree.toUpperCase()).contains("N"))) {
			    long strLong = BinaryFormat.stringBinaryLong(inputTree);
			    TreeObject local = new TreeObject(strLong);

			    bTree.bTree_insert(local); // calling the insert method
			}
		    }
		    inputString = "";
		    scanLine.close();
		}
		if ((line.toUpperCase()).contains("ORIGIN")) {
		    nextPrint = true;
		}
	    }
	    // ** Writing Tree meta-data at last***//
	    bTree.write_Tree_MetaData();
	    System.err.println("BTree insert is finished.");

	    // note that Scanner suppresses exceptions
	    if (scanFile.ioException() != null) {
		scanFile.ioException();
	    }
	} catch (IOException e) {
	    System.err.println("File not Found");
	    printUsage();
	    System.exit(1);
	}
	scanFile.close();
    }

    /**
     * Prints the usage of the GeneBandCreateBTree class.
     */
    private static void printUsage() {
	System.err.println(
		"Usage:\nJava GeneBankCreateBTree <0/1(no/with Cache)> <degree> <gbk file> <sequence length> [<cache size>] [<debug level>]");
	System.err.println(
		"\n\nDebug 0: Any diagnostic messages, help and status messages must be be printed on standard error stream.");
	System.err.println(
		"Debug 1: The program writes a text file named dump, that has the following line format: \n\t\tDNA string: frequency \nThe dump file contains DNA string (corresponding to the key stored) and frequency in an inorder traversal.");
    }
}
