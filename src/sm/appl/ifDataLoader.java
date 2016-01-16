package sm.appl;

import sm.base.data.ifMatrixData;
import sm.base.data.ifVectorData;
import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jun 25, 2004
 * Time: 4:10:36 PM
 * Description: Data loading interface.
 */
interface ifDataLoader extends Runnable {
    int getNubmerOfInputs();

    boolean isDataLoaded();

    ifMatrixData getInput();

    ifVectorData getInput(int i);

    ifVectorData getOutput();

    void start();

    clCancelException getCancelled();
}
