package sm.base.data;

import sm.base.util.clMathEx;
import sm.base.util.clTracer;

/**
 * User: Oleg
 * Date: Jun 16, 2004
 * Time: 3:35:39 PM
 * Description: this class implements basic operations with 2D data
 */
public class clMatrixData extends clData implements ifMatrixData {
    private double[][] m_dData = null;
    private int m_iRows = 0;
    private int m_iCols = 0;

    /**
     * That is a simple copy ctor.
     *
     * @param md - the matrix to copy
     */
    protected clMatrixData(final ifMatrixData md) {
        m_iRows = md.getRows();
        m_iCols = md.getCols();
        m_dData = md.get2DArrayPtr();
    }

    /**
     * Create matrix from vector
     *
     * @param vd - vector to wrap
     */
    public clMatrixData(final ifVectorData vd) {
        m_iRows = vd.getArraySize();
        m_iCols = 1;
        m_dData = new double[m_iRows][m_iCols];
        for (int i = 0; i < m_iRows; i++) {
            m_dData[i][0] = vd.getValue(i);
        }
    }


    /**
     * Create matrix from 2d array
     *
     * @param dData - data to wrap
     */
    public clMatrixData(final double[][] dData) {
        m_iRows = dData.length;
        m_iCols = dData[0].length;
        m_dData = dData;
    }

    protected clMatrixData() {
    }

    public clMatrixData(final int iRows, final int iCols) {
        m_iCols = iCols;
        m_iRows = iRows;
        allocateData();
    }

    protected final void setVArrayPtr(final ifVectorData[] dData) {
        m_iRows = dData.length;
        m_iCols = dData[0].getArraySize();
        m_dData = new double[m_iRows][];

        for (int i = 0; i < m_iRows; i++) {
            m_dData[i] = dData[i].getArrayPtr();
        }
    }
    /*
    private final void set2DArrayPtr( final double[][] dData ) {
        m_iRows = dData.length;
        m_iCols = dData[0].length;
        m_dData = dData;
    }*/

    public final void setValue(final int i, final int j, final double d) {
        m_dData[i][j] = d;
    }

    public final ifVectorData[] getVArrayPtr() {
        final ifVectorData[] vd = new clVectorData[m_iRows];
        for (int i = 0; i < m_iRows; i++) {
            vd[i] = getVectorPtr(i);
        }
        return vd;
    }

    public final ifVectorData getVectorPtr(final int i) {
        //if (i >= m_iRows) return null; // better to generate some exception
        final ifVectorData vd = new clVectorData();
        vd.setArrayPtr(m_dData[i]);
        return vd;
    }

    public final double[][] get2DArrayPtr() {
        return m_dData;
    }

    public final double getValue(final int i, final int j) {
        return m_dData[i][j];
    }

    public final int getRows() {
        return m_iRows;
    }

    public final int getCols() {
        return m_iCols;
    }

    public final void normalize(final double leftBound, final double rightBound) {
        // this one normalizes vectors in each row
        for (int i = 0; i < m_iRows; i++) {
            getVectorPtr(i).normalize(leftBound, rightBound);
        }
    }

    protected final void allocateData() {
        m_dData = new double[m_iRows][m_iCols];
    }

    public final void dumpData() {
        for (int i = 0; i < m_iRows; i++) {
            String s = "";
            for (int j = 0; j < m_iCols; j++) {
                s = s + '\t' + clMathEx.formatDouble(m_dData[i][j], 4);
            }
            clTracer.straceln(s);
        }
    }

    public String toString() {
        return "Matrix data handling class " + getClass().getName();
    }

}
