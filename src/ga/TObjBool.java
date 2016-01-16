package ga;

/**
 * User: Oleg
 * Date: Jul 6, 2004
 * Time: 11:59:27 AM
 * Description: simple boolean type
 */
final class TObjBool {
    TObjBool(final boolean b) {
        this.b = b;
    }

    public boolean getValue() {
        return b;
    }

    private final boolean b;
}