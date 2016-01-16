package sm.algo;

import sm.base.data.clMatrixData;
import sm.base.data.ifMatrixData;
import sm.base.data.ifVectorData;
import sm.base.md.ifFunction;

/**
 * User: Oleg
 * Date: Jun 18, 2004
 * Time: 1:53:25 PM
 * Description: this class describes the linear view of model
 * and creates corresponding matrix data from a set of vectors X1, X2, ..., XN
 * where Xi - is a vector.
 * e.g. :
 * y1 = a01 + a11x11 + a21x21 + ... + aN1xN1
 * y2 = a02 + a12x12 + a22x22 + ... + aN2xN2
 * .   .   .
 * yM = a0M + a1Mx1M + a2Mx2M + ... + aNMxNM
 */
final class clFunctionLinear implements ifFunction {
    /**
     * @param vd - input set of vectors. vd[i] = Xi, iRows == M, iCols = N + 1.
     * @return - expanded matrix for linear model
     */
    public ifMatrixData buildMatrix(final ifVectorData[] vd) {
        final int iArgs = vd.length;
        final int iCols = iArgs + 1; // plus free term!
        final int iRows = vd[0].getArraySize();
        final ifMatrixData md = new clMatrixData(iRows, iCols);
        for (int i = 0; i < iRows; i++) {
            md.setValue(i, 0, 1.0);
            for (int j = 1; j < iCols; j++) {
                md.setValue(i, j, vd[j - 1].getValue(i));
            }
        }

        return md;
    }

    public String toString() {
        return "Linear";
    }
}
