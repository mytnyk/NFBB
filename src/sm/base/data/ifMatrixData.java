package sm.base.data;

/**
 * User: Oleg
 * Date: Jun 16, 2004
 * Time: 2:42:04 PM
 * Description: this interface is intended to handle all operations with 2D matrix data
 */
public interface ifMatrixData extends ifDataHandler {
    void setValue(int i, int j, double d);

    ifVectorData[] getVArrayPtr();

    double[][] get2DArrayPtr();

    double getValue(int i, int j);

    int getRows();

    int getCols();

    ifVectorData getVectorPtr(int i);
}
