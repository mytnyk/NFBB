package sm.algo.cast;

import sm.base.util.clTracer;

/**
 * User: Oleg
 * Date: 20/6/2004
 * Time: 18:14:36
 * Description: ...
 */
public final class clUniCasteljau extends clCasteljau {
    private double m_dFirstEndKnot = 0.0;
    private double m_dSecondEndKnot = 0.0;

    public clUniCasteljau(final int iOrder) {
        super(iOrder, iOrder);
    }

    public void setKnotsUniformly(final double[] vBMin, final double[] vBMax) throws clCasteljauException {
        if (vBMin.length != 1 || vBMax.length != 1) {
            throw new clCasteljauException("Invalid vector size!!");
        }

        final int iNumberOfBaseKnots = m_iDim;

        m_pKnotB = new double[iNumberOfBaseKnots][1];

        final double dBMin = vBMin[0];
        final double dBMax = vBMax[0];

        final double dB0w = dBMax - dBMin;
        final double xmin = dBMin - dB0w;
        final double xmax = dBMax + dB0w;

        if (dBMin > dBMax) {
            throw new clCasteljauException("Invalid configuration parameters!!");
        }

        for (int i = 0; i < iNumberOfBaseKnots; i++)//uniformly method
        {
            m_pKnotB[i][0] = xmin + (xmax - xmin) * (double) i / (double) (iNumberOfBaseKnots - 1);
        }
    }

    public void setPredeterminiedKnots(final double[][] vKnots) throws clCasteljauException {
        if (vKnots == null || vKnots.length != m_iDim || vKnots[0].length != 1) {
            throw new clCasteljauException("Invalid vector size!!");
        }
        m_pKnotB = vKnots;
    }

    // straight forward procedure
    protected void buildKnots(final double[] vec, final double[] vX) throws clCasteljauException {
        if (vec.length != 1) {
            throw new clCasteljauException("Invalid vector size!!");
        }

        final double ds = vec[0];

        if (ds < 0 || ds > 1) {
            throw new clCasteljauException("Invalid configuration parameters!!");
        }

        final int m = m_iDim;

        if (m_pKnotB.length != m) {
            throw new clCasteljauException("Invalid vector size!!");
        }

        final double[][] db = new double[m * m][1];

        System.arraycopy(m_pKnotB, 0, db, 0, m);

        for (int nLevel = 0; nLevel < m; nLevel++) {
            String sLog = "Level " + nLevel;
            final int r = nLevel;   //  r = 0..m-1
            for (int j = 0; j < m - r; j++)    //  j = 0..m-r-1
            {
                //  TODO: optimize algorithm
                if (r != 0) {
                    db[j + r * m][0] = (1.0 - ds) * db[j + (r - 1) * m][0] + ds * db[j + 1 + (r - 1) * m][0];
                }
                sLog += " " + db[j + r * m][0];
            }
            if (m_bDebug) {
                clTracer.straceln(sLog);
            }
        }

        m_dFirstEndKnot = db[(m - 2) * m][0];
        m_dSecondEndKnot = db[(m - 2) * m + 1][0];

        if (m_dSecondEndKnot == m_dFirstEndKnot) {
            throw new clCasteljauException("Oops!!!");
        }

        vX[0] = db[(m - 1) * m][0];
        if (m_bDebug) {
            clTracer.straceln("X = " + vX[0]);
        }
        //  this function should return the X-point value by Casteljau algorithm
    }

    // inverse procedure using backpropagation
    public void mapUsingBackProp(final double[] vec, final double[] vs) throws clCasteljauException {
        if (vec.length != 1) {
            throw new clCasteljauException("Invalid vector size!!");
        }

        final double x = vec[0];
        double ds = Math.random(); // exactly from 0 to 1

        if (m_bDebug) {
            clTracer.straceln("random ds = " + ds);
        }
        if (ds < 0 || ds > 1) {
            throw new clCasteljauException("Invalid configuration parameters!!");
        }
        int i = 0; //   iteration number

        final double[] vxest = new double[1];
        while (i++ < m_iMaxIteration) {
            //  knots should be already predetermined!!!
            vs[0] = ds;
            buildKnots(vs, vxest);
            //  backpropagation rule for generating new point:
            final double diff = Math.abs(vxest[0] - x);
            if (diff < m_dEpsilon) {
                break;
            }

            //  TODO: nLearningRate could change its value to more perfect learning
            final double xnext = vxest[0] + m_dAlpha * (x - vxest[0]);

            ds = (xnext - m_dFirstEndKnot) / (m_dSecondEndKnot - m_dFirstEndKnot);

            //assert(0<=ds && ds<=1); // this is possible if xnext step out the range of end knots

            if (ds > 1) {
                ds = 1.0;
            }
            if (ds < 0) {
                ds = 0.0;
            }

            if (m_bDebug) {
                clTracer.straceln("ds = " + ds + " delta = " + diff);
            }
        }
    }

    // inverse procedure using new fast access alg. due to spec. knots!
    public void mapUsingFastAccess(final double[] vec, final double[] vs) throws clCasteljauException {
        if (vec.length != 1) {
            throw new clCasteljauException("Invalid vector size!!");
        }

        final double x = vec[0];

        final int p0 = 0;            //first base knot - _pKnotB[p0]
        final int p1 = m_iDim - 1;   //last base knot - _pKnotB[p1]

        final double ds = (x - m_pKnotB[p0][0]) / (m_pKnotB[p1][0] - m_pKnotB[p0][0]);
        vs[0] = ds;

        final double[] vxest = new double[1];
        buildKnots(vs, vxest);

        final double diff = Math.abs(vxest[0] - x);
        if (m_bDebug) {
            clTracer.straceln("ds = " + ds + " delta = " + diff);
        }
        if (diff > m_dEpsilon) {
            clTracer.straceln("impossible!!!");
        }
    }

    public String toString() {
        return "Univariate Casteljau handling " + getClass().getName();
    }

}
