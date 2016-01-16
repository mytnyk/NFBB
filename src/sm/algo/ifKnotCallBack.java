package sm.algo;

import sm.base.data.ifMatrixData;

/**
 * User: Oleg
 * Date: 11/7/2004
 * Time: 11:17:09
 * Description: knot callback
 */
interface ifKnotCallBack {
    /**
     * Function sets knot's data with specific index for bundle identification.
     */
    void knotCallback(int iIndex, ifMatrixData data);

    /**
     * Function indicates that knot's data just have been changed.
     */
    void refresh();
}
