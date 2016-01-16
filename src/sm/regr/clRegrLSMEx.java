package sm.regr;

import sm.base.data.clMatrixData;
import sm.base.data.ifMatrixData;
import sm.base.util.clTracer;

/**
 * User: Oleg
 * Date: Jun 18, 2004
 * Time: 4:28:56 PM
 * Description: this class implements least square method to build the regression
 * - does coef. estimations on learning samples
 * - does model estimations on test samples
 */
public final class clRegrLSMEx extends clRegrLSM {
    private final ifMatrixData m_cXMatrixFull;
    private ifMatrixData m_cYMatrixFull = null;

    private final ifMatrixData m_cXMatrixLearn;
    private final ifMatrixData m_cXMatrixTest;
    private final ifMatrixData m_cYMatrixLearn;
    private final ifMatrixData m_cYMatrixTest;
    private final int m_iNumOfTestSamples;
    private final int m_iNumOfLearnSamples;
    private final int m_iTotalSamples;

    public clRegrLSMEx(final ifMatrixData in, final ifMatrixData out, final double dPercOfTestSamples) {
        m_cXMatrixFull = in;
        m_cYMatrixFull = out;
        m_iTotalSamples = in.getRows();
        m_iNumOfTestSamples = (int) ((double) m_iTotalSamples * dPercOfTestSamples);
        m_iNumOfLearnSamples = m_iTotalSamples - m_iNumOfTestSamples;
        // Should think about more wise splitting!!!
        m_cXMatrixLearn = new clMatrixData(m_iNumOfLearnSamples, in.getCols());
        m_cXMatrixTest = new clMatrixData(m_iNumOfTestSamples, in.getCols());
        m_cYMatrixLearn = new clMatrixData(m_iNumOfLearnSamples, out.getCols());
        m_cYMatrixTest = new clMatrixData(m_iNumOfTestSamples, out.getCols());
        for (int i = 0; i < m_iNumOfLearnSamples; i++) {
            for (int j = 0; j < in.getCols(); j++) {
                m_cXMatrixLearn.setValue(i, j, in.getValue(i, j));
            }
            for (int j = 0; j < out.getCols(); j++) {
                m_cYMatrixLearn.setValue(i, j, out.getValue(i, j));
            }

        }
        for (int i = 0; i < m_iNumOfTestSamples; i++) {
            for (int j = 0; j < in.getCols(); j++) {
                m_cXMatrixTest.setValue(i, j, in.getValue(i + m_iNumOfLearnSamples, j));
            }
            for (int j = 0; j < out.getCols(); j++) {
                m_cYMatrixTest.setValue(i, j, out.getValue(i + m_iNumOfLearnSamples, j));
            }
        }
        m_cXMatrix = new clMatrix(m_cXMatrixLearn);
        m_cYMatrix = new clMatrix(m_cYMatrixLearn);

    }

    protected void estimateOutput() {
        // m_cYEstOut should contain full est.
        m_cYEstOut = (new clMatrix(m_cXMatrixFull)).product(m_cXRes);
        if (m_bDebug) {
            clTracer.straceln("Estimated output:");
        }
        if (m_bDebug) {
            m_cYEstOut.dumpData();
        }
        // Variance on test(validation) data only
        //final ifMatrixTransform cYEstOutPart = (new clMatrix(m_cXMatrixTest)).product(m_cXRes);
        //final ifMatrixTransform yDiff = (new clMatrix(m_cYMatrixTest)).subtract(cYEstOutPart);
        //m_dVariance = yDiff.getVariance();

        // Variance on full data set
        final ifMatrixTransform yDiffFull = (new clMatrix(m_cYMatrixFull)).subtract(m_cYEstOut);
        m_dVariance = yDiffFull.getVariance();
        if (m_bDebug) {
            clTracer.straceln("Mean square error: " + m_dVariance);
        }
    }

    public String toString() {
        return "Advanced LSM handling class " + getClass().getName();
    }
}
