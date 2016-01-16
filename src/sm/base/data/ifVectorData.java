package sm.base.data;

/**
 * User: Oleg
 * Date: Jun 16, 2004
 * Time: 2:20:15 PM
 * Description: this interface is intended to handle all operations with vector data
 */
public interface ifVectorData extends ifDataHandler {
    void setArrayPtr(double[] dData);

    void setValue(int i, double d);

    double[] getArrayPtr();

    double getValue(int i);

    int getArraySize();

}
