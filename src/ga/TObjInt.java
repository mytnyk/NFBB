package ga;

/**
 * User: Oleg
 * Date: Jul 6, 2004
 * Time: 12:07:52 PM
 * Description: simple integer class
 */
public final class TObjInt {
    public TObjInt(final int i) {
        this.i = i;
    }

    public int getValue() {
        return i;
    }

    private final int i;
}