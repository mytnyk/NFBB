package sm.base.md;

import sm.base.data.ifMatrixData;
import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: 19/6/2004
 * Time: 12:59:35
 * Description: The interface handles data rows
 * e.g. in Multirow Polynomial GMDH Algorithm
 */
public interface ifRowHandling extends ifSystem {

    boolean identifyRow() throws clCancelException;

    ifMatrixData getNextInput(int reduceModels) throws clCancelException;

    int getRowNumber();

    ifSystem getLastRow() throws clCancelException;
}
