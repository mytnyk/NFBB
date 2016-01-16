package ga;

/**
 * User: Oleg
 * Date: Jul 6, 2004
 * Time: 12:00:31 PM
 * Description: simple float type
 */
final class TObjFloat {
    TObjFloat(final float f) {
        this.f = f;
    }

    public float getValue() {
        return f;
    }

    private final float f;
}