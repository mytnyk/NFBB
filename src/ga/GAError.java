package ga;

import java.io.PrintStream;

/**
 * User: Oleg
 * Date: Jul 1, 2004
 * Time: 2:42:23 PM
 * Description: This defines the error routines for handling errors
 */

final class GAError implements GAErrorIndex {
    /**
     * This error string contains the text of the most recent error message.  If a
     * GAlib function returns an error code, this string will contain the text of
     * the explanation for the error.
     */
    private static String gaErrMsg = null;

    private static PrintStream ps = System.out;
    private static boolean __gaErrFlag = true;
    private static final String[] __gaErrStr = {
        "error reading from file: ",
        "error writing to file: ",
        "unexpected EOF encountered during read.",
        "bad probability value.  Must be between 0.0 and 1.0, inclusive.",
        "objects are different types.",
        "this method has not been defined.",
        "core deleted with references remaining.",

        "the custom replacement strategy requires a replacement function",
        "unknown replacement strategy",
        "number of children must be greater than 0",
        "replacement percentage must be between 0.0 and 1.0, inclusive",
        "number of indiv for replacement must be less than pop size",
        "index of individual is out-of-bounds",
        "population contains no individuals from which to clone new individuals",
        "there must be at least one individual in each population",
        "no sexual crossover has been defined.  no mating can occur.",
        "no asexual crossover has been defined.  no mating can occur.",

        "children must have same resize behaviour for any given dimension",
        "parents and children must have the same dimensions",
        "parents must be the same length",
        "upper limit must be greater than lower limit.",
        "bad phenotype - ID is out of bounds.",
        "bad phenotype - value is less than min or greater than max.",
        "dimensions of bounds set do not match dimensions of the genome",

        "linear scaling multiplier must be greater than 1.0",
        "sigma truncation multiplier must be greater than 0.0",
        "negative objective function score!\n" +
        "all raw objective scores must be positive for linear scaling.",
        "negative objective function score!\n" +
        "all raw objective scores must be positive for power law scaling.",
        "the cutoff for triangular sharing must be greater than 0.0",

        "cannot index an allele in a bounded, non-discretized set of alleles",
        "length of binary string exceeds maximum for this computer/OS type.",
        "specified value cannot be exactly represented with these bits.",
        "bad 'where' indicator",
        "bogus type, data may be corrupt",
        "bad links in tree.  operation aborted.",
        "cannot swap a node with its ancestor",
        "cannot insert this object into itself",
        "node relative to which insertion is made must be non-NULL.",
        "root node must have no siblings.  insertion aborted.",
        "cannot insert before a root node (only below).",
        "cannot insert after a root node (only below)."
    };

    static void GAErr(final String clss, final String func,
                      final String msg1, final String msg2, final String msg3) {
        gaErrMsg = "\0";
        gaErrMsg += clss;
        gaErrMsg += "::";
        gaErrMsg += func;
        gaErrMsg += ":\n  ";
        gaErrMsg += msg1;
        gaErrMsg += "\n";
        if (msg2 != null) {
            gaErrMsg += "  ";
            gaErrMsg += msg2;
            gaErrMsg += "\n";
        }
        if (msg3 != null) {
            gaErrMsg += "  ";
            gaErrMsg += msg3;
            gaErrMsg += "\n";
        }
        ps.print(gaErrMsg);
    }

    static void GAErr(final String clss, final String func, final int i) {
        GAErr(clss, func, i, null, null);
    }

    private static void GAErr(final String clss, final String func,
                              final int i, final String msg2, final String msg3) {
        gaErrMsg = "\0";
        gaErrMsg += clss;
        gaErrMsg += "::";
        gaErrMsg += func;
        gaErrMsg += ":\n  ";

        gaErrMsg += __gaErrStr[i];
        gaErrMsg += "\n";
        if (msg2 != null) {
            gaErrMsg += "  ";
            gaErrMsg += msg2;
            gaErrMsg += "\n";
        }
        if (msg3 != null) {
            gaErrMsg += "  ";
            gaErrMsg += msg3;
            gaErrMsg += "\n";
        }
        ps.print(gaErrMsg);
    }

    static void GAErr(final String func,
                      final int i, final String msg2, final String msg3) {
        gaErrMsg = "\0";
        gaErrMsg += func;
        gaErrMsg += ":\n  ";
        gaErrMsg += __gaErrStr[i];
        gaErrMsg += "\n";
        if (msg2 != null) {
            gaErrMsg += "  ";
            gaErrMsg += msg2;
            gaErrMsg += "\n";
        }
        if (msg3 != null) {
            gaErrMsg += "  ";
            gaErrMsg += msg3;
            gaErrMsg += "\n";
        }
        ps.print(gaErrMsg);
    }

    /**
     * Use this function to turn on/off the error reporting.  If you turn off the
     * error reporting, the messages will still get stuck into the global error
     * message string, but they will not be sent to the error stream.
     */
    static void GAReportErrors(final boolean flag) {
        __gaErrFlag = flag;
    }

    /**
     * Provide a mechanism for redirecting the error messages.
     */
    static void GASetErrorStream(final PrintStream s) {
        ps = s;
    }
}
