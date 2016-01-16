package ga;

import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 5, 2004
 * Time: 3:32:52 PM
 * Description:
 * ----------------------------------------------------------------------------
 * Statistics class
 * We define this class as a storage object for the current state of the GA.
 * Whereas the parameters object keeps track of the user-definable settings for
 * the GA, the statistics object keeps track of the data that the GA generates
 * along the way.
 * ----------------------------------------------------------------------------
 */
public final class GAStatistics {
    // Default settings and their names.
    static final int gaDefNumBestGenomes = 1;
    static final int gaDefScoreFrequency1 = 1;
    static final int gaDefScoreFrequency2 = 100;
    static final int gaDefFlushFrequency = 0;
    static final String gaDefScoreFilename = "generations.dat";

    static final int NoScores = 0x00;
    private static final int Mean = 0x01;
    static final int Maximum = 0x02;
    private static final int Minimum = 0x04;
    private static final int Deviation = 0x08;
    private static final int Diversity = 0x10;
    static final int AllScores = 0xff;

    // These should be protected (accessible only to the GA class) but for now they
    // are publicly accessible.  Do not try to set these unless you know what you
    // are doing!!
    int numsel;     // number of selections since reset
    int numcro;     // number of crossovers since reset
    int nummut;     // number of mutations since reset
    int numrep;     // number of replacements since reset
    int numeval;    // number of individual evaluations since reset
    private int numpeval;     // number of population evals since reset


    private int curgen;       // current generation number
    private int scoreFreq;    // how often (in generations) to record scores
    private boolean dodiv;    // should we record diversity?

    private float maxever;        // maximum score since initialization
    private float minever;        // minimum score since initialization
    private float on;             // "on-line" performance (ave of all scores)
    private float offmax;         // "off-line" performance (ave of maximum)
    private float offmin;         // "off-line" performance (ave of minimum)

    private float aveInit;        // stats from the initial population
    private float maxInit;
    private float minInit;
    private float devInit;
    private float divInit;

    private float aveCur;         // stats from the current population
    private float maxCur;
    private float minCur;
    private float devCur;
    private float divCur;

    private int nconv;
    private int Nconv;     // how many scores we're recording (flushFreq)
    private float[] cscore;      // best score of last n generations

    private int nscrs;
    private int Nscrs;     // how many scores do we have?
    private int[] gen;           // generation number corresponding to scores
    private float[] aveScore;    // average scores of each generation
    private float[] maxScore;    // best scores of each generation
    private float[] minScore;    // worst scores of each generation
    private float[] devScore;    // stddev of each generation
    private float[] divScore;    // diversity of each generation
    private String scorefile;    // name of file to which scores get written
    private int which;           // which data to write to file
    private GAPopulation boa;    // keep a copy of the best genomes

    /**
     * For recording the convergence we have to keep a running list of the past N
     * best scores.  We just keep looping around and around the array of past
     * scores.  nconv keeps track of which one is the current one.  The current
     * item is thus nconv%Nconv.  The oldest is nconv%Nconv+1 or 0.
     */
    private void setConvergence(final float s) {
        nconv++;
        cscore[nconv % Nconv] = s;
    }

    /**
     * Set the score info to the appropriate values.  Update the score count.
     */
    private void setScore(final GAPopulation pop) throws clCancelException {
        aveCur = pop.ave();
        maxCur = pop.max();
        minCur = pop.min();
        devCur = pop.dev();
        divCur = dodiv ? pop.div() : (float) -1.0;

        if (Nscrs == 0) {
            return;
        }
        gen[nscrs] = curgen;
        aveScore[nscrs] = aveCur;
        maxScore[nscrs] = maxCur;
        minScore[nscrs] = minCur;
        devScore[nscrs] = devCur;
        divScore[nscrs] = divCur;
        nscrs++;
    }

    private void updateBestIndividual(final GAPopulation pop) throws clCancelException {
        updateBestIndividual(pop, false);
    }

    /**
     * Update the genomes in the 'best of all' population to reflect any
     * changes made to the current population.  We just grab the genomes with
     * the highest scores from the current population, and if they are higher than
     * those of the genomes in the boa population, they get copied.  Note that
     * the bigger the boa array, the bigger your running performance hit because
     * we have to look through all of the boa to figure out which are better than
     * those in the population.  The fastest way to use the boa is to keep only
     * one genome in the boa population.  A flag of 'True' will reset the boa
     * population so that it is filled with the best of the current population.
     * Unfortunately it could take a long time to update the boa array using the
     * copy method.  We'd like to simply keep pointers to the best genomes, but
     * the genomes change from generation to generation, so we can't depend on
     * that.
     * Notice that keeping boa is useful even for overlapping populations.  The
     * boa keeps individuals that are different from each other - the overlapping
     * population may not.  However, keeping boa is most useful for populations
     * with little overlap.
     * When we check to see if a potentially better member is already in our
     * best-of-all population, we use the operator== comparator not the genome
     * comparator to do the comparison.
     */
    private void updateBestIndividual(final GAPopulation pop, final boolean flag) throws clCancelException {
        if (boa == null || boa.size() == 0) {
            return; // do nothing
        }
        if (pop.order() != boa.order()) {
            boa.order(pop.order());
        }

        if (flag) {     // reset the BOA array
            int j = 0;
            for (int i = 0; i < boa.size(); i++) {
                boa.best(i).copy(pop.best(j));
                if (j < pop.size() - 1) {
                    j++;
                }
            }
            return;
        }

        if (boa.size() == 1) {  // there's only one boa so replace it with bop
            if (boa.order() == GAPopulation.SortOrder.HIGH_IS_BEST &&
                pop.best().score() > boa.best().score()) {
                boa.best().copy(pop.best());
            }
            if (boa.order() == GAPopulation.SortOrder.LOW_IS_BEST &&
                pop.best().score() < boa.best().score()) {
                boa.best().copy(pop.best());
            }
        } else {
            int i = 0, j, k;
            if (boa.order() == GAPopulation.SortOrder.HIGH_IS_BEST) {
                while (i < pop.size() && pop.best(i).score() > boa.worst().score()) {
                    for (k = 0;
                         pop.best(i).score() < boa.best(k).score() && k < boa.size();
                         k++) {
                    }
                    for (j = k; j < boa.size(); j++) {
                        if (pop.best(i) == boa.best(j)) {
                            break;
                        }
                        if (pop.best(i).score() > boa.best(j).score()) {
                            boa.worst().copy(pop.best(i));        // replace worst individual
                            boa.sort(true, GAPopulation.SortBasis.RAW);  // re-sort the population
                            break;
                        }
                    }
                    i++;
                }
            }
            if (boa.order() == GAPopulation.SortOrder.LOW_IS_BEST) {
                while (i < pop.size() && pop.best(i).score() < boa.worst().score()) {
                    for (k = 0;
                         pop.best(i).score() > boa.best(k).score() && k < boa.size();
                         k++) {
                    }
                    for (j = k; j < boa.size(); j++) {
                        if (pop.best(i) == boa.best(j)) {
                            break;
                        }
                        if (pop.best(i).score() < boa.best(j).score()) {
                            boa.worst().copy(pop.best(i));        // replace worst individual
                            boa.sort(true, GAPopulation.SortBasis.RAW);  // re-sort the population
                            break;
                        }
                    }
                    i++;
                }
            }
        }

    }

    /**
     * Write the current scores to file.  If this is the first chunk (ie gen[0]
     * is 0) then we create a new file.  Otherwise we append to an existing file.
     * We give no notice that we're overwriting the existing file!!
     */
    private void writeScores() {
        if (scorefile != null) {
            return;
        }
        // TODO: add the code for writing to file
    }

    /**
     * Resize the scores vectors to the specified amount.  Copy any scores that
     * exist.
     */
    private void resizeScores(final int n) {
        final int[] tmpi;
        float[] tmpf;

        if (n == 0) {
            //delete [] gen;
            gen = null;
            //delete [] aveScore;
            aveScore = null;
            //delete [] maxScore;
            maxScore = null;
            //delete [] minScore;
            minScore = null;
            //delete [] devScore;
            devScore = null;
            //delete [] divScore;
            divScore = null;
            nscrs = n;
        } else {
            tmpi = gen;
            gen = new int[n];
            if (tmpi != null) {
                System.arraycopy(tmpi, 0, gen, 0, n < Nscrs ? n : Nscrs);
            }
            //delete [] tmpi;

            tmpf = aveScore;
            aveScore = new float[n];
            if (tmpf != null) {
                System.arraycopy(tmpf, 0, aveScore, 0, n < Nscrs ? n : Nscrs);
            }
            //delete [] tmpf;

            tmpf = maxScore;
            maxScore = new float[n];
            if (tmpf != null) {
                System.arraycopy(tmpf, 0, maxScore, 0, n < Nscrs ? n : Nscrs);
            }
            //delete [] tmpf;

            tmpf = minScore;
            minScore = new float[n];
            if (tmpf != null) {
                System.arraycopy(tmpf, 0, minScore, 0, n < Nscrs ? n : Nscrs);
            }
            //delete [] tmpf;

            tmpf = devScore;
            devScore = new float[n];
            if (tmpf != null) {
                System.arraycopy(tmpf, 0, devScore, 0, n < Nscrs ? n : Nscrs);
            }
            //delete [] tmpf;

            tmpf = divScore;
            divScore = new float[n];
            if (tmpf != null) {
                System.arraycopy(tmpf, 0, divScore, 0, n < Nscrs ? n : Nscrs);
            }
            //delete [] tmpf;
            if (nscrs > n) {
                nscrs = n;
            }
        }
        Nscrs = n;
    }

    GAStatistics() {
        curgen = 0;
        numsel = numcro = nummut = numrep = numeval = numpeval = 0;
        maxever = minever = (float) 0.0;
        on = offmax = offmin = (float) 0.0;
        aveInit = maxInit = minInit = devInit = (float) 0.0;
        divInit = (float) -1.0;
        aveCur = maxCur = minCur = devCur = (float) 0.0;
        divCur = (float) -1.0;

        scoreFreq = gaDefScoreFrequency1;
        dodiv = false;  // default is do not calculate diversity

        nconv = 0;
        Nconv = 10;
        cscore = new float[Nconv];

        nscrs = 0;
        Nscrs = gaDefFlushFrequency;
        gen = new int[Nscrs];
        aveScore = new float[Nscrs];
        maxScore = new float[Nscrs];
        minScore = new float[Nscrs];
        devScore = new float[Nscrs];
        divScore = new float[Nscrs];
        scorefile = gaDefScoreFilename;
        which = Maximum;
        boa = null;
    }

    GAStatistics(final GAStatistics orig) {
        cscore = null;
        gen = null;
        aveScore = null;
        maxScore = null;
        minScore = null;
        devScore = null;
        divScore = null;
        scorefile = null;
        boa = null;
        copy(orig);
    }

    GAStatistics assign(final GAStatistics orig) {
        copy(orig);
        return this;
    }

    void delete() {
        /*delete [] cscore;
        delete [] gen;
        delete [] aveScore;
        delete [] maxScore;
        delete [] minScore;
        delete [] devScore;
        delete [] divScore;
        delete [] scorefile;*/
        boa.delete();
    }

    private void copy(final GAStatistics orig) {
        if (orig == this) {
            return;
        }
        curgen = orig.curgen;
        numsel = orig.numsel;
        numcro = orig.numcro;
        nummut = orig.nummut;
        numrep = orig.numrep;
        numeval = orig.numeval;
        numpeval = orig.numpeval;
        maxever = orig.maxever;
        minever = orig.minever;
        on = orig.on;
        offmax = orig.offmax;
        offmin = orig.offmin;
        aveInit = orig.aveInit;
        maxInit = orig.maxInit;
        minInit = orig.minInit;
        devInit = orig.devInit;
        divInit = orig.divInit;
        aveCur = orig.aveCur;
        maxCur = orig.maxCur;
        minCur = orig.minCur;
        devCur = orig.devCur;
        divCur = orig.divCur;
        scoreFreq = orig.scoreFreq;
        dodiv = orig.dodiv;
        nconv = orig.nconv;
        Nconv = orig.Nconv;
        //delete [] cscore;
        cscore = new float[Nconv];
        System.arraycopy(orig.cscore, 0, cscore, 0, Nconv);
        nscrs = orig.nscrs;
        Nscrs = orig.Nscrs;
        //delete [] gen;
        gen = new int[Nscrs];
        System.arraycopy(orig.gen, 0, gen, 0, Nscrs);
        //delete [] aveScore;
        aveScore = new float[Nscrs];
        System.arraycopy(orig.aveScore, 0, aveScore, 0, Nscrs);
        //delete [] maxScore;
        maxScore = new float[Nscrs];
        System.arraycopy(orig.maxScore, 0, maxScore, 0, Nscrs);
        //delete [] minScore;
        minScore = new float[Nscrs];
        System.arraycopy(orig.minScore, 0, minScore, 0, Nscrs);
        //delete [] devScore;
        devScore = new float[Nscrs];
        System.arraycopy(orig.devScore, 0, devScore, 0, Nscrs);
        //delete [] divScore;
        divScore = new float[Nscrs];
        System.arraycopy(orig.divScore, 0, divScore, 0, Nscrs);
        //delete [] scorefile;
        if (orig.scorefile != null) {
            scorefile = orig.scorefile;
        } else {
            scorefile = null;
        }
        which = orig.which;
        boa.delete();
        if (orig.boa != null) {
            boa = (GAPopulation) orig.boa.clone();
        }
    }

    float online() {
        return on;
    }

    float offlineMax() {
        return offmax;
    }

    float offlineMin() {
        return offmin;
    }

    float initial() {
        return initial(Maximum);
    }

    private float initial(final int w) {
        float val = (float) 0.0;
        switch (w) {
            case Mean:
                val = aveInit;
                break;
            case Maximum:
                val = maxInit;
                break;
            case Minimum:
                val = minInit;
                break;
            case Deviation:
                val = devInit;
                break;
            case Diversity:
                val = divInit;
                break;
            default:
                break;
        }
        return val;
    }

    float current() {
        return current(Maximum);
    }

    private float current(final int w) {
        float val = (float) 0.0;
        switch (w) {
            case Mean:
                val = aveCur;
                break;
            case Maximum:
                val = maxCur;
                break;
            case Minimum:
                val = minCur;
                break;
            case Deviation:
                val = devCur;
                break;
            case Diversity:
                val = divCur;
                break;
            default:
                break;
        }
        return val;
    }

    float maxEver() {
        return maxever;
    }

    float minEver() {
        return minever;
    }

    int generation() {
        return curgen;
    }

    int selections() {
        return numsel;
    }

    int crossovers() {
        return numcro;
    }

    int mutations() {
        return nummut;
    }

    int replacements() {
        return numrep;
    }

    int indEvals() {
        return numeval;
    }

    int popEvals() {
        return numpeval;
    }

    float convergence() {
        double cnv = 0.0;
        if (nconv >= Nconv - 1 && cscore[nconv % Nconv] != 0) {
            cnv = (double) cscore[(nconv + 1) % Nconv] / (double) cscore[nconv % Nconv];
        }
        return (float) cnv;
    }

    int nConvergence() {
        return Nconv;
    }

    /**
     * When a new number of gens to conv is specified, keep all the data that we
     * can in the transfer.  Make sure we do it in the right order!  Then just
     * continue on as before.
     * If someone passes us a zero then we set to 1.
     */
    int nConvergence(int n) {
        if (n == 0) {
            n = 1;
        }
        final float[] tmp = cscore;
        cscore = new float[n];
        if (Nconv < n) {
            if (nconv < Nconv) {
                System.arraycopy(tmp, 0, cscore, 0, nconv + 1);
            } else {
                System.arraycopy(tmp, 0, cscore, Nconv - nconv % Nconv - 1, nconv % Nconv + 1);
                System.arraycopy(tmp, nconv % Nconv + 1, cscore, 0, Nconv - nconv % Nconv - 1);
            }
        } else {
            if (nconv < n) {
                System.arraycopy(tmp, 0, cscore, 0, nconv + 1);
            } else {
                if (nconv % Nconv + 1 < n) {
                    System.arraycopy(tmp, 0, cscore, n - nconv % Nconv - 1, nconv % Nconv + 1);
                    System.arraycopy(tmp, Nconv - (1 + n - nconv % Nconv), cscore, 0, 1);
                } else {
                    System.arraycopy(tmp, 1 + nconv % Nconv - n, cscore, 0, n);
                }
            }
        }
        Nconv = n;
        //delete [] tmp;
        return Nconv;
    }

    int nBestGenomes(final GAGenome genome, final int n) throws clCancelException {
        if (n == 0) {
            boa.delete();
            boa = null;
        } else if (boa == null) {
            boa = new GAPopulation(genome, n);
        } else {
            boa.size(n);
        }
        return n;
    }

    int nBestGenomes() {
        return boa != null ? boa.size() : 0;
    }

    int scoreFrequency(final int x) {
        return scoreFreq = x;
    }

    int scoreFrequency() {
        return scoreFreq;
    }

    /**
     * Adjust the scores buffers to match the specified amount.  If someone
     * specifies zero then we don't keep the scores, so set all to NULL.
     */
    int flushFrequency(final int freq) {
        if (freq == 0) {
            if (nscrs > 0) {
                flushScores();
            }
            resizeScores(freq);
        } else if (freq > Nscrs) {
            resizeScores(freq);
        } else if (freq < Nscrs) {
            if (nscrs > freq) {
                flushScores();
            }
            resizeScores(freq);
        }
        Nscrs = freq;
        return freq;
    }

    int flushFrequency() {
        return Nscrs;
    }

    String scoreFilename(final String filename) {
        //delete [] scorefile;
        scorefile = null;
        if (filename != null) {
            scorefile = filename;
        }
        return scorefile;
    }

    String scoreFilename() {
        return scorefile;
    }

    int selectScores(final int w) {
        return which = w;
    }

    int selectScores() {
        return which;
    }

    boolean recordDiversity(final boolean flag) {
        return dodiv = flag;
    }

    boolean recordDiversity() {
        return dodiv;
    }

    void flushScores() {
        if (nscrs == 0) {
            return;
        }
        writeScores();
        /*memset(gen, 0, Nscrs*sizeof(int));
        memset(aveScore, 0, Nscrs*sizeof(float));
        memset(maxScore, 0, Nscrs*sizeof(float));
        memset(minScore, 0, Nscrs*sizeof(float));
        memset(devScore, 0, Nscrs*sizeof(float));
        memset(divScore, 0, Nscrs*sizeof(float));  */
        nscrs = 0;
    }

    /**
     * Use this method to update the statistics to account for the current
     * population.  This routine increments the generation counter and assumes that
     * the population that gets passed is the current population.
     * If we are supposed to flush the scores, then we dump them to the specified
     * file.  If no flushing frequency has been specified then we don't record.
     */
    void update(final GAPopulation pop) throws clCancelException {
        ++curgen;     // must do this first so no divide-by-zero
        if (scoreFreq > 0 && curgen % scoreFreq == 0) {
            setScore(pop);
        }
        if (Nscrs > 0 && nscrs >= Nscrs) {
            flushScores();
        }
        maxever = pop.max() > maxever ? pop.max() : maxever;
        minever = pop.min() < minever ? pop.min() : minever;
        float tmpval;
        tmpval = (on * (curgen - 1) + pop.ave()) / curgen;
        on = tmpval;
        tmpval = (offmax * (curgen - 1) + pop.max()) / curgen;
        offmax = tmpval;
        tmpval = (offmin * (curgen - 1) + pop.min()) / curgen;
        offmin = tmpval;
        setConvergence(pop.order() == GAPopulation.SortOrder.HIGH_IS_BEST ? pop.max() : pop.min());
        updateBestIndividual(pop);
        numpeval = pop.nevals();
    }


    /**
     * Reset the GA's statistics based on the population.  To do this right you
     * should initialize the population before you pass it to this routine.  If you
     * don't, the stats will be based on a non-initialized population.
     */
    void reset(final GAPopulation pop) throws clCancelException {
        curgen = 0;
        numsel = numcro = nummut = numrep = numeval = numpeval = 0;
        /*memset(gen, 0, Nscrs*sizeof(int));
        memset(aveScore, 0, Nscrs*sizeof(float));
        memset(maxScore, 0, Nscrs*sizeof(float));
        memset(minScore, 0, Nscrs*sizeof(float));
        memset(devScore, 0, Nscrs*sizeof(float));
        memset(divScore, 0, Nscrs*sizeof(float)); */
        nscrs = 0;
        setScore(pop);
        if (Nscrs > 0) {
            flushScores();
        }

        //memset(cscore, 0, Nconv*sizeof(float));
        nconv = 0;      // should set to -1 then call setConv
        cscore[0] =
        pop.order() == GAPopulation.SortOrder.HIGH_IS_BEST ? pop.max() : pop.min();

        updateBestIndividual(pop, true);
        aveCur = aveInit = pop.ave();
        maxCur = maxInit = maxever = pop.max();
        minCur = minInit = minever = pop.min();
        devCur = devInit = pop.dev();
        divCur = divInit = dodiv ? pop.div() : (float) -1.0;

        on = pop.ave();
        offmax = pop.max();
        offmin = pop.min();
        numpeval = pop.nevals();
        for (int i = 0; i < pop.size(); i++) {
            numeval += pop.individual(i).nevals();
        }
    }

    GAPopulation bestPopulation() {
        return boa;
    }

    public GAGenome bestIndividual() throws clCancelException {
        return bestIndividual(0);
    }

    private GAGenome bestIndividual(int n) throws clCancelException {
        if (boa == null || (int) n >= boa.size()) {
            GAError.GAErr("GAStatistics", "bestIndividual", GAError.gaErrBadPopIndex);
            n = 0;
        }
        return boa.best(n);       // this will crash if no boa
    }
}
