package sm.algo;

import sm.base.data.ifMatrixData;
import sm.base.data.ifVectorData;
import sm.base.md.clBaseRow;
import sm.base.md.ifFunction;
import sm.base.md.ifPartDescriptor;
import sm.base.md.ifRowHandling;
import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: 18/7/2004
 * Time: 15:07:54
 * Description: class represents rows which have optimized PD by GA on knots
 */
final class clBBOptRow extends clBaseRow {

    private final ifFullCallBack m_cFullCallBack;

    clBBOptRow(final ifMatrixData dInput,
               final ifVectorData dOutput,
               final ifFunction func,
               final ifFullCallBack cb,
               final ifRowHandling parentRow) throws clCancelException {
        super(dInput, dOutput, func, cb, parentRow);
        m_cFullCallBack = cb;
    }

    protected ifPartDescriptor createPartDescription(final ifVectorData[] input) throws clCancelException {
        if (m_cPDFunction instanceof ifBBFunction) {
            return new clBBOptPartDesc(input, m_dOutput, (ifBBFunction) m_cPDFunction, m_cFullCallBack);
        } else {
            return super.createPartDescription(input);
        }

    }

    public String toString() {
        return "BB Opt Row " + getClass().getName();
    }
}
