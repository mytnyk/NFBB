package ga;

/**
 * User: Oleg
 * Date: Jul 8, 2004
 * Time: 10:48:18 AM
 * Description: ...
 * <p/>
 * This class defines the interface for the 2D binary string genome, including
 * crossover objects and all the default and built-in operators.
 * ----------------------------------------------------------------------------
 */
public final class GA2DBinaryStringGenome extends GAGenome {
    private int nx;
    private int ny;
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;
    private final GABinaryString bs;

    public String className() {
        return "GA2DBinaryStringGenome";
    }

    public int classID() {
        return BinaryStringGenome2D;
    }

    private final OnePointCrossover _OnePointCrossover = new OnePointCrossover();
    private final UniformInitializer _UniformInitializer = new UniformInitializer();
    private final FlipMutator _FlipMutator = new FlipMutator();
    private final BitComparator _BitComparator = new BitComparator();

    static final class OnePointCrossover implements SexualCrossover {
        /**
         * When we do single point crossover on resizable 2D genomes we can either
         * clip or pad to make the mismatching geometries work out.  Either way, both
         * children end up with the same dimensions (the children have the same
         * dimensions as each other, not the same as if they were clipped/padded).
         * When we pad, the extra space is filled with random bits.  This
         * implementation does only clipping, no padding!
         */
        public int sexualCrossover(final GAGenome p1, final GAGenome p2, final GAGenome c1, final GAGenome c2) {
            final GA2DBinaryStringGenome mom = (GA2DBinaryStringGenome) p1;
            final GA2DBinaryStringGenome dad = (GA2DBinaryStringGenome) p2;

            int nc = 0;
            final int momsitex;
            final int momlenx;
            final int momsitey;
            final int momleny;
            final int dadsitex;
            final int dadlenx;
            final int dadsitey;
            final int dadleny;
            final int sitex;
            final int lenx;
            final int sitey;
            final int leny;

            if (c1 != null && c2 != null) {
                final GA2DBinaryStringGenome sis = (GA2DBinaryStringGenome) c1;
                final GA2DBinaryStringGenome bro = (GA2DBinaryStringGenome) c2;

                if (sis.resizeBehaviour(Dimension.WIDTH) == Size.FIXED_SIZE &&
                    bro.resizeBehaviour(Dimension.WIDTH) == Size.FIXED_SIZE) {
                    if (mom.width() != dad.width() ||
                        sis.width() != bro.width() ||
                        sis.width() != mom.width()) {
                        GAError.GAErr(mom.className(), "one-point cross", GAError.gaErrSameLengthReqd);
                        return nc;
                    }
                    sitex = momsitex = dadsitex = GARandom.GARandomInt(0, mom.width());
                    lenx = momlenx = dadlenx = mom.width() - momsitex;
                } else if (sis.resizeBehaviour(Dimension.WIDTH) == Size.FIXED_SIZE ||
                           bro.resizeBehaviour(Dimension.WIDTH) == Size.FIXED_SIZE) {
                    GAError.GAErr(mom.className(), "one-point cross", GAError.gaErrSameBehavReqd);
                    return nc;
                } else {
                    momsitex = GARandom.GARandomInt(0, mom.width());
                    dadsitex = GARandom.GARandomInt(0, dad.width());
                    momlenx = mom.width() - momsitex;
                    dadlenx = dad.width() - dadsitex;
                    sitex = (int) GAUtils.GAMin(momsitex, dadsitex);
                    lenx = (int) GAUtils.GAMin(momlenx, dadlenx);
                }

                if (sis.resizeBehaviour(Dimension.HEIGHT) == Size.FIXED_SIZE &&
                    bro.resizeBehaviour(Dimension.HEIGHT) == Size.FIXED_SIZE) {
                    if (mom.height() != dad.height() ||
                        sis.height() != bro.height() ||
                        sis.height() != mom.height()) {
                        GAError.GAErr(mom.className(), "one-point cross", GAError.gaErrSameLengthReqd);
                        return nc;
                    }
                    sitey = momsitey = dadsitey = GARandom.GARandomInt(0, mom.height());
                    leny = momleny = dadleny = mom.height() - momsitey;
                } else if (sis.resizeBehaviour(Dimension.HEIGHT) == Size.FIXED_SIZE ||
                           bro.resizeBehaviour(Dimension.HEIGHT) == Size.FIXED_SIZE) {
                    GAError.GAErr(mom.className(), "one-point cross", GAError.gaErrSameBehavReqd);
                    return nc;
                } else {
                    momsitey = GARandom.GARandomInt(0, mom.height());
                    dadsitey = GARandom.GARandomInt(0, dad.height());
                    momleny = mom.height() - momsitey;
                    dadleny = dad.height() - dadsitey;
                    sitey = (int) GAUtils.GAMin(momsitey, dadsitey);
                    leny = (int) GAUtils.GAMin(momleny, dadleny);
                }

                sis.resize(sitex + lenx, sitey + leny);
                bro.resize(sitex + lenx, sitey + leny);

                sis.copy(mom, 0, 0, momsitex - sitex, momsitey - sitey, sitex, sitey);
                sis.copy(dad, sitex, 0, dadsitex, dadsitey - sitey, lenx, sitey);
                sis.copy(dad, 0, sitey, dadsitex - sitex, dadsitey, sitex, leny);
                sis.copy(mom, sitex, sitey, momsitex, momsitey, lenx, leny);

                bro.copy(dad, 0, 0, dadsitex - sitex, dadsitey - sitey, sitex, sitey);
                bro.copy(mom, sitex, 0, momsitex, momsitey - sitey, lenx, sitey);
                bro.copy(mom, 0, sitey, momsitex - sitex, momsitey, sitex, leny);
                bro.copy(dad, sitex, sitey, dadsitex, dadsitey, lenx, leny);

                nc = 2;
            } else if (c1 != null || c2 != null) {
                final GA2DBinaryStringGenome sis = c1 != null ?
                                                   (GA2DBinaryStringGenome) c1 : (GA2DBinaryStringGenome) c2;

                if (sis.resizeBehaviour(Dimension.WIDTH) == Size.FIXED_SIZE) {
                    if (mom.width() != dad.width() || sis.width() != mom.width()) {
                        GAError.GAErr(mom.className(), "one-point cross", GAError.gaErrSameLengthReqd);
                        return nc;
                    }
                    sitex = momsitex = dadsitex = GARandom.GARandomInt(0, mom.width());
                    lenx = momlenx = dadlenx = mom.width() - momsitex;
                } else {
                    momsitex = GARandom.GARandomInt(0, mom.width());
                    dadsitex = GARandom.GARandomInt(0, dad.width());
                    momlenx = mom.width() - momsitex;
                    dadlenx = dad.width() - dadsitex;
                    sitex = (int) GAUtils.GAMin(momsitex, dadsitex);
                    lenx = (int) GAUtils.GAMin(momlenx, dadlenx);
                }

                if (sis.resizeBehaviour(Dimension.HEIGHT) == Size.FIXED_SIZE) {
                    if (mom.height() != dad.height() || sis.height() != mom.height()) {
                        GAError.GAErr(mom.className(), "one-point cross", GAError.gaErrSameLengthReqd);
                        return nc;
                    }
                    sitey = momsitey = dadsitey = GARandom.GARandomInt(0, mom.height());
                    leny = momleny = dadleny = mom.height() - momsitey;
                } else {
                    momsitey = GARandom.GARandomInt(0, mom.height());
                    dadsitey = GARandom.GARandomInt(0, dad.height());
                    momleny = mom.height() - momsitey;
                    dadleny = dad.height() - dadsitey;
                    sitey = (int) GAUtils.GAMin(momsitey, dadsitey);
                    leny = (int) GAUtils.GAMin(momleny, dadleny);
                }

                sis.resize(sitex + lenx, sitey + leny);

                if (GARandom.GARandomBit() != 0) {
                    sis.copy(mom, 0, 0, momsitex - sitex, momsitey - sitey, sitex, sitey);
                    sis.copy(dad, sitex, 0, dadsitex, dadsitey - sitey, lenx, sitey);
                    sis.copy(dad, 0, sitey, dadsitex - sitex, dadsitey, sitex, leny);
                    sis.copy(mom, sitex, sitey, momsitex, momsitey, lenx, leny);
                } else {
                    sis.copy(dad, 0, 0, dadsitex - sitex, dadsitey - sitey, sitex, sitey);
                    sis.copy(mom, sitex, 0, momsitex, momsitey - sitey, lenx, sitey);
                    sis.copy(mom, 0, sitey, momsitex - sitex, momsitey, sitex, leny);
                    sis.copy(dad, sitex, sitey, dadsitex, dadsitey, lenx, leny);
                }

                nc = 1;
            }

            return nc;

        }
    }

    static final class UniformInitializer implements Initializer {
        /**
         * ----------------------------------------------------------------------------
         * 2D Binary String Genome
         * The order for looping through indices is height-then-width (ie height loops
         * before a single width increment)
         * ----------------------------------------------------------------------------
         */
        public void initializer(final GAGenome c) {
            final GA2DBinaryStringGenome child = (GA2DBinaryStringGenome) c;
            child.resize(Size.ANY_SIZE, Size.ANY_SIZE);
            for (int i = child.width() - 1; i >= 0; i--) {
                for (int j = child.height() - 1; j >= 0; j--) {
                    child.gene(i, j, (short) GARandom.GARandomBit());
                }
            }
        }
    }

    static final class FlipMutator implements Mutator {
        public int mutator(final GAGenome c, final float pmut) {
            final GA2DBinaryStringGenome child = (GA2DBinaryStringGenome) c;
            int n, m, i, j;
            if (pmut <= 0.0) {
                return 0;
            }

            float nMut = pmut * (float) child.size();
            if (nMut < 1.0) { // we have to do a flip test on each bit
                nMut = 0;
                for (i = child.width() - 1; i >= 0; i--) {
                    for (j = child.height() - 1; j >= 0; j--) {
                        if (GARandom.GAFlipCoin(pmut)) {
                            child.gene(i, j, (short) (child.gene(i, j) == 0 ? 1 : 0));
                            nMut++;
                        }
                    }
                }
            } else {       // only flip the number of bits we need to flip
                for (n = 0; n < nMut; n++) {
                    m = GARandom.GARandomInt(0, child.size() - 1);
                    i = m % child.width();
                    j = m / child.width();
                    child.gene(i, j, (short) (child.gene(i, j) == 0 ? 1 : 0));
                }
            }
            return (int) nMut;
        }
    }

    static final class BitComparator implements Comparator {
        public float comparator(final GAGenome a, final GAGenome b) {
            final GA2DBinaryStringGenome sis = (GA2DBinaryStringGenome) a;
            final GA2DBinaryStringGenome bro = (GA2DBinaryStringGenome) b;
            if (sis.size() != bro.size()) {
                return -1;
            }
            if (sis.size() == 0) {
                return 0;
            }
            float count = (float) 0.0;
            for (int i = sis.width() - 1; i >= 0; i--) {
                for (int j = sis.height() - 1; j >= 0; j--) {
                    count += sis.gene(i, j) == bro.gene(i, j) ? 0 : 1;
                }
            }
            return count / sis.size();
        }
    }

    private GA2DBinaryStringGenome(final int x, final int y) {
        this(x, y, null);
    }

    public GA2DBinaryStringGenome(final int x, final int y, final Evaluator f) {
        this(x, y, f, null);
    }

    private GA2DBinaryStringGenome(final int width, final int height, final Evaluator f, final Object u) {
        super(null, null, null);
        bs = new GABinaryString(width * height);
        initializer(_UniformInitializer);
        mutator(_FlipMutator);
        comparator(_BitComparator);
        evaluator(f);
        userData(u);
        crossover(_OnePointCrossover);
        nx = minX = maxX = 0;
        ny = minY = maxY = 0;
        resize(width, height);
    }

    GA2DBinaryStringGenome(final GA2DBinaryStringGenome orig) {
        super(null, null, null);
        bs = new GABinaryString(orig.bs.size());
        nx = minX = maxX = 0;
        ny = minY = maxY = 0;
        copy(orig);
    }

    GA2DBinaryStringGenome assign(final GAGenome arg) {
        copy(arg);
        return this;
    }

    GA2DBinaryStringGenome assign(final short[][] array) {
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                gene(i, j, array[i][j]);
            }
        }
        return this;
    }

    GA2DBinaryStringGenome assign(final int[][] array) {
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                gene(i, j, (short) array[i][j]);
            }
        }
        return this;
    }

    void delete() {

    }

    public Object clone() {
        return clone(CloneMethod.CONTENTS);
    }

    Object clone(final int flag) {
        final GA2DBinaryStringGenome cpy = new GA2DBinaryStringGenome(nx, ny);
        if (flag == CloneMethod.CONTENTS) {
            cpy.copy(this);
        } else {
            ((GAGenome) cpy).copy(this);
            cpy.minX = minX;
            cpy.minY = minY;
            cpy.maxX = maxX;
            cpy.maxY = maxY;
        }
        return cpy;
    }

    void copy(final GAGenome orig) {
        if (orig == this) {
            return;
        }
        final GA2DBinaryStringGenome c = (GA2DBinaryStringGenome) orig;
        if (c != null) {
            super.copy(c);
            bs.copy(c.bs);
            nx = c.nx;
            ny = c.ny;
            minX = c.minX;
            minY = c.minY;
            maxX = c.maxX;
            maxY = c.maxY;
        }
    }

    boolean equal(final GAGenome c) {
        if (this == c) {
            return true;
        }
        final GA2DBinaryStringGenome b = (GA2DBinaryStringGenome) c;
        if (nx != b.nx || ny != b.ny) {
            return false;
        }
        boolean val = false;
        for (int j = 0; j < ny && !val; j++) {
            val = bs.equal(b.bs, j * nx, j * nx, nx) ? false : true;
        }
        return val ? false : true;
    }

// specific to this class
    public short gene(final int x, final int y) {
        return bs.bit(x + nx * y);
    }

    private short gene(final int x, final int y, final short value) {
        _evaluated = false;
        return bs.bit(x + nx * y) == value ? value : bs.bit(x + nx * y, value);
    }

    public int width() {
        return nx;
    }

    int width(final int w) {
        resize(w, ny);
        return nx;
    }

    public int height() {
        return ny;
    }

    int height(final int h) {
        resize(nx, h);
        return ny;
    }

    private int resize(int w, int h) {
        if ((int) w == nx && (int) h == ny) {
            return bs.sz;
        }

        if (w == Size.ANY_SIZE) {
            w = GARandom.GARandomInt(minX, maxX);
        } else if (w < 0) {
            w = nx;       // do nothing
        } else if (minX == maxX) {
            minX = maxX = w;
        } else {
            if (w < minX) {
                w = minX;
            }
            if (w > maxX) {
                w = maxX;
            }
        }

        if (h == Size.ANY_SIZE) {
            h = GARandom.GARandomInt(minY, maxY);
        } else if (h < 0) {
            h = ny;       // do nothing
        } else if (minY == maxY) {
            minY = maxY = h;
        } else {
            if (h < minY) {
                h = minY;
            }
            if (h > maxY) {
                h = maxY;
            }
        }
// Move the bits into the right position.  If we're smaller, then shift to
// the smaller size before we do the resize (the resize method maintains bit
// integrety).  If we're larger, do the move after the resize.  If we're the
// same size the we don't do anything.  When we're adding more bits, the new
// bits get set randomly to 0 or 1.
        if (w < nx) {
            final int y = (int) GAUtils.GAMin(ny, h);
            for (int j = 0; j < y; j++) {
                bs.move(j * w, j * nx, w);
            }
        }
        bs.resize(w * h);
        if (w > nx) {        // adjust the existing chunks of bits
            final int y = (int) GAUtils.GAMin(ny, h);
            for (int j = y - 1; j >= 0; j--) {
                bs.move(j * w, j * nx, nx);
                for (int i = nx; i < w; i++) {
                    bs.bit(j * w + i, (short) GARandom.GARandomBit());
                }
            }
        }
        if (h > ny) {        // change in height is always new bits
            for (int i = w * ny; i < w * h; i++) {
                bs.bit(i, (short) GARandom.GARandomBit());
            }
        }
        nx = w;
        ny = h;
        _evaluated = false;
        return bs.sz;
    }

    private int resizeBehaviour(final int which) {
        int val = 0;
        if (which == Dimension.WIDTH) {
            if (maxX == minX) {
                val = Size.FIXED_SIZE;
            } else {
                val = maxX;
            }
        } else if (which == Dimension.HEIGHT) {
            if (maxY == minY) {
                val = Size.FIXED_SIZE;
            } else {
                val = maxY;
            }
        }
        return val;
    }

    private int resizeBehaviour(final int which, final int lower, final int upper) {
        if (upper < lower) {
            GAError.GAErr(className(), "resizeBehaviour", GAError.gaErrBadResizeBehaviour);
            return resizeBehaviour(which);
        }

        switch (which) {
            case Dimension.WIDTH:
                minX = lower;
                maxX = upper;
                if (nx > upper) {
                    resize(upper, ny);
                }
                if (nx < lower) {
                    resize(lower, ny);
                }
                break;

            case Dimension.HEIGHT:
                minY = lower;
                maxY = upper;
                if (ny > upper) {
                    resize(nx, upper);
                }
                if (ny < lower) {
                    resize(nx, lower);
                }
                break;

            default:
                break;
        }
        return resizeBehaviour(which);
    }

    int resizeBehaviour(final int lowerX, final int upperX, final int lowerY, final int upperY) {
        return resizeBehaviour(Dimension.WIDTH, lowerX, upperX) * resizeBehaviour(Dimension.HEIGHT, lowerY, upperY);
    }

    private void copy(final GA2DBinaryStringGenome orig,
                      final int r, final int s,
                      final int x, final int y,
                      int w, int h) {
        if (w == 0 || x >= orig.nx || r >= nx ||
            h == 0 || y >= orig.ny || s >= ny) {
            return;
        }
        if (x + w > orig.nx) {
            w = orig.nx - x;
        }
        if (y + h > orig.ny) {
            h = orig.ny - y;
        }
        if (r + w > nx) {
            w = nx - r;
        }
        if (s + h > ny) {
            h = ny - s;
        }

        for (int j = 0; j < h; j++) {
            bs.copy(orig.bs, (s + j) * nx + r, (y + j) * orig.nx + x, w);
        }
        _evaluated = false;
    }

    int equal(final GA2DBinaryStringGenome orig, final int x, final int y, final int srcx, final int srcy,
              final int w, final int h) {
        int eq = 0;
        for (int j = 0; j < h; j++) {
            eq += bs.equal(orig.bs, (y + j) * nx + x, (srcy + j) * nx + srcx, w) ? 1 : 0;
        }
        return eq == h ? 1 : 0;
    }

    void set(final int x, final int y, int w, int h) {
        if (x + w > nx) {
            w = nx - x;
        }
        if (y + h > ny) {
            h = ny - y;
        }

        for (int j = 0; j < h; j++) {
            bs.set((y + j) * nx + x, w);
        }
        _evaluated = false;
    }

    void unset(final int x, final int y, int w, int h) {
        if (x + w > nx) {
            w = nx - x;
        }
        if (y + h > ny) {
            h = ny - y;
        }

        for (int j = 0; j < h; j++) {
            bs.unset((y + j) * nx + x, w);
        }
        _evaluated = false;
    }

    void randomize(final int x, final int y, int w, int h) {
        if (x + w > nx) {
            w = nx - x;
        }
        if (y + h > ny) {
            h = ny - y;
        }

        for (int j = 0; j < h; j++) {
            bs.randomize((y + j) * nx + x, w);
        }
        _evaluated = false;
    }

    void randomize() {
        bs.randomize();
    }

    void move(final int x, final int y, final int srcx, final int srcy, int w, int h) {
        if (srcx + w > nx) {
            w = nx - srcx;
        }
        if (x + w > nx) {
            w = nx - x;
        }
        if (srcy + h > ny) {
            h = ny - srcy;
        }
        if (y + h > ny) {
            h = ny - y;
        }

        if (srcy < y) {
            for (int j = h - 1; j >= 0; j--) {
                bs.move((y + j) * nx + x, (srcy + j) * nx + srcx, w);
            }
        } else {
            for (int j = 0; j < h; j++) {
                bs.move((y + j) * nx + x, (srcy + j) * nx + srcx, w);
            }
        }
        _evaluated = false;
    }
}
