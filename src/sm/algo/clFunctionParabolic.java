package sm.algo;

import sm.base.data.clMatrixData;
import sm.base.data.ifMatrixData;
import sm.base.data.ifVectorData;
import sm.base.md.ifFunction;

/**
 * User: Oleg
 * Date: 19/6/2004
 * Time: 12:13:15
 * Description: this class describes the parabolic view of model
 * and creates corresponding matrix data from a set of vectors X1, X2, ..., XN
 * where Xi - is a vector.
 * e.g. :
 * y1 = a01 + a11x11 + ... + aN1xN1 + ... + aK1xP1xQ1 + ...
 * y2 = a02 + a12x12 + ... + aN2xN2 + ... + aK2xP2xQ2 + ...
 * .   .   .
 * yM = a0M + a1Mx1M + ... + aNMxNM + ... + aK2xP2xQ2 + ...
 */

final class clFunctionParabolic implements ifFunction {
    /**
     * @param vd - input set of vectors. vd[i] = Xi, iRows == M, iCols = N + 1 + (N+1)*N/2.
     * @return - expanded matrix for parabolic model
     */
    public ifMatrixData buildMatrix(final ifVectorData[] vd) {
        final int iArgs = vd.length;
        final int iCols = 1 + iArgs + ((iArgs + 1) * iArgs >> 1);
        final int iRows = vd[0].getArraySize();
        final ifMatrixData md = new clMatrixData(iRows, iCols);
        for (int i = 0; i < iRows; i++) {
            md.setValue(i, 0, 1.0);
            int k = 1;
            for (int j = 0; j < iArgs; j++) {
                md.setValue(i, j + k, vd[j].getValue(i));
            }
            k += iArgs;
            for (int j = 0; j < iArgs; j++) {
                for (int j2 = 0; j2 < iArgs; j2++) {
                    if (j <= j2) {
                        md.setValue(i, k++, vd[j2].getValue(i) * vd[j].getValue(i));
                    }
                }
            }
        }
        return md;
    }

    public String toString() {
        return "Parabolic";
    }
}
