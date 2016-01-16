/**
 * User: Oleg
 * Date: Jun 25, 2004
 * Time: 11:48:20 AM
 * Description: Class provides the enumeration of the available methods.
 */
package sm.appl;

final class clMethodKind {
    static final clMethodKind enGMDHLinear = new clMethodKind("GMDH Linear");
    static final clMethodKind enGMDHParabolic = new clMethodKind("GMDH Parabolic");
    static final clMethodKind enGMDHBB = new clMethodKind("GMDH BB");
    static final clMethodKind enHarrisBB = new clMethodKind("Bezier Bern.");
    static final clMethodKind enHarrisOptimal = new clMethodKind("BB Optimal");
    static final clMethodKind enGMDHOptimal = new clMethodKind("GMDH Optimal");

    private final String m_sName;

    String getName() {
        return m_sName;
    }

    private clMethodKind(final String name) {
        m_sName = name;
    }

    public String toString() {
        return m_sName;
    }
}
