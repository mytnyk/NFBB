package ga;

import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 5, 2004
 * Time: 3:33:32 PM
 * Description:
 * The population holds an array of pointers to genomes.  It also keeps
 * track of the fitness statistics for the genomes in the population.
 */

/**
 * ----------------------------------------------------------------------------
 * size
 * Use the size member function to get and set the size of the population.  The
 * population allocates space for genomes in chunks, so you can vary the
 * chunksize as well if you are really tight for space.  The compact member
 * function will remove all extra pointers.  If you shrink the population to 0
 * then you cannot use the 'size' method to make the population bigger.  When
 * resizing to a larger size, we clone randomly individuals from the existing
 * population.
 * <p/>
 * sort
 * The sort member is defined so that it can work on a const population.  It
 * does not change the logical state of the population, but it does change its
 * physical state.  We sort from best (0th individual) to worst (n-1).  The sort
 * figures out whether high is best or low is best.
 * <p/>
 * evaluate
 * If you want to force an evaluation, pass true to the evaluate member
 * function.  Otherwise the population will use its internal state to determine
 * whether or not it needs to do the evaluation.
 * <p/>
 * initialize
 * This method determines how the population should be initialized.  The
 * default is to call the initializer for each genome.
 * <p/>
 * statistics
 * Update the statistics.  We do this only on-demand so that no unneeded
 * calculations take place.
 * <p/>
 * diversity
 * Like the statistics function, we call this one only on demand.  This member
 * function can be particularly expensive, especially for large populations.  So
 * we store the values and update them only as needed.  The population diversity
 * measure is the average of the individual measures (less the diagonal scores).
 * ----------------------------------------------------------------------------
 */

final class GAPopulation implements GAID, PInitializer, PEvaluator {
    private static final int GA_POP_CHUNKSIZE = 10; // allocate chrom ptrs in chunks of this many

    private int neval;          // number of evals since initialization
    private int csz;            // how big are chunks we allocate?
    private int n;
    private int N;              // how many are in the population, allocated
    private int sortorder;          // is best a high score or a low score?
    private boolean rsorted;        // are the individuals sorted? (raw)
    private boolean ssorted;        // are the individuals sorted? (scaled)
    private boolean scaled;         // has the population been scaled?
    private boolean statted;        // are the stats valid?
    private boolean evaluated;      // has the population been evaluated?
    private boolean divved;         // has the population diversity been measured?
    private boolean selectready;    // has the selector been updated?
    private float rawSum;
    private float rawAve;     // sum, ave of the population's objectives
    private float rawMax;
    private float rawMin;     // max, min of the population's objectives
    private float rawVar;
    private float rawDev;     // variance, standard deviation
    private float popDiv;     // overall population diversity [0,)
    private float[] indDiv;   // table for genome similarities (diversity)
    private GAGenome[] rind;  // the individuals of the population (raw)
    private GAGenome[] sind;  // the individuals of the population (scaled)
    private float fitSum;
    private float fitAve;     // sum, ave of the population's fitness scores
    private float fitMax;
    private float fitMin;     // max, min of the population's fitness scores
    private float fitVar;
    private float fitDev;     // variance, standard deviation of fitness
    private GAScalingScheme sclscm;    // scaling method
    private GASelectionScheme slct;    // selection method
    private PInitializer init;         // initialization method
    private PEvaluator eval;           // population evaluation method
    private Object ud;                 // pointer to user data
    private GAGeneticAlgorithm ga;     // the ga that is using this population
    private GAEvalData evaldata;       // data for evaluator to use (optional)

    /**
     * This is a private method for adjusting the size of the arrays used by the
     * population object.  Unlike the size method, this method does not allocate
     * more genomes (but it will delete genomes if the specified size is smaller
     * than the current size).
     * This maintains the integrity of the diversity scores (but the new ones
     * will not have been set yet).
     * We return the total amount allocated (not the amount used).
     */
    private int grow(final int s) {
        if (s <= N) {
            return N;
        }
        final int oldsize = N;
        while (N < s) {
            N += csz;
        }
        GAGenome[] tmp;
        tmp = rind;
        rind = new GAGenome[N];
        System.arraycopy(tmp, 0, rind, 0, oldsize);
        //delete [] tmp;
        tmp = sind;
        sind = new GAGenome[N];
        System.arraycopy(tmp, 0, sind, 0, oldsize);
        //delete [] tmp;
        if (indDiv != null) {
            final float[] tmpd = indDiv;
            indDiv = new float[N * N];
            for (int i = 0; i < oldsize; i++) {
                System.arraycopy(tmpd, 0, indDiv, 0, oldsize);
            }
            //delete [] tmpd;
        }
        return N;
    }

    private static void QuickSortAscendingRaw(final GAGenome[] c, final int l, final int r) throws clCancelException {
        int i, j;
        final float v;
        GAGenome t;
        if (r > l) {
            v = c[r].score();
            i = l - 1;
            j = r;
            while (true) {
                while (c[++i].score() < v && i <= r) {
                }
                while (c[--j].score() > v && j > 0) {
                }
                if (i >= j) {
                    break;
                }
                t = c[i];
                c[i] = c[j];
                c[j] = t;
            }
            t = c[i];
            c[i] = c[r];
            c[r] = t;
            QuickSortAscendingRaw(c, l, i - 1);
            QuickSortAscendingRaw(c, i + 1, r);
        }
    }

    private static void QuickSortDescendingRaw(final GAGenome[] c, final int l, final int r) throws clCancelException {
        int i, j;
        final float v;
        GAGenome t;
        if (r > l) {
            v = c[r].score();
            i = l - 1;
            j = r;
            while (true) {
                while (c[++i].score() > v && i <= r) {
                }
                while (c[--j].score() < v && j > 0) {
                }
                if (i >= j) {
                    break;
                }
                t = c[i];
                c[i] = c[j];
                c[j] = t;
            }
            t = c[i];
            c[i] = c[r];
            c[r] = t;
            QuickSortDescendingRaw(c, l, i - 1);
            QuickSortDescendingRaw(c, i + 1, r);
        }
    }

    private static void QuickSortAscendingScaled(final GAGenome[] c, final int l, final int r) {
        int i, j;
        final float v;
        GAGenome t;
        if (r > l) {
            v = c[r].fitness();
            i = l - 1;
            j = r;
            while (true) {
                while (c[++i].fitness() < v && i <= r) {
                }
                while (c[--j].fitness() > v && j > 0) {
                }
                if (i >= j) {
                    break;
                }
                t = c[i];
                c[i] = c[j];
                c[j] = t;
            }
            t = c[i];
            c[i] = c[r];
            c[r] = t;
            QuickSortAscendingScaled(c, l, i - 1);
            QuickSortAscendingScaled(c, i + 1, r);
        }
    }

    private static void QuickSortDescendingScaled(final GAGenome[] c, final int l, final int r) {
        int i, j;
        final float v;
        GAGenome t;
        if (r > l) {
            v = c[r].fitness();
            i = l - 1;
            j = r;
            while (true) {
                while (c[++i].fitness() > v && i <= r) {
                }
                while (c[--j].fitness() < v && j > 0) {
                }
                if (i >= j) {
                    break;
                }
                t = c[i];
                c[i] = c[j];
                c[j] = t;
            }
            t = c[i];
            c[i] = c[r];
            c[r] = t;
            QuickSortDescendingScaled(c, l, i - 1);
            QuickSortDescendingScaled(c, i + 1, r);
        }
    }

    public boolean sameClass(final GAID b) {
        return classID() == b.classID();
    }

    public String className() {
        return "GAPopulation";
    }

    public int classID() {
        return Population;
    }

    /**
     * This is the default population initializer.  It simply calls the initializer
     * for each member of the population.  Then we touch the population to tell it
     * that it needs to update stats and/or sort (but we don't actually force
     * either one to occur.
     * The population object takes care of setting/unsetting the status flags.
     */
    public void initializer(final GAPopulation p) {
        for (int i = 0; i < p.size(); i++) {
            p.individual(i).initialize();
        }
    }


    /**
     * The default evaluator simply calls the evaluate member of each genome in
     * the population.  The population object takes care of setting/unsetting the
     * status flags for indicating when the population needs to be updated again
     */
    public void evaluator(final GAPopulation p) throws clCancelException {
        for (int i = 0; i < p.size(); i++) {
            p.individual(i).evaluate();
        }
    }

    interface SortBasis {
        int RAW = 0;
        int SCALED = 1;
    }

    interface SortOrder {
        int LOW_IS_BEST = 0;
        int HIGH_IS_BEST = 1;
    }

    interface Replacement {
        int BEST = -1;
        int WORST = -2;
        int RANDOM = -3;
    }

    /**
     * ----------------------------------------------------------------------------
     * Population
     * <p/>
     * The population class is basically just a holder for the genomes.  We also
     * keep track of statistics about the fitness of our genomes.  We don't care
     * what kind of genomes we get.  To create the population we call the clone
     * method of the genome we're given.
     * By default we do not calculate the population's diversity, so we set the
     * div matrix to NULL.
     * ----------------------------------------------------------------------------
     */
    GAPopulation() {
        csz = N = GA_POP_CHUNKSIZE;
        n = 0;
        while (N < n) {
            N += csz;
        }

        rind = new GAGenome[N];
        sind = new GAGenome[N];
        indDiv = null;

        neval = 0;
        rawSum = rawAve = rawDev = rawVar = rawMax = rawMin = (float) 0.0;
        fitSum = fitAve = fitDev = fitVar = fitMax = fitMin = (float) 0.0;
        popDiv = (float) -1.0;
        rsorted = ssorted = evaluated = false;
        scaled = statted = divved = selectready = false;
        sortorder = SortOrder.HIGH_IS_BEST;
        init = this;
        eval = this;
        slct = new GARouletteWheelSelector();
        slct.assign(this);
        sclscm = new GALinearScaling();
        evaldata = null;
        ga = null;
    }

    GAPopulation(final GAGenome c, final int popsize) {
        csz = N = GA_POP_CHUNKSIZE;
        n = popsize < 1 ? 1 : popsize;
        while (N < n) {
            N += csz;
        }

        rind = new GAGenome[N];
        sind = new GAGenome[N];
        for (int i = 0; i < n; i++) {
            rind[i] = (GAGenome) c.clone(GAGenome.CloneMethod.ATTRIBUTES);
        }
        System.arraycopy(rind, 0, sind, 0, N);
        indDiv = null;

        neval = 0;
        rawSum = rawAve = rawDev = rawVar = rawMax = rawMin = (float) 0.0;
        fitSum = fitAve = fitDev = fitVar = fitMax = fitMin = (float) 0.0;
        popDiv = (float) -1.0;
        rsorted = ssorted = evaluated = false;
        scaled = statted = divved = selectready = false;
        sortorder = SortOrder.HIGH_IS_BEST;
        init = this;
        eval = this;
        slct = new GARouletteWheelSelector();
        slct.assign(this);
        sclscm = new GALinearScaling();
        evaldata = null;
        ga = null;
    }

    GAPopulation(final GAPopulation orig) throws clCancelException {
        n = N = 0;
        rind = sind = null;
        indDiv = null;
        sclscm = null;
        slct = null;
        evaldata = null;
        copy(orig);
    }

    GAPopulation assign(final GAPopulation arg) throws clCancelException {
        copy(arg);
        return this;
    }

    void delete() {
        for (int i = 0; i < n; i++) {
            rind[i].delete();
        }
        //delete [] rind;
        //delete [] sind;
        //delete [] indDiv;
        if (sclscm != null) {
            sclscm.delete();
        }
        if (slct != null) {
            slct.delete();
        }
        if (evaldata != null) {
            evaldata.delete();
        }
    }

    public Object clone() {
        try {
            return new GAPopulation(this);
        } catch (clCancelException e) {
            e.printStackTrace();  // Add your code here.
        }
        return null;
    }

    /**
     * Make a complete copy of the original population.  This is a deep copy of
     * the population object - we clone everything in the genomes and copy all of
     * the population's information.
     */
    void copy(final GAPopulation arg) throws clCancelException {
        int i;
        for (i = 0; i < n; i++) {
            rind[i].delete();
        }
        //delete [] rind;
        //delete [] sind;
        //delete [] indDiv;
        if (sclscm != null) {
            sclscm.delete();
        }
        if (slct != null) {
            slct.delete();
        }
        if (evaldata != null) {
            evaldata.delete();
        }

        csz = arg.csz;
        N = arg.N;
        n = arg.n;
        rind = new GAGenome[N];
        for (i = 0; i < n; i++) {
            rind[i] = (GAGenome) arg.rind[i].clone();
        }
        sind = new GAGenome[N];
        System.arraycopy(rind, 0, sind, 0, N);

        if (arg.indDiv != null) {
            indDiv = new float[N * N];
            System.arraycopy(arg.indDiv, 0, indDiv, 0, N * N);
        } else {
            indDiv = null;
        }

        sclscm = (GAScalingScheme) arg.sclscm.clone();
        scaled = false;
        if (arg.scaled) {
            scale();
        }

        slct = (GASelectionScheme) arg.slct.clone();
        slct.assign(this);
        selectready = false;
        if (arg.selectready) {
            prepselect();
        }

        if (arg.evaldata != null) {
            evaldata = (GAEvalData) arg.evaldata.clone();
        } else {
            evaldata = null;
        }

        neval = 0;      // don't copy the evaluation count!
        rawSum = arg.rawSum;
        rawAve = arg.rawAve;
        rawMax = arg.rawMax;
        rawMin = arg.rawMin;
        rawVar = arg.rawVar;
        rawDev = arg.rawDev;
        popDiv = arg.popDiv;

        fitSum = arg.fitSum;
        fitAve = arg.fitAve;
        fitMax = arg.fitMax;
        fitMin = arg.fitMin;
        fitVar = arg.fitVar;
        fitDev = arg.fitDev;

        sortorder = arg.sortorder;
        rsorted = arg.rsorted;
        ssorted = false;    // we must sort at some later point
        statted = arg.statted;
        evaluated = arg.evaluated;
        divved = arg.divved;

        init = arg.init;
        eval = arg.eval;
        ud = arg.ud;
        ga = arg.ga;
    }

    int size() {
        return n;
    }

    /**
     * Resize the population.  If we shrink, we delete the extra genomes.  If
     * we grow, we clone new ones (and we DO NOT initialize them!!!).  When we
     * trash the genomes, we delete the worst of the population!  We do not
     * free up the space used by the array of pointers, but we do free up the
     * space used by the genomes.
     * We do a clone of the genome contents so that we don't have to initialize
     * the new ones (what if the population has a custom initilizer?).  We randomly
     * pick which ones to clone from the existing individuals.  If the population
     * contains no genomes, then we post an error message (since there are no
     * individuals from which to clone the new ones).
     * If the population was evaluated, then we evaluate the new genomes.  We
     * do not sort nor restat the population, and we tag the statted and sorted
     * flags to reflect the fact that they are no longer valid.
     * Resizing to a bigger size is the same as a batch 'add'
     */
    int size(final int popsize) throws clCancelException {
        if (popsize == n) {
            return n;
        }
        if (n == 0 && popsize > 0) {
            GAError.GAErr("GAPopuluation", "size", GAError.gaErrNoIndividuals);
            return n;
        }
        if (popsize > n) {
            grow(popsize);
            for (int i = n; i < popsize; i++) {
                rind[i] = (GAGenome) rind[GARandom.GARandomInt(0, n - 1)].clone(GAGenome.CloneMethod.CONTENTS);
            }
            rsorted = false;
        } else {
            for (int i = popsize; i < n; i++) // trash the worst ones (if sorted)
            {
                rind[i].delete();             // may not be sorted!!!!
            }
        }
        System.arraycopy(rind, 0, sind, 0, N);
        ssorted = scaled = statted = divved = selectready = false;
        n = popsize;
        if (evaluated) {
            evaluate(true);
        }
        return n;
    }

    int chunksize() {
        return csz;
    }

    int chunksize(final int csize) {
        return csz = csize;
    }

    /**
     * Get rid of 'extra' memory that we have allocated.  We just trash the
     * diversity matrix and flag it as being invalid.  Return the amount
     * allocated (which is also the amount used).
     */
    int compact() {
        if (n == N) {
            return N;
        }
        GAGenome[] tmp;
        tmp = rind;
        rind = new GAGenome[n];
        System.arraycopy(tmp, 0, rind, 0, n);
        //delete [] tmp;
        tmp = sind;
        sind = new GAGenome[n];
        System.arraycopy(tmp, 0, sind, 0, n);
        //delete [] tmp;
        if (indDiv != null) {
            //delete [] indDiv;
            indDiv = null;
        }
        return N = n;
    }

    private void touch() {
        rsorted = ssorted = selectready = divved = statted = scaled = evaluated = false;
    }

    private void statistics() throws clCancelException {
        statistics(false);
    }

    /**
     * Evaluate each member of the population and store basic population statistics
     * in the member variables.  It is OK to run this on a const object - it
     * changes to physical state of the population, but not the logical state.
     * The partial sums are normalized to the range [0,1] so that they can be
     * used whether the population is sorted as low-is-best or high-is-best.
     * Individual 0 is always the best individual, and the partial sums are
     * calculated so that the worst individual has the smallest partial sum.  All
     * of the partial sums add to 1.0.
     */

    private void statistics(final boolean flag) throws clCancelException {
        if (statted && !flag) {
            return;
        }
        final GAPopulation This = this;

        if (n > 0) {
            float tmpsum;
            This.rawMin = This.rawMax = tmpsum = rind[0].score();

            int i;
            for (i = 1; i < n; i++) {
                tmpsum += rind[i].score();
                This.rawMax = GAUtils.GAMax(rawMax, rind[i].score());
                This.rawMin = GAUtils.GAMin(rawMin, rind[i].score());
            }
            final float tmpave = tmpsum / n;
            This.rawAve = tmpave;
            This.rawSum = tmpsum;   // if scores are huge we'll lose data here

            float tmpvar = (float) 0.0;
            if (n > 1) {
                for (i = 0; i < n; i++) {
                    float s = rind[i].score() - This.rawAve;
                    s *= s;
                    tmpvar += s;
                }
                tmpvar /= n - 1;
            }
            This.rawDev = (float) Math.sqrt(tmpvar);
            This.rawVar = tmpvar;   // could lose data if huge variance
        } else {
            This.rawMin = This.rawMax = This.rawSum = (float) 0.0;
            This.rawDev = This.rawVar = (float) 0.0;
        }
        This.statted = true;
    }

    private void diversity() {
        diversity(false);
    }

    /**
     * Calculate the population's diversity score.  The matrix is triangular and
     * we don't have to calculate the diagonals.  This assumes that div(i,j) is
     * the same as div(j,i) (for our purposes this will always be true, but it is
     * possible for someone to override some of the individuals in the population
     * and not others).
     * For now we keep twice as many diversity numbers as we need.  We need only
     * n*(n-1)/2, but I can't seem to figure out an efficient way to map i,j to the
     * reduced n*(n-1)/2 set (remember that the diagonals are always 0.0).
     * The diversity of the entire population is just the average of all the
     * individual diversities.  So if every individual is completely different from
     * all of the others, the population diversity is > 0.  If they are all the
     * same, the diversity is 0.0.  We don't count the diagonals for the population
     * diversity measure.  0 means minimal diversity means all the same.
     */
    private void diversity(final boolean flag) {
        if (divved && !flag) {
            return;
        }
        final GAPopulation This = this;
        if (n > 1) {
            if (This.indDiv == null) {
                This.indDiv = new float[N * N];
            }

            This.popDiv = (float) 0.0;
            for (int i = 0; i < n; i++) {
                This.indDiv[i * n + i] = (float) 0.0;
                for (int j = i + 1; j < n; j++) {
                    This.indDiv[j * n + i] = This.indDiv[i * n + j] =
                                             individual(i).compare(individual(j));
                    This.popDiv += indDiv[i * n + j];
                }
            }
            This.popDiv /= n * (n - 1) >> 1;
        } else {
            This.popDiv = (float) 0.0;
        }
        This.divved = true;
    }

    void scale() throws clCancelException {
        scale(false);
    }

    /**
     * Do the scaling on the population.  Like the statistics and diversity, this
     * method does not change the contents of the population, but it does change
     * the values of the status members of the object.  So we allow it to work on
     * a const population.
     */
    private void scale(final boolean flag) throws clCancelException {
        if (scaled && !flag) {
            return;
        }
        final GAPopulation This = this;

        if (n > 0) {
            sclscm.evaluate(This);
            float tmpsum;
            This.fitMin = This.fitMax = tmpsum = sind[0].fitness();
            int i;
            for (i = 1; i < n; i++) {
                tmpsum += sind[i].fitness();
                This.fitMax = GAUtils.GAMax(fitMax, sind[i].fitness());
                This.fitMin = GAUtils.GAMin(fitMin, sind[i].fitness());
            }
            final float tmpave = tmpsum / n;
            This.fitAve = tmpave;
            This.fitSum = tmpsum;	// if scores are huge we'll lose data here

            float tmpvar = (float) 0.0;
            if (n > 1) {
                for (i = 0; i < n; i++) {
                    float s = sind[i].fitness() - This.fitAve;
                    s *= s;
                    tmpvar += s;
                }
                tmpvar /= n - 1;
            }
            This.fitDev = (float) Math.sqrt(tmpvar);
            This.fitVar = tmpvar;	// could lose data if huge variance
        } else {
            This.fitMin = This.fitMax = This.fitSum = (float) 0.0;
            This.fitVar = This.fitDev = (float) 0.0;
        }

        This.scaled = true;
        This.ssorted = false;
    }

    private void prepselect() throws clCancelException {
        prepselect(false);
    }

    private void prepselect(final boolean flag) throws clCancelException {
        if (selectready && !flag) {
            return;
        }
        final GAPopulation This = this;
        This.slct.update();
        This.selectready = true;
    }

    /**
     * Sort using the quicksort method.  The sort order depends on whether a high
     * number means 'best' or a low number means 'best'.  Individual 0 is always
     * the 'best' individual, Individual n-1 is always the 'worst'.
     * We may sort either array of individuals - the array sorted by raw scores
     * or the array sorted by scaled scores.
     */
    void sort(final boolean flag, final int basis) throws clCancelException {
        final GAPopulation This = this;
        if (basis == SortBasis.RAW) {
            if (!rsorted || flag) {
                if (sortorder == SortOrder.LOW_IS_BEST) {
                    QuickSortAscendingRaw(This.rind, 0, n - 1);
                } else {
                    QuickSortDescendingRaw(This.rind, 0, n - 1);
                }
                This.selectready = false;
            }
            This.rsorted = true;
        } else if (basis == SortBasis.SCALED) {
            if (!ssorted || flag) {
                if (sortorder == SortOrder.LOW_IS_BEST) {
                    QuickSortAscendingScaled(This.sind, 0, n - 1);
                } else {
                    QuickSortDescendingScaled(This.sind, 0, n - 1);
                }
                This.selectready = false;
            }
            This.ssorted = true;
        }
    }

    float sum() throws clCancelException {
        if (!statted) {
            statistics();
        }
        return rawSum;
    }

    float ave() throws clCancelException {
        if (!statted) {
            statistics();
        }
        return rawAve;
    }

    float var() throws clCancelException {
        if (!statted) {
            statistics();
        }
        return rawVar;
    }

    float dev() throws clCancelException {
        if (!statted) {
            statistics();
        }
        return rawDev;
    }

    float max() throws clCancelException {
        if (!statted) {
            statistics();
        }
        return rawMax;
    }

    float min() throws clCancelException {
        if (!statted) {
            statistics();
        }
        return rawMin;
    }

    float div() {
        if (!divved) {
            diversity();
        }
        return popDiv;
    }

    float div(final int i, final int j) {
        if (!divved) {
            diversity();
        }
        return indDiv[i * n + j];
    }

    float fitsum() throws clCancelException {
        if (!scaled) {
            scale();
        }
        return fitSum;
    }

    float fitave() throws clCancelException {
        if (!scaled) {
            scale();
        }
        return fitAve;
    }

    float fitmax() throws clCancelException {
        if (!scaled) {
            scale();
        }
        return fitMax;
    }

    float fitmin() throws clCancelException {
        if (!scaled) {
            scale();
        }
        return fitMin;
    }

    float fitvar() throws clCancelException {
        if (!scaled) {
            scale();
        }
        return fitVar;
    }

    float fitdev() throws clCancelException {
        if (!scaled) {
            scale();
        }
        return fitDev;
    }

    int nevals() {
        return neval;
    }

    void evaluate() throws clCancelException {
        evaluate(false);
    }

    void evaluate(final boolean flag) throws clCancelException {
        if (!evaluated || flag) {
            eval.evaluator(this);
            neval++;
            scaled = statted = divved = rsorted = ssorted = false;
        }
        evaluated = true;
    }

    PEvaluator evaluator() {
        return eval;
    }

    PEvaluator evaluator(final PEvaluator e) {
        evaluated = false;
        return eval = e;
    }

    void initialize() {
        neval = 0;
        init.initializer(this);
        touch();
    }

    PInitializer initializer() {
        return init;
    }

    PInitializer initializer(final PInitializer i) {
        return init = i;
    }

    int order() {
        return sortorder;
    }

    int order(final int flag) {
        if (sortorder == flag) {
            return flag;
        }
        sortorder = flag;
        rsorted = ssorted = false;
        return flag;
    }

    GAGenome select() throws clCancelException {
        if (!selectready) {
            prepselect();
        }
        return slct.select();
    }

    GASelectionScheme selector() {
        return slct;
    }

    // Return a reference to the selection object.
    GASelectionScheme selector(final GASelectionScheme s) {
        slct.delete();
        slct = (GASelectionScheme) s.clone();
        slct.assign(this);
        selectready = false;
        return slct;
    }

    GAScalingScheme scaling() {
        final GAPopulation This = this;
        This.scaled = false;
        return sclscm;
    }

    // Return a reference to the scaling object.
    GAScalingScheme scaling(final GAScalingScheme s) {
        sclscm.delete();
        sclscm = (GAScalingScheme) s.clone();
        scaled = false;
        return sclscm;
    }

    GAGeneticAlgorithm geneticAlgorithm() {
        return ga;
    }

    GAGeneticAlgorithm geneticAlgorithm(final GAGeneticAlgorithm g) {
        for (int i = 0; i < n; i++) {
            rind[i].geneticAlgorithm(g);
        }
        return ga = g;
    }

    Object userData() {
        return ud;
    }

    Object userData(final Object u) {
        return ud = u;
    }

    GAEvalData evalData() {
        return evaldata;
    }

    GAEvalData evalData(final GAEvalData o) {
        evaldata.delete();
        evaldata = (GAEvalData) o.clone();
        return evaldata;
    }

    GAGenome best() throws clCancelException {
        return best(0);
    }

    GAGenome best(final int i) throws clCancelException {
        return best(i, SortBasis.RAW);
    }

    private GAGenome best(final int i, final int basis) throws clCancelException {
        if (basis == SortBasis.SCALED) {
            scale();
        }
        sort(false, basis);
        return basis == SortBasis.RAW ? rind[i] : sind[i];
    }

    GAGenome worst() throws clCancelException {
        return worst(0);
    }

    private GAGenome worst(final int i) throws clCancelException {
        return worst(i, SortBasis.RAW);
    }

    private GAGenome worst(final int i, final int basis) throws clCancelException {
        if (basis == SortBasis.SCALED) {
            scale();
        }
        sort(false, basis);
        return basis == SortBasis.RAW ? rind[n - 1 - i] : sind[n - 1 - i];
    }

    GAGenome individual(final int i) {
        return individual(i, SortBasis.RAW);
    }

    GAGenome individual(final int i, final int basis) {
        return basis == SortBasis.RAW ? rind[i] : sind[i];
    }

    /**
     * Add the specified individual to the population.  We don't update the stats
     * or sort - let those get updated next time they are needed.
     * Notice that it is possible to add individuals to the population that are
     * not the same type as the other genomes in the population.  Eventually we
     * probably won't allow this (or at least we'll have to fix things so that the
     * behaviour is completely defined).
     * If you invoke the add with a genome reference, the population will make
     * a clone of the genome then it owns it from then on.  If you invoke add with
     * a genome pointer, then the population does not allocate any memory - it uses
     * the memory pointed to by the argument.  So don't trash the genome without
     * first letting the population know about the change.
     */
    GAGenome addnew(final GAGenome g) {
        return add((GAGenome) g.clone());
    }

    /**
     * This one does *not* allocate space for the genome - it uses the one that
     * was passed to us.  So the caller should not free it up or leave it dangling!
     * We own it from now on (unless remove is called on it), and the population
     * will destroy it when the population destructor is invoked.
     */
    GAGenome add(final GAGenome c) {
        if (c == null) {
            return c;
        }
        grow(n + 1);
        rind[n] = sind[n] = c;
        if (ga != null) {
            rind[n].geneticAlgorithm(ga);
        }
        n++;

        rsorted = ssorted = false;    // may or may not be true, but must be sure
        evaluated = scaled = statted = divved = selectready = false;

        return c;
    }

    /**
     * Remove the xth genome from the population.  If index is out of bounds, we
     * return NULL.  Otherwise we return a pointer to the genome that was
     * removed.  The population is now no longer responsible for freeing the
     * memory used by that genome.
     * We don't touch the sorted flag for the array we modify - a remove will not
     * affect the sort order.
     */
    GAGenome remove(int i, final int basis) throws clCancelException {
        GAGenome removed = null;
        if (i == Replacement.BEST) {
            sort(false, basis);
            i = 0;
        } else if (i == Replacement.WORST) {
            sort(false, basis);
            i = n - 1;
        } else if (i == Replacement.RANDOM) {
            i = GARandom.GARandomInt(0, n - 1);
        } else if (i < 0 || i >= (int) n) {
            return removed;
        }

        if (basis == SortBasis.RAW) {
            removed = rind[i];
            // memmove(&(rind[i]), &(rind[i+1]), (n-i-1)*sizeof(GAGenome *));
            if (n - i - 1 > 0)// TODO: make sure this works
            {
                System.arraycopy(rind, i + 1, rind, i, n - i - 1);
            }
            System.arraycopy(rind, 0, sind, 0, N);
            ssorted = false;
        } else if (basis == SortBasis.SCALED) {
            removed = sind[i];
            // memmove(&(sind[i]), &(sind[i+1]), (n-i-1)*sizeof(GAGenome *));
            if (n - i - 1 > 0)// TODO: make sure this works
            {
                System.arraycopy(sind, i + 1, sind, i, n - i - 1);
            }
            System.arraycopy(sind, 0, rind, 0, N);
            rsorted = false;
        } else {
            return removed;
        }
        n--;
        evaluated = false;
// *** should be smart about these and do incremental update?
        scaled = statted = divved = selectready = false;
        return removed;
    }

    /**
     * Remove the specified genome from the population.  If the genome is
     * not in the population, we return NULL.  We do a linear search here (yuk for
     * large pops, but little else we can do).  The memory used by the genome is
     * now the responsibility of the caller.
     */
    GAGenome remove(final GAGenome r) throws clCancelException {
        GAGenome removed = null;
        if (r == null) {
            return removed;
        }
        int i;
        for (i = 0; i < n && rind[i] != r; i++) {
        }
        if (i < n) {
            removed = remove(i, SortBasis.RAW);
        }
        return removed;
    }

    GAGenome replace(final GAGenome repl, final int which) throws clCancelException {
        return replace(repl, which, SortBasis.RAW);
    }

    /**
     * Replace the specified genome with the one that is passed to us then
     * return the one that got replaced.  Use the replacement flags to determine
     * which genome will be replaced.  If we get a genome as the second
     * argument, then replace that one.  If we get a NULL genome, then we
     * return a NULL and don't do anything.
     * If the population is sorted, then we maintain the sort by doing a smart
     * replacement.
     * If the population is not sorted, then we just do the replacement without
     * worrying about the sort.  Replace best and worst both require that we know
     * which chromsomes are which, so we do a sort before we do the replacement,
     * then we do a smart replacement.
     * In both cases we flag the stats as out-of-date, but we do not update the
     * stats.  Let that happen when it needs to happen.
     * If which is < 0 then it is a flag that tells us to do a certain kind of
     * replacement.  Anything non-negative is assumed to be an index to a
     * genome in the population.
     * This does not affect the state of the evaluated member - it assumes that
     * the individual genome has a valid number for its score.
     */
    private GAGenome replace(final GAGenome repl, final int which, final int basis) throws clCancelException {
        int i = -1;
        GAGenome orig = null;
        if (repl == null) {
            return orig;
        }
        switch (which) {
            case Replacement.BEST:
                sort(false, basis);
                i = 0;
                break;
            case Replacement.WORST:
                sort(false, basis);
                i = n - 1;
                break;
            case Replacement.RANDOM:
                i = GARandom.GARandomInt(0, n - 1);
                break;
            default:
                if (which >= 0 && which < (int) n) {
                    i = which;
                }
                break;
        }
        if (i >= 0) {
// We could insert this properly if the population is sorted, but that would
// require us to evaluate the genome, and we don't want to do that 'cause that
// will screw up any parallel implementations.  So we just stick it in the
// population and let the sort take care of it at a later time as needed.
            if (basis == SortBasis.RAW) {
                orig = rind[i]; // keep the original to return at the end
                rind[i] = repl;
                System.arraycopy(rind, 0, sind, 0, N);
            } else {
                orig = sind[i]; // keep the original to return at the end
                sind[i] = repl;
                System.arraycopy(sind, 0, rind, 0, N);
            }
            rsorted = ssorted = false;	// must sort again
            // flag for recalculate stats
            statted = false;
            // Must flag for a new evaluation.
            evaluated = false;
            // No way to do incremental update of scaling info since we don't know what the
            // scaling object will do.
            scaled = false;
            // *** should do an incremental update of the diversity here so we don't
            // recalculate all of the diversities when only one is updated
            divved = false;
            // selector needs update
            selectready = false;
            // make sure the genome has the correct genetic algorithm pointer
            if (ga != null) {
                repl.geneticAlgorithm(ga);
            }
        }
        return orig;
    }

    /**
     * Replace the genome o in the population with the genome r.  Return a
     * pointer to the original genome, o.  This assumes that o exists in the
     * population.   If it does not, we return a NULL.  If the genomes are the
     * same, do nothing and return a pointer to the genome.
     */
    GAGenome replace(final GAGenome r, final GAGenome o) throws clCancelException {
        GAGenome orig = null;
        if (r == null || o == null) {
            return orig;
        }
        if (r == o) {
            return r;
        }
        int i;
        for (i = 0; i < n && rind[i] != o; i++) {
        }
        if (i < n) {
            orig = replace(r, i, SortBasis.RAW);
        }
        return orig;
    }

    void destroy(final int w, final int b) throws clCancelException {
        //delete
        remove(w, b);
    }
}

