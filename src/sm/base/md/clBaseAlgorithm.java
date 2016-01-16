package sm.base.md;

import sm.base.data.ifMatrixData;
import sm.base.data.ifVectorData;
import sm.base.util.clCancelException;
import sm.base.util.ifInfoCallBack;

/**
 * User: Oleg
 * Date: 26/6/2004
 * Time: 19:18:18
 * Description: represent base abstract class for algorithms on system identification
 */
public class clBaseAlgorithm extends clBaseSystem {
    protected final ifFunction m_cFunction;

    /**
     * Creates the base algorithm for system identification
     *
     * @param input  - input vectors
     * @param output - output vector
     * @param func   - descriptor of expansion function for the corresponding model
     * @param cb     - callback provided by user
     */
    protected clBaseAlgorithm(final ifMatrixData input,
                              final ifVectorData output,
                              final ifFunction func,
                              final ifInfoCallBack cb) {
        super(input, output, cb);
        m_cFunction = func;
        // normalization issue:
        m_dInput.normalize(0.0, 1.0);
        m_dOutput.normalize(0.0, 1.0);
    }

    public String toString() {
        return "algorithm";
    }

    /**
     * Function creates appropriate row for data handling.
     * This may be overriden to support another rows besides base row
     *
     * @param input     - row input
     * @param parentRow - descriptor of the parent row
     * @return - newly generated row descriptor
     */
    protected ifRowHandling createRow(final ifMatrixData input, final ifRowHandling parentRow) throws clCancelException {
        return new clBaseRow(input, m_dOutput, m_cFunction, m_cInfoCallBack, parentRow);
    }
}
