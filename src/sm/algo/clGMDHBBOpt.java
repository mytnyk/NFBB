package sm.algo;

import sm.base.data.ifMatrixData;
import sm.base.data.ifVectorData;
import sm.base.md.ifRowHandling;
import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 15, 2004
 * Time: 3:56:27 PM
 * Description: GMDH with Bezier-Bernstein model view for representation of
 * partial descriptions in rows - optimal rows case
 */
public final class clGMDHBBOpt extends clGMDHAlgorithm {

    private final ifFullCallBack m_cFullCallBack;

    /**
     * That is special kind of BB GMDH - with GA optimization
     * on partial description model's fitness.
     *
     * @param input          - input data
     * @param output         - output vector data of the system
     * @param iPolyDegree    - degree of bernstein polynomials used
     * @param bFullExpansion - full or partial expansion of BB function
     * @param cb             - complex callback
     * @param iIndex         - alg. identification
     */
    public clGMDHBBOpt(final ifMatrixData input,
                       final ifVectorData output,
                       final int iPolyDegree,
                       final boolean bFullExpansion,
                       final ifFullCallBack cb,
                       final int iIndex) {
        super(input, output, bFullExpansion ?
                             new clFunctionFullBB(iPolyDegree, false, null, cb, iIndex) :
                             new clFunctionBB(iPolyDegree, false, null, cb, iIndex)
              , cb);
        m_cFullCallBack = cb;
    }

    protected ifRowHandling createRow(ifMatrixData input, ifRowHandling parentRow) throws clCancelException {
        return new clBBOptRow(input, m_dOutput, m_cFunction, m_cFullCallBack, parentRow);
    }

    public String toString() {
        return super.toString() + " - Opt BB";
    }
}
