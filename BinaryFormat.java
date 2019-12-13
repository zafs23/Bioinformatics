/**
 * BinaryFormat class encodes a (long) object to a binary sequence or decodes a
 * binary sequence to a string.
 * 
 * @author Sajia Zafreen
 */
public class BinaryFormat {

    /**
     * Produces a binary sequence presented as a long value from an object.
     * 
     * @param object is the object to be encoded to a binary sequence
     * @return the binary sequence
     */
    public static long stringBinaryLong(Object object) {
	String subString = object.toString();
	String upperCaseString = subString.toUpperCase();
	String binaryString = "";
	for (int i = 0; i < upperCaseString.length(); i++) {
	    switch (upperCaseString.charAt(i)) {
	    // A and T are pairs
	    case 'A':
		binaryString = binaryString + "00";
		break;
	    case 'T':
		binaryString = binaryString + "11";
		break;
	    case 'C':
		binaryString = binaryString + "01";
		break;
	    case 'G':
		binaryString = binaryString + "10";
		break;
	    }
	}
	long binaryLong = Long.parseLong(binaryString, 2);
	return binaryLong;
    }

    /**
     * Produces a string from a binary sequence presented as a long value
     * 
     * @param value  is the long value of the binary sequence
     * @param length is the length of the DNA sequence.
     * @return the string representation of the binary sequence of the long value
     */
    public static String longBinaryString(long value, int length) {
	String dnaString = "";
	String binaryString = Long.toBinaryString(value);
	if (binaryString.length() < length * 2) {
	    String leadingZero = "";
	    for (int i = 0; i < length * 2 - binaryString.length(); i++) {
		leadingZero = "0" + leadingZero;
	    }
	    binaryString = leadingZero + binaryString;
	}
	String[] arrayString = binaryString.split("(?<=\\G.{2})");
	for (int i = 0; i < arrayString.length; i++) {
	    switch (arrayString[i]) {
	    case "00":
		dnaString = dnaString + "A";
		break;
	    case "11":
		dnaString = dnaString + "T";
		break;
	    case "01":
		dnaString = dnaString + "C";
		break;
	    case "10":
		dnaString = dnaString + "G";
		break;
	    }
	}
	return dnaString.toLowerCase();
    }
}
