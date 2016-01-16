package sm.base.data;

/**
 * User: Oleg
 * Date: Jun 16, 2004
 * Time: 4:07:12 PM
 * Description: base class implements basic operations with abstract data
 */
abstract class clData implements ifDataHandler {
    private String m_sDataDescription = "";

    public final String getDataDescription() {
        return m_sDataDescription;
    }

    public final void setDataDescription(final String s) {
        m_sDataDescription = s;
    }

    protected abstract void allocateData();

    public String toString() {
        return "You'll never shoud see this message " + getClass().getName();
    }
}
