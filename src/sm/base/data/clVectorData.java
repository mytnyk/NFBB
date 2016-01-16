package sm.base.data;

import sm.base.util.clMathEx;
import sm.base.util.clTracer;

/**
 * User: Oleg
 * Date: Jun 17, 2004
 * Time: 11:58:00 AM
 * Description: this class implements basic operations with 1D data
 */
public final class clVectorData extends clData implements ifVectorData {
    private double[] m_dData = null;
    private int m_iDim = 0;

    public clVectorData() {
    }

    public clVectorData(final int iDim) {
        m_iDim = iDim;
        allocateData();
    }

    public void setArrayPtr(final double[] dData) {
        m_iDim = dData.length;
        m_dData = dData;
    }

    public void setValue(final int i, final double d) {
        m_dData[i] = d;
    }

    public double[] getArrayPtr() {
        return m_dData;
    }

    public double getValue(final int i) {
        return m_dData[i];
    }

    public int getArraySize() {
        return m_iDim;
    }

    public void normalize(final double leftBound, final double rightBound) {

        if (leftBound >= rightBound) {
            return;
        }
        // first determine the min and max for each column
        double dMin = m_dData[0];
        double dMax = m_dData[0];
        for (int i = 1; i < m_iDim; i++) {
            if (dMin > m_dData[i]) {
                dMin = m_dData[i];
            }
            if (dMax < m_dData[i]) {
                dMax = m_dData[i];
            }
        }
        if (dMax <= dMin) {
            return;
        }
        // normalize:
        for (int i = 0; i < m_iDim; i++) {
            // reduce to range [0, 1]
            m_dData[i] = (m_dData[i] - dMin) / (dMax - dMin);
            // reduce to required range [leftBound, rightBound]
            m_dData[i] = (m_dData[i] + leftBound) * (rightBound - leftBound);
        }

    }

    protected void allocateData() {
        m_dData = new double[m_iDim];
    }

    public void dumpData() {
        for (int i = 0; i < m_iDim; i++) {
            final String s = clMathEx.formatDouble(m_dData[i], 4);
            clTracer.straceln(s);
        }
    }

    public String toString() {
        return "Vector data handling class " + getClass().getName();
    }

}
