package sm.base.pm;

import sm.base.data.clMatrixData;
import sm.base.data.clVectorData;
import sm.base.data.ifVectorData;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * User: Oleg
 * Date: Jun 17, 2004
 * Time: 11:12:12 AM
 * Description: the class is intended to handle 2D data with ports
 * in the following format:
 * m rows n cols
 * a11 a12 ... a1n
 * ...
 * am1 am2 ... amn
 */
final class clPlain2DReader extends clMatrixData {
    static final class clEndOfDataException extends Exception {
        clEndOfDataException(final String s) {
            super(s);
        }
    }

    clPlain2DReader(final BufferedReader reader) throws IOException, clEndOfDataException {

        String line;
        if ((line = reader.readLine()) == null) {
            throw new clEndOfDataException("No more data found!");
        }

        final ifVectorData vInfo = getVector(line);
        final int iSize = (int) vInfo.getValue(0);
        if (iSize < 0) {
            throw new IOException("Invalid stream size!");
        }

        final ifVectorData[] vData = new clVectorData[iSize];
        int i = 0;
        while ((line = reader.readLine()) != null) {
            vData[i++] = getVector(line);
            if (i >= iSize) {
                break;
            }
        }
        setVArrayPtr(vData);
    }

    private static ifVectorData getVector(final String line) {
        final StringTokenizer tokenizer = new StringTokenizer(line, " ,;:\t", false);
        final int n = tokenizer.countTokens();
        final ifVectorData v = new clVectorData(n);
        for (int i = 0; i < n; i++) {
            v.setValue(i, Double.parseDouble(tokenizer.nextToken()));
        } // for
        return v;
    } // getVector


    public String toString() {
        return "Plain reader handling class " + getClass().getName();
    }

}
