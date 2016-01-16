package sm.base.data;

/**
 * User: Oleg
 * Date: Jun 16, 2004
 * Time: 12:51:01 PM
 * Description: this basic interface is intended to handle all operations with data
 */
interface ifDataHandler {
    String getDataDescription();

    void setDataDescription(String s);

    void dumpData();

    void normalize(double leftBound, double rightBound);
}
