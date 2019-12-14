import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * GeneBankSearch searches an entire BTree file for query of particular DNA
 * sequence. The data file of the BTree must be compatible with the query DNA
 * sequence.It means the BTree file tree node's DNA sequence length should be
 * same as the query search sequence length.
 * 
 * @author Sajia Zafreen
 */

public class GeneBankSearch {
    /**
     * Driver method of the GeneBankSearch class
     * 
     * @param args
     */
    public static void main(String[] args) {
	if (args.length < 3 || args.length > 5) {
	    printUsage();
	    System.exit(1);
	}
	int cache;
	String bTreeFile;
	String queryFile;
	int debug = 0;
	int cacheSize = 0;
	try {
	    cache = Integer.parseInt(args[0]);
	    bTreeFile = args[1].toString();
	    queryFile = args[2].toString();
	    if (args.length >= 3 && args.length < 5) {
		if (cache == 0 && args.length == 4) {
		    debug = Integer.parseInt(args[3]);
		    if (debug < 0 || debug > 1) {
			System.err.println("Debug level can be only 0 or 1");
			printUsage();
			System.exit(1);
		    }
		} else if (cache == 1 && args.length == 4) {
		    cacheSize = Integer.parseInt(args[3]);
		    if (cacheSize <= 1) {
			System.err.println("If Cache can't be negative , 0 or 1");
			printUsage();
			System.exit(1);
		    }
		} else if (cache == 1 && args.length == 3) {
		    System.err.println("If Cache is 1, then CacheSize must be given.");
		    printUsage();
		    System.exit(1);
		}
	    } else if (args.length == 5) {
		if (cache == 1) {
		    cacheSize = Integer.parseInt(args[3]);
		    debug = Integer.parseInt(args[4]);
		    if (debug < 0 || debug > 1) {
			System.err.println("Debug level can be only 0 or 1");
			printUsage();
			System.exit(1);
		    }
		} else {
		    System.err.println("If Cache is 0 no Cache, if cache is 1 Cache Size must be given.");
		    printUsage();
		    System.exit(1);
		}
	    }
	    try {
		String outputFile = bTreeFile + "_" + queryFile + "_result";
		String[] parseInput = parseSearch(queryFile);
		read_BTreeFile(outputFile, bTreeFile, parseInput, debug, cacheSize);
		if (debug == 1) {
		    System.err.println("A Query Result is written to " + outputFile + " file.");
		}
	    } catch (IOException e) {
		System.err.println("Cannot parse file");
		printUsage();
		System.exit(1);
	    }

	} catch (NumberFormatException e) {
	    System.err.println("Length,Cache,Degree should be a number");
	    printUsage();
	    System.exit(1);
	}
    }

    /**
     * Parse the query file
     * 
     * @param queryFile is the file with queries
     * @return the query as an array of strings.
     * @throws IOException
     */
    private static String[] parseSearch(String queryFile) throws IOException {
	String input = new String(Files.readAllBytes(Paths.get(queryFile)));
	String[] parseInput = input.split("\\r?\\n");
	return parseInput;
    }

    /**
     * Prints the usage of this GeneBankSearch.
     */
    private static void printUsage() {
	System.err.println(
		"java GeneBankSearch <0/1(no/with Cache)> <btree file> <query file> [<cache size>] [0/1<debug level>]");
	System.err.println("Debug 0: The output of the queries should be printed on the standard output stream");
	System.err.println("Debug 1: The output of the queries should be printed on a Query result file");
    }

    /**
     * Reads the BTree file and perform the search of the BTree to look for
     * particular DNA sequence in the query file
     * 
     * @param outputFile is the file where the result of the query sequence is
     *                   written
     * @param bTreeFile  is the BTree file which contains the data
     * @param parseInput is the parsed array of strings of queries
     * @param debug      is the debug level of this search
     * @param cacheSize  is the cache size of this search
     * @throws IOException
     */
    private static void read_BTreeFile(String outputFile, String bTreeFile, String[] parseInput, int debug,
	    int cacheSize) throws IOException {
	System.err.println("A query will start now for " + bTreeFile + " file.");
	if (cacheSize > 0) {
	    System.err.println("A cache of size " + cacheSize + " is implemented.");
	}
	RandomAccessFile file = new RandomAccessFile(bTreeFile, "r");
	BTree readTree = new BTree(file, cacheSize);
	readTree.read_Tree_MetaData();
	PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
	for (int i = 0; i < parseInput.length; i++) {
	    long inputLong = BinaryFormat.stringBinaryLong(parseInput[i]);
	    TreeObject returnObject = readTree.bTree_Search(readTree.getRootNode(), inputLong);
	    if (returnObject != null) {
		if (debug == 0) {
		    System.out.println(parseInput[i].toLowerCase() + ": " + returnObject.getFrequency());
		} else {
		    writer.println(parseInput[i].toLowerCase() + ": " + returnObject.getFrequency());
		}
	    }
	}
	file.close();
	if (writer != null) {
	    writer.close();
	}
    }
}
