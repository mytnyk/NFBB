package sm.algo.cast;

import sm.base.util.clMathEx;
import sm.base.util.clTracer;

import java.util.Arrays;

/**
 * User: Oleg
 * Date: 20/6/2004
 * Time: 18:53:54
 * Description: ...
 */
public final class clBiiCasteljau extends clCasteljau {
    private double[] m_vFirstEndKnot = null;
    private double[] m_vSecondEndKnot = null;
    private double[] m_vThirdEndKnot = null;

    public clBiiCasteljau(final int iOrder) {
        super(iOrder, (iOrder + 1) * (iOrder + 2) >> 1);
    }

    public void setKnotsUniformly(final double[] vBMin, final double[] vBMax) throws clCasteljauException {
        if (vBMin.length != 2 || vBMax.length != 2) {
            throw new clCasteljauException("Invalid vector size!!");
        }

        final int iNumberOfBaseKnots = m_iDim;

        m_pKnotB = new double[iNumberOfBaseKnots][2];

        final double dB0Min = vBMin[0];
        final double dB0Max = vBMax[0];
        if (dB0Min > dB0Max) {
            throw new clCasteljauException("Invalid configuration parameters!!");
        }

        final double dB1Min = vBMin[1];
        final double dB1Max = vBMax[1];
        if (dB1Min > dB1Max) {
            throw new clCasteljauException("Invalid configuration parameters!!");
        }

        final double c05 = 0.5;
        final double dB0w = dB0Max - dB0Min;
        final double dB1w = dB1Max - dB1Min;
        final double xmin = dB0Min - dB0w;
        final double xmax = dB0Max + dB0w;
        final double ymin = dB1Min - dB1w + c05 * dB1w;
        final double ymax = dB1Max + dB1w + c05 * dB1w;

        //  straight way pre-knots
        final double ystep = (ymax - ymin) / (double) m_iOrder;
        final double xstep = (xmax - xmin) / (double) m_iOrder;
        final double x05step = c05 * xstep;

        int i = 0;
        for (int level = m_iOrder; level >= 0; level--) {
            final double y = ymin + (double) (m_iOrder - level) * ystep;
            for (int item = level; item >= 0; item--) {
                final double x = (double) (m_iOrder - level) * x05step + xmin + (double) item * xstep;
                // addData pair (x,y) to pre-set
                m_pKnotB[i][0] = x;
                m_pKnotB[i][1] = y;
                i += 1;
            }
        }
    }

    public void setPredeterminiedKnots(final double[][] vKnots) throws clCasteljauException {
        if (vKnots == null || vKnots.length != m_iDim || vKnots[0].length != 2) {
            throw new clCasteljauException("Invalid vector size!!");
        }
        m_pKnotB = vKnots;
    }

    // straight forward procedure
    protected void buildKnots(final double[] vec, final double[] vX) throws clCasteljauException {
        if (vec.length != 2) {
            throw new clCasteljauException("Invalid vector size!!");
        }

        final double u = vec[0];
        final double v = vec[1];
        final double w = 1.0 - u - v;

        if (u < 0 || u > 1 || v < 0 || v > 1 || w < 0 || w > 1) {
            throw new clCasteljauException("Invalid configuration parameters!!");
        }

        final int iNumberOfBaseKnots = m_iDim;

        final int m = m_iOrder;
        if (m_pKnotB.length != iNumberOfBaseKnots) {
            throw new clCasteljauException("Invalid configuration parameters!!");
        }

        final double[][] db = new double[m * iNumberOfBaseKnots + 1][2];

        System.arraycopy(m_pKnotB, 0, db, 0, iNumberOfBaseKnots);

        final int p = iNumberOfBaseKnots;

        String[] pIndexStruct = null;

        for (int nLevel = 0; nLevel < m + 1; nLevel++) {
            if (m_bDebug) {
                clTracer.straceln("Level " + nLevel);
            }
            String sLog = "";
            final int r = nLevel;  //  r = 0..m-1

            // TODO: optimize algorithm
            final int iPolyPackNumber = (m - r + 1) * (m - r + 2) >> 1;
            final String[] pIndexStructPrev = pIndexStruct;
            pIndexStruct = new String[iPolyPackNumber];

            int iCounter = 0;

            for (int i = 0; i <= m - r; i++) {
                for (int j = 0; j <= m - r; j++) {
                    for (int k = 0; k <= m - r; k++) {
                        if (i + j + k == m - r) {

                            pIndexStruct[iCounter] = "" + i + j + k;
                            //  then for these i,j,k
                            //  have to do mapping ijk <-> with one number
                            //  we could use String mapping
                            final int bijk = iCounter;
                            if (r != 0) {
                                //1:
                                final int bim1jk = Arrays.binarySearch(pIndexStructPrev, "" + (i + 1) + j + k);
                                //2:
                                final int bijm1k = Arrays.binarySearch(pIndexStructPrev, "" + i + (j + 1) + k);
                                //3:
                                final int bijkm1 = Arrays.binarySearch(pIndexStructPrev, "" + i + j + (k + 1));

                                db[r * p + bijk][0] = u * db[(r - 1) * p + bim1jk][0] +
                                                      v * db[(r - 1) * p + bijm1k][0] +
                                                      w * db[(r - 1) * p + bijkm1][0];
                                db[r * p + bijk][1] = u * db[(r - 1) * p + bim1jk][1] +
                                                      v * db[(r - 1) * p + bijm1k][1] +
                                                      w * db[(r - 1) * p + bijkm1][1];
                            }

                            sLog += " " + db[r * p + bijk][0] + ' ' + db[r * p + bijk][1] + ';';

                            iCounter++;
                        }
                    }
                }
            }
            if (m_bDebug) {
                clTracer.straceln(sLog);
            }
        }

        System.arraycopy(db[p * m], 0, vX, 0, 2);

        m_vFirstEndKnot = db[p * (m - 1)];
        m_vSecondEndKnot = db[p * (m - 1) + 1];
        m_vThirdEndKnot = db[p * (m - 1) + 2];

        if (m_bDebug) {
            clTracer.straceln("X = " + vX[0] + "; " + vX[1]);
        }
        //  this function should return the X-point value by Casteljau algorithm
    }

    // inverse procedure using backpropagation
    public void mapUsingBackProp(final double[] vec, final double[] vuv) throws clCasteljauException {
        if (vec.length != 2) {
            throw new clCasteljauException("Invalid vector size!!");
        }

        final double x1 = vec[0];
        final double x2 = vec[1];
        //initialize the first baryc. values with zeros
        double u = 0.0;
        double v = 0.0;
        final double w = 1.0 - u - v;

        if (m_bDebug) {
            clTracer.straceln("random u = " + u + ", v = " + v);
        }
        if (u < 0 || u > 1 || v < 0 || v > 1 || w < 0 || w > 1) {
            throw new clCasteljauException("Invalid configuration parameters!!");
        }

        int i = 0; //   iteration number
        final double[] vxest = new double[2];

        while (i++ < m_iMaxIteration) {
            //  knots should be already predetermined!!!
            vuv[0] = u;
            vuv[1] = v;
            buildKnots(vuv, vxest);

            //  backpropagation rule for generating new point:
            final double diff = Math.sqrt((vxest[0] - x1) * (vxest[0] - x1) + (vxest[1] - x2) * (vxest[1] - x2));
            if (diff < m_dEpsilon) {
                break;
            }

            //  TODO: nLearningRate could change its value to more perfect learning
            final double[] vxnext = new double[2];
            vxnext[0] = vxest[0] + m_dAlpha * (x1 - vxest[0]);
            vxnext[1] = vxest[1] + m_dAlpha * (x2 - vxest[1]);


            final double det = clMathEx.det(m_vFirstEndKnot[0], m_vSecondEndKnot[0], m_vThirdEndKnot[0],
                                            m_vFirstEndKnot[1], m_vSecondEndKnot[1], m_vThirdEndKnot[1],
                                            1.0, 1.0, 1.0);


            /*
            final double w1 = clMathEx.det( vxnext[0], m_vSecondEndKnot[0], m_vThirdEndKnot[0],
                                            vxnext[1], m_vSecondEndKnot[1], m_vThirdEndKnot[1],
                                            1.0, 1.0, 1.0); */

            final double v1 = clMathEx.det(m_vFirstEndKnot[0], vxnext[0], m_vThirdEndKnot[0],
                                           m_vFirstEndKnot[1], vxnext[1], m_vThirdEndKnot[1],
                                           1.0, 1.0, 1.0);

            final double u1 = clMathEx.det(m_vFirstEndKnot[0], m_vSecondEndKnot[0], vxnext[0],
                                           m_vFirstEndKnot[1], m_vSecondEndKnot[1], vxnext[1],
                                           1.0, 1.0, 1.0);

            u = u1 / det;
            v = v1 / det;
            //w = w1 / det;

            // length violations are possible if x_next step out the range of end knots

            if (u > 1) {
                u = 1.0;
            }
            if (u < 0) {
                u = 0.0;
            }
            if (v > 1) {
                v = 1.0;
            }
            if (v < 0) {
                v = 0.0;
            }
            if (u + v > 1) {
                final double dRed = 0.51 * (u + v - 1);
                u -= dRed; // here for sure will be positive numbers!
                v -= dRed;
            }
            if (m_bDebug) {
                clTracer.straceln("Iteration " + i + ": u = " + u + ", v = " + v + ", delta = " + diff);
            }
        }
    }

    // inverse procedure using new fast access alg. due to spec. knots!
    public void mapUsingFastAccess(final double[] vec, final double[] vuv) throws clCasteljauException {
        if (vec.length != 2) {
            throw new clCasteljauException("Invalid vector size!!");
        }

        final double x1 = vec[0];
        final double x2 = vec[1];

        final int p0 = 0;
        final int p1 = m_iOrder;
        final int p2 = m_iDim - 1;

        final double det = clMathEx.det(m_pKnotB[p0][0], m_pKnotB[p1][0], m_pKnotB[p2][0],
                                        m_pKnotB[p0][1], m_pKnotB[p1][1], m_pKnotB[p2][1],
                                        1.0, 1.0, 1.0);
        /*
        final double w1 = clMathEx.det(x1, m_pKnotB[p1][0], m_pKnotB[p2][0],
                                       x2, m_pKnotB[p1][1], m_pKnotB[p2][1],
                                       1.0, 1.0, 1.0);   */

        final double v1 = clMathEx.det(m_pKnotB[p0][0], x1, m_pKnotB[p2][0],
                                       m_pKnotB[p0][1], x2, m_pKnotB[p2][1],
                                       1.0, 1.0, 1.0);

        final double u1 = clMathEx.det(m_pKnotB[p0][0], m_pKnotB[p1][0], x1,
                                       m_pKnotB[p0][1], m_pKnotB[p1][1], x2,
                                       1.0, 1.0, 1.0);

        final double u = u1 / det;

        final double v = v1 / det;

        //final double w = w1 / det;

        vuv[0] = u;
        vuv[1] = v;

        final double[] vxest = new double[2];
        buildKnots(vuv, vxest);

        final double diff = Math.sqrt((vxest[0] - x1) * (vxest[0] - x1) + (vxest[1] - x2) * (vxest[1] - x2));

        if (m_bDebug) {
            clTracer.straceln("u = " + u + ", v = " + v + ", delta = " + diff);
        }
        if (diff > m_dEpsilon) {
            clTracer.straceln("impossible!!!");
        }
    }

    public String toString() {
        return "Bivariate Casteljau handling " + getClass().getName();
    }
}
