package sm.base.pm;

import sm.base.util.clTracer;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * User: Oleg
 * Date: Jun 17, 2004
 * Time: 1:00:04 PM
 * Description: the class is intended to handle twin 2D data with ports
 * in the following format:
 * m rows n cols
 * a11 a12 ... a1n
 * ...
 * am1 am2 ... amn
 * p rows q cols
 * a11 a12 ... a1q
 * ...
 * ap1 ap2 ... apq
 */
public final class clPlain2DTwinPort extends clPort {
    /**
     * Constructs correct filename
     */
    public final static String CorrectFileName(final String fileName)
    {
        if (fileName.endsWith(".iom"))
            return fileName;
        return fileName + ".iom";
    }
    /**
     * Constructs a clPlain2DTwinPort from the contents of the named file.
     */
    public clPlain2DTwinPort(final String fileName) throws IOException {
        this(new File(fileName));
    } // constructor

    /**
     * Constructs a clPlain2DTwinPort from the contents of the file represented by
     * the File object.
     */
    private clPlain2DTwinPort(final File file) throws IOException {
        this(new FileInputStream(file));
    } // constructor

    /**
     * Constructs a clPlain2DTwinPort from the contents located at the URL.
     */
    public clPlain2DTwinPort(final URL url) throws IOException {
        this(url.openStream());
    } // constructor

    /**
     * Constructs a clPlain2DTwinPort from the contents read through the InputStream.
     */
    private clPlain2DTwinPort(final InputStream is) throws IOException {
        setPortDescription("Plain 2D data twinned port");

        final InputStreamReader isr = new InputStreamReader(is);
        final BufferedReader reader = new BufferedReader(isr);
        int i = 2;
        m_listData = new ArrayList(i);

        try {
            while (i-- != 0) {
                m_listData.add(new clPlain2DReader(reader));
            }
            m_listDataDesc = (new clDescriptionReader(reader)).getDescriptions();
        } catch (clPlain2DReader.clEndOfDataException e) {
            // so do nothing
            clTracer.straceln("" + e);
        }

    } // constructor

    public String toString() {
        return "Plain 2D twin port";
    }

    public static void main(final String[] args) {
        try {
            new clPlain2DTwinPort(args[0]);
        } catch (IOException e) {
            clTracer.straceln("io error: " + e);
        }
    }

}
