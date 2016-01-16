package sm.algo;

import sm.base.data.clMatrixData;
import sm.base.data.ifMatrixData;
import sm.base.data.ifVectorData;
import sm.base.util.clCancelException;
import sm.base.util.ifProgressCallBack;


/**
 * User: Oleg
 * Date: Jun 22, 2004
 * Time: 1:36:58 PM
 * Description: this class describes the bezier bernstein view of model
 * and creates corresponding matrix data from a set of vectors X1, X2, ..., XN
 * where Xi - is a vector.
 * for bivariates and for univariates:
 * y1 = f(x11) + f(x21) + f(x11, x21)
 * y2 = f(x12) + f(x22) + f(x12, x22)
 * .   .   .
 * yM = f(x1M) + f(x2M) + f(x1M, x2M)
 * where f - (uni)bi-variate bezier-bernstein polynomial functions
 */
final class clFunctionFullBB extends clFunctionBB {
    clFunctionFullBB(final int iPolyOrder,
                     final boolean bFastMethod,
                     final ifProgressCallBack pcb,
                     final ifKnotCallBack kcb,
                     final int iIndex) {
        super(iPolyOrder, bFastMethod, pcb, kcb, iIndex);
    }

    public ifMatrixData buildMatrix(final ifVectorData[] vd) throws clCancelException {
        final int iArgs = vd.length;
        if (iArgs == 1) {
            return super.buildMatrix(vd);
        }
        if (iArgs != 2) {
            throw new clCancelException("Args=" + iArgs + " not supported by function!");
        }

        final ifMatrixData com = super.buildMatrix(vd);
        final ifVectorData[] fiv = {vd[0]};
        final ifVectorData[] sev = {vd[1]};
        final ifMatrixData fim = super.buildMatrix(fiv);
        final ifMatrixData sem = super.buildMatrix(sev);
        final int iCols = com.getCols() + fim.getCols() + sem.getCols();
        final int iRows = vd[0].getArraySize();
        final ifMatrixData md = new clMatrixData(iRows, iCols);
        for (int i = 0; i < iRows; i++) {
            int k = 0;
            for (int j = 0; j < com.getCols(); j++) {
                md.setValue(i, k++, com.getValue(i, j));
            }
            for (int j = 0; j < fim.getCols(); j++) {
                md.setValue(i, k++, fim.getValue(i, j));
            }
            for (int j = 0; j < sem.getCols(); j++) {
                md.setValue(i, k++, sem.getValue(i, j));
            }
        }
        //md.dumpData();
        return md;
    }

    public String toString() {
        return "B-B full";
    }
}
