package sm.regr;

import sm.base.data.ifMatrixData;
import sm.base.pm.clPlain2DTwinPort;
import sm.base.pm.ifPortHandler;
import sm.base.util.clMathEx;
import sm.base.util.clTracer;

import java.io.IOException;

/**
 * User: Oleg
 * Date: Jun 17, 2004
 * Time: 4:07:59 PM
 * Description: this class implements least square method to build the regression
 */
public class clRegrLSM implements ifRegression {
    static boolean m_bDebug = false;
    ifMatrixTransform m_cXMatrix = null;
    ifMatrixTransform m_cYMatrix = null;
    ifMatrixTransform m_cYEstOut = null;
    ifMatrixTransform m_cXRes = null;
    double m_dVariance = 0.0;

    protected clRegrLSM() {
    }

    public clRegrLSM(final ifMatrixData in, final ifMatrixData out) {
        m_cXMatrix = new clMatrix(in);
        m_cYMatrix = new clMatrix(out);
    }


    public clRegrLSM(final ifMatrixTransform in, final ifMatrixTransform out) {
        m_cXMatrix = in;
        m_cYMatrix = out;
    }

    public final ifMatrixData getRegressors() {
        return m_cXRes;
    }

    public final double getVariance() {
        return m_dVariance;
    }

    public final ifMatrixData getEstimatedOutput() {
        return m_cYEstOut;
    }

    void estimateOutput() {
        m_cYEstOut = m_cXMatrix.product(m_cXRes);
        if (m_bDebug) {
            clTracer.straceln("Estimated output:");
        }
        if (m_bDebug) {
            m_cYEstOut.dumpData();
        }
        final ifMatrixTransform yDiff = m_cYMatrix.subtract(m_cYEstOut);
        m_dVariance = yDiff.getVariance();
        if (m_bDebug) {
            clTracer.straceln("Mean square error: " + m_dVariance);
        }
    }

    public final void buildRegression() {
        if (m_cXMatrix.getRows() < m_cXMatrix.getCols()) {
            clTracer.straceln("Number of regressors is more then data samples!!!");
        }
        final ifMatrixTransform xTr = m_cXMatrix.transpose();
        final ifMatrixTransform xPr = xTr.product(m_cXMatrix); // get inform. matrix
        if (m_bDebug) {
            xPr.dumpData();
        }
        xPr.invert(); // get cov. matrix
        if (m_bDebug) {
            xPr.dumpData();
        } // do not use here getCovariance due to following use of transp. matrice
        if (true/*m_bDebug*/) {
            // show cov. trace and det here just for debugging:
            clTracer.straceln("Covariance trace:" + clMathEx.formatDouble(xPr.trace()));
            clTracer.straceln("Covariance determinant:" + clMathEx.formatDouble(xPr.absdet()));
        }
        final ifMatrixTransform yPr = xTr.product(m_cYMatrix);
        m_cXRes = xPr.product(yPr);
        if (m_bDebug) {
            clTracer.straceln("Coefficients:");
        }
        if (m_bDebug) {
            m_cXRes.dumpData();
        }
        estimateOutput();
    }

    /**
     * just for debugging - reading from files and apply LSM
     */
    public static void main(final String[] args) {
        try {
            /* 1-st test */
            final ifPortHandler p = new clPlain2DTwinPort(args[0]);
            final ifMatrixData xm = p.getData(0);
            final ifMatrixData ym = p.getData(1);
            m_bDebug = true;
            /* 2-st test
            final ifPortHandler p1 = new clPlain2DTwinPort(args[0]);
            final ifPortHandler p2 = new clPlain2DTwinPort(args[1]);
            final ifMatrixData xm = p1.getData(0);
            final ifMatrixData ym = p2.getData(0);  */
            // main workflow:

            final ifRegression ls = new clRegrLSM(xm, ym);
            ls.buildRegression();

        } catch (IOException e) {
            clTracer.straceln("io error: " + e);
        }
    }

    public String toString() {
        return "LSM handling class " + getClass().getName();
    }

}
