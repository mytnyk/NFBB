package ga;

/**
 * User: Oleg
 * Date: Jul 6, 2004
 * Time: 12:00:16 PM
 * Description:  simple double type
 */
final class TObjDouble {
    TObjDouble(final double d) {
        this.d = d;
    }

    public double getValue() {
        return d;
    }

    private final double d;
}
