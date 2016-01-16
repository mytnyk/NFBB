package ga;

import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 2, 2004
 * Time: 3:34:53 PM
 * Description:
 * ----------------------------------------------------------------------------
 * The base GA class is abstract - it defines the core data elements and parts
 * of the interface that are common to all genetic algorithms (as defined in
 * GAlib, that is).
 * <p/>
 * initialize
 * Undefined for the base class.  The initialization routine typically calls
 * the population initializer (which typically calls the genome initializers).
 * It should also reset the statistics.
 * <p/>
 * step
 * Evolve by one generation.  'generation' can be defined different ways for
 * different genetic algorithms, but in the traditional formulation a generation
 * mean creation of a new population (or portion thereof).
 * <p/>
 * done
 * Calls the completion measure routine to tell whether or not the GA is done.
 * <p/>
 * evolve
 * This method is provided as a convenience so that you don't have to increment
 * the GA generation-by-generation by hand.  If you do decide to do it by hand,
 * be sure that you initialize before you start evolving!
 * ----------------------------------------------------------------------------
 */

abstract class GAGeneticAlgorithm implements GAID {
    static final int MINIMIZE = -1;
    static final int MAXIMIZE = 1;

    GAStatistics stats;
    GAParameterList params;
    GAPopulation pop;
    private Terminator cf;  // function for determining done-ness
    private Object ud;      // pointer to user data structure

    private int ngen;
    private int nconv;
    private float pconv;
    private float pcross;
    private float pmut;
    int minmax;
    SexualCrossover scross;   // sexual crossover to use
    private AsexualCrossover across;  // asexual crossover to use

    // Here we assign the default values of the GAlib default parameters.
    private static final int gaDefNumGen = 250;
    private static final float gaDefPConv = (float) 0.99;
    private static final int gaDefNConv = 20;
    private static final float gaDefPMut = (float) 0.01;
    private static final float gaDefPCross = (float) 0.90;
    private static final int gaDefPopSize = 50;
    static final int gaDefNPop = 10;
    static final float gaDefPRepl = (float) 0.5;
    static final int gaDefNRepl = 10;
    static final int gaDefNumOff = 2;
    static final float gaDefPMig = (float) 0.1;
    static final int gaDefNMig = 5;
    private static final int gaDefSelectScores = GAStatistics.Maximum;
    private static final int gaDefMiniMaxi = 1;
    private static final boolean gaDefDivFlag = false;
    static final boolean gaDefElitism = true;

    public final boolean sameClass(final GAID b) {
        return classID() == b.classID();
    }

    public String className() {
        return "GAIncrementalGA";
    }

    public int classID() {
        return BaseGA;
    }

    static GAParameterList registerDefaultParameters(final GAParameterList p) {
        p.add(GADefs.gaNminimaxi, GADefs.gaSNminimaxi, GAParameter.INT, new TObjInt(gaDefMiniMaxi));
        p.add(GADefs.gaNnGenerations, GADefs.gaSNnGenerations, GAParameter.INT, new TObjInt(gaDefNumGen));
        p.add(GADefs.gaNnConvergence, GADefs.gaSNnConvergence, GAParameter.INT, new TObjInt(gaDefNConv));
        p.add(GADefs.gaNpConvergence, GADefs.gaSNpConvergence, GAParameter.FLOAT, new TObjFloat(gaDefPConv));
        p.add(GADefs.gaNpCrossover, GADefs.gaSNpCrossover, GAParameter.FLOAT, new TObjFloat(gaDefPCross));
        p.add(GADefs.gaNpMutation, GADefs.gaSNpMutation, GAParameter.FLOAT, new TObjFloat(gaDefPMut));
        p.add(GADefs.gaNpopulationSize, GADefs.gaSNpopulationSize, GAParameter.INT, new TObjInt(gaDefPopSize));
        p.add(GADefs.gaNnBestGenomes, GADefs.gaSNnBestGenomes, GAParameter.INT,
              new TObjInt(GAStatistics.gaDefNumBestGenomes));
        p.add(GADefs.gaNscoreFrequency, GADefs.gaSNscoreFrequency, GAParameter.INT,
              new TObjInt(GAStatistics.gaDefScoreFrequency1));
        p.add(GADefs.gaNflushFrequency, GADefs.gaSNflushFrequency, GAParameter.INT,
              new TObjInt(GAStatistics.gaDefFlushFrequency));
        p.add(GADefs.gaNrecordDiversity, GADefs.gaSNrecordDiversity, GAParameter.BOOLEAN, new TObjBool(gaDefDivFlag));
        p.add(GADefs.gaNscoreFilename, GADefs.gaSNscoreFilename, GAParameter.STRING, GAStatistics.gaDefScoreFilename);
        p.add(GADefs.gaNselectScores, GADefs.gaSNselectScores, GAParameter.INT, new TObjInt(gaDefSelectScores));
        return p;
    }

    /**
     * When we create a GA, we stuff the parameters with the basics that will be
     * needed by most genetic algorithms - num generations, p convergence, etc.
     */
    GAGeneticAlgorithm(final GAGenome g) throws clCancelException {
        stats = new GAStatistics();
        params = new GAParameterList();
        pop = new GAPopulation(g, gaDefPopSize);
        pop.geneticAlgorithm(this);

        ud = null;
        cf = new TerminateUponGeneration();

        minmax = gaDefMiniMaxi;
        params.add(GADefs.gaNminimaxi, GADefs.gaSNminimaxi, GAParameter.INT, new TObjInt(minmax));
        ngen = gaDefNumGen;
        params.add(GADefs.gaNnGenerations, GADefs.gaSNnGenerations, GAParameter.INT, new TObjInt(ngen));
        nconv = gaDefNConv;
        stats.nConvergence(nconv);
        params.add(GADefs.gaNnConvergence, GADefs.gaSNnConvergence, GAParameter.INT, new TObjInt(nconv));
        pconv = gaDefPConv;
        params.add(GADefs.gaNpConvergence, GADefs.gaSNpConvergence, GAParameter.FLOAT, new TObjFloat(pconv));
        pcross = gaDefPCross;
        params.add(GADefs.gaNpCrossover, GADefs.gaSNpCrossover, GAParameter.FLOAT, new TObjFloat(pcross));
        pmut = gaDefPMut;
        params.add(GADefs.gaNpMutation, GADefs.gaSNpMutation, GAParameter.FLOAT, new TObjFloat(pmut));
        final int psize = pop.size();
        params.add(GADefs.gaNpopulationSize, GADefs.gaSNpopulationSize, GAParameter.INT, new TObjInt(psize));

        stats.scoreFrequency(GAStatistics.gaDefScoreFrequency1);
        params.add(GADefs.gaNscoreFrequency, GADefs.gaSNscoreFrequency,
                   GAParameter.INT, new TObjInt(GAStatistics.gaDefScoreFrequency1));
        stats.flushFrequency(GAStatistics.gaDefFlushFrequency);
        params.add(GADefs.gaNflushFrequency, GADefs.gaSNflushFrequency,
                   GAParameter.INT, new TObjInt(GAStatistics.gaDefFlushFrequency));
        stats.recordDiversity(gaDefDivFlag);
        params.add(GADefs.gaNrecordDiversity, GADefs.gaSNrecordDiversity,
                   GAParameter.BOOLEAN, new TObjBool(gaDefDivFlag));
        stats.scoreFilename(GAStatistics.gaDefScoreFilename);
        params.add(GADefs.gaNscoreFilename, GADefs.gaSNscoreFilename,
                   GAParameter.STRING, GAStatistics.gaDefScoreFilename);
        stats.selectScores(gaDefSelectScores);
        params.add(GADefs.gaNselectScores, GADefs.gaSNselectScores,
                   GAParameter.INT, new TObjInt(gaDefSelectScores));
        stats.nBestGenomes(g, GAStatistics.gaDefNumBestGenomes);
        params.add(GADefs.gaNnBestGenomes, GADefs.gaSNnBestGenomes,
                   GAParameter.INT, new TObjInt(GAStatistics.gaDefNumBestGenomes));
        scross = g.sexual();
        across = g.asexual();
    }

    GAGeneticAlgorithm(final GAPopulation p) throws clCancelException {
        stats = new GAStatistics();
        params = new GAParameterList();
        pop = new GAPopulation(p);
        pop.geneticAlgorithm(this);

        ud = null;
        cf = new TerminateUponGeneration();

        minmax = gaDefMiniMaxi;
        params.add(GADefs.gaNminimaxi, GADefs.gaSNminimaxi, GAParameter.INT, new TObjInt(minmax));
        ngen = gaDefNumGen;
        params.add(GADefs.gaNnGenerations, GADefs.gaSNnGenerations, GAParameter.INT, new TObjInt(ngen));
        nconv = gaDefNConv;
        stats.nConvergence(nconv);
        params.add(GADefs.gaNnConvergence, GADefs.gaSNnConvergence, GAParameter.INT, new TObjInt(nconv));
        pconv = gaDefPConv;
        params.add(GADefs.gaNpConvergence, GADefs.gaSNpConvergence, GAParameter.FLOAT, new TObjFloat(pconv));
        pcross = gaDefPCross;
        params.add(GADefs.gaNpCrossover, GADefs.gaSNpCrossover, GAParameter.FLOAT, new TObjFloat(pcross));
        pmut = gaDefPMut;
        params.add(GADefs.gaNpMutation, GADefs.gaSNpMutation, GAParameter.FLOAT, new TObjFloat(pmut));
        final int psize = pop.size();
        params.add(GADefs.gaNpopulationSize, GADefs.gaSNpopulationSize, GAParameter.INT, new TObjInt(psize));

        stats.scoreFrequency(GAStatistics.gaDefScoreFrequency1);
        params.add(GADefs.gaNscoreFrequency, GADefs.gaSNscoreFrequency,
                   GAParameter.INT, new TObjInt(GAStatistics.gaDefScoreFrequency1));
        stats.flushFrequency(GAStatistics.gaDefFlushFrequency);
        params.add(GADefs.gaNflushFrequency, GADefs.gaSNflushFrequency,
                   GAParameter.INT, new TObjInt(GAStatistics.gaDefFlushFrequency));
        stats.recordDiversity(gaDefDivFlag);
        params.add(GADefs.gaNrecordDiversity, GADefs.gaSNrecordDiversity,
                   GAParameter.BOOLEAN, new TObjBool(gaDefDivFlag));
        stats.scoreFilename(GAStatistics.gaDefScoreFilename);
        params.add(GADefs.gaNscoreFilename, GADefs.gaSNscoreFilename,
                   GAParameter.STRING, GAStatistics.gaDefScoreFilename);
        stats.selectScores(gaDefSelectScores);
        params.add(GADefs.gaNselectScores, GADefs.gaSNselectScores,
                   GAParameter.INT, new TObjInt(gaDefSelectScores));
        stats.nBestGenomes(p.individual(0), GAStatistics.gaDefNumBestGenomes);
        params.add(GADefs.gaNnBestGenomes, GADefs.gaSNnBestGenomes,
                   GAParameter.INT, new TObjInt(GAStatistics.gaDefNumBestGenomes));
        scross = p.individual(0).sexual();
        across = p.individual(0).asexual();
    }

    GAGeneticAlgorithm(final GAGeneticAlgorithm ga) {
        stats = new GAStatistics(ga.stats);
        params = new GAParameterList(ga.params);
        pop = (GAPopulation) ga.pop.clone();
        pop.geneticAlgorithm(this);

        cf = ga.cf;
        ud = ga.ud;
        ngen = ga.ngen;
        nconv = ga.nconv;
        pconv = ga.pconv;
        pcross = ga.pcross;
        pmut = ga.pmut;
        minmax = ga.minmax;
        scross = ga.scross;
        across = ga.across;
    }

    void delete() {
        pop.delete();
    }

    void copy(final GAGeneticAlgorithm ga) throws clCancelException {
        if (pop != null) {
            pop.copy(ga.pop);
        } else {
            pop = (GAPopulation) ga.pop.clone();
        }
        pop.geneticAlgorithm(this);

        stats = ga.stats;
        params = ga.params;

        cf = ga.cf;
        ud = ga.ud;
        ngen = ga.ngen;
        nconv = ga.nconv;
        pconv = ga.pconv;
        pcross = ga.pcross;
        pmut = ga.pmut;
        minmax = ga.minmax;
        scross = ga.scross;
        across = ga.across;
    }

    private boolean done() {
        return cf.terminator(this);
    }

    abstract void initialize(int seed) throws clCancelException;

    abstract void step() throws clCancelException;

    public final void evolve() throws clCancelException {
        evolve(0);
    }

    public final void evolve(final int seed) throws clCancelException {
        initialize(seed);
        while (!done()) {
            step();
        }
        if (stats.flushFrequency() > 0) {
            stats.flushScores();
        }
    }

    final Object userData() {
        return ud;
    }

    final Object userData(final Object d) {
        return ud = d;
    }

    final Terminator terminator() {
        return cf;
    }

    final Terminator terminator(final Terminator f) {
        return cf = f;
    }

    final GAParameterList parameters() {
        return params;
    }

    public final GAParameterList parameters(final GAParameterList list) throws clCancelException {
        for (int i = 0; i < list.size(); i++) {
            setptr(list.getAt(i).fullname(), list.getAt(i).value());
        }
        return params;
    }

    final GAParameterList parameters(final int argc, final String[] argv) throws clCancelException {
        return parameters(argc, argv, false);
    }

    private GAParameterList parameters(final int argc, final String[] argv, final boolean flag)
            throws clCancelException {
        params.parse(argc, argv, flag);   // get the args we understand
        for (int i = 0; i < params.size(); i++) {
            setptr(params.getAt(i).fullname(), params.getAt(i).value());
        }
        return params;
    }

    int get(final String name, Object value) {
        int status = 1;

        if (name == GADefs.gaNnBestGenomes ||
            name == GADefs.gaSNnBestGenomes) {
            value = new TObjInt(stats.nBestGenomes());
            status = 0;
        } else if (name == GADefs.gaNpopulationSize ||
                   name == GADefs.gaSNpopulationSize) {
            value = new TObjInt(pop.size());
            status = 0;
        } else if (name == GADefs.gaNminimaxi ||
                   name == GADefs.gaSNminimaxi) {
            value = new TObjInt(minmax);
            status = 0;
        } else if (name == GADefs.gaNnGenerations ||
                   name == GADefs.gaSNnGenerations) {
            value = new TObjInt(ngen);
            status = 0;
        } else if (name == GADefs.gaNpConvergence ||
                   name == GADefs.gaSNpConvergence) {
            value = new TObjFloat(pconv);
            status = 0;
        } else if (name == GADefs.gaNnConvergence ||
                   name == GADefs.gaSNnConvergence) {
            value = new TObjInt(nconv);
            status = 0;
        } else if (name == GADefs.gaNpCrossover ||
                   name == GADefs.gaSNpCrossover) {
            value = new TObjFloat(pcross);
            status = 0;
        } else if (name == GADefs.gaNpMutation ||
                   name == GADefs.gaSNpMutation) {
            value = new TObjFloat(pmut);
            status = 0;
        } else if (name == GADefs.gaNscoreFrequency ||
                   name == GADefs.gaSNscoreFrequency) {
            value = new TObjInt(stats.scoreFrequency());
            status = 0;
        } else if (name == GADefs.gaNflushFrequency ||
                   name == GADefs.gaSNflushFrequency) {
            value = new TObjInt(stats.flushFrequency());
            status = 0;
        } else if (name == GADefs.gaNrecordDiversity ||
                   name == GADefs.gaSNrecordDiversity) {
            value = new TObjBool(stats.recordDiversity());
            status = 0;
        } else if (name == GADefs.gaNselectScores ||
                   name == GADefs.gaSNselectScores) {
            value = new TObjInt(stats.selectScores());
            status = 0;
        } else if (name == GADefs.gaNscoreFilename ||
                   name == GADefs.gaSNscoreFilename) {
            value = stats.scoreFilename();
            status = 0;
        }
        return status;
    }

    /**
     * Return 0 if everything is OK, non-zero if error.  If we did not set anything
     * then we return non-zero (this is not an error, but we indicate that we did
     * not do anything).
     * The set method must set both the GA's parameter and the value in the
     * parameter list (kind of stupid to maintain two copies of the same data, but
     * oh well).  The call to set on params is redundant for the times when this
     * method is called *after* the parameter list has been updated, but it is
     * necessary when this method is called directly by the user.
     */
    int setptr(final String name, final Object value) throws clCancelException {
        int status = 1;

        params.set(name, value);    // redundant for some cases, but not others

        if (name == GADefs.gaNnBestGenomes || name == GADefs.gaSNnBestGenomes) {
            nBestGenomes(((TObjInt) value).getValue());
            status = 0;
        } else if (name == GADefs.gaNpopulationSize || name == GADefs.gaSNpopulationSize) {
            populationSize(((TObjInt) value).getValue());
            status = 0;
        } else if (name == GADefs.gaNminimaxi || name == GADefs.gaSNminimaxi) {
            minimaxi(((TObjInt) value).getValue());
            status = 0;
        } else if (name == GADefs.gaNnGenerations || name == GADefs.gaSNnGenerations) {
            nGenerations(((TObjInt) value).getValue());
            status = 0;
        } else if (name == GADefs.gaNpConvergence || name == GADefs.gaSNpConvergence) {
            pConvergence(((TObjFloat) value).getValue());
            status = 0;
        } else if (name == GADefs.gaNnConvergence || name == GADefs.gaSNnConvergence) {
            nConvergence(((TObjInt) value).getValue());
            status = 0;
        } else if (name == GADefs.gaNpCrossover || name == GADefs.gaSNpCrossover) {
            pCrossover(((TObjFloat) value).getValue());
            status = 0;
        } else if (name == GADefs.gaNpMutation || name == GADefs.gaSNpMutation) {
            pMutation(((TObjFloat) value).getValue());
            status = 0;
        } else if (name == GADefs.gaNscoreFrequency || name == GADefs.gaSNscoreFrequency) {
            stats.scoreFrequency(((TObjInt) value).getValue());
            status = 0;
        } else if (name == GADefs.gaNflushFrequency || name == GADefs.gaSNflushFrequency) {
            stats.flushFrequency(((TObjInt) value).getValue());
            status = 0;
        } else if (name == GADefs.gaNrecordDiversity || name == GADefs.gaSNrecordDiversity) {
            stats.recordDiversity(((TObjBool) value).getValue());
            status = 0;
        } else if (name == GADefs.gaNselectScores || name == GADefs.gaSNselectScores) {
            stats.selectScores(((TObjInt) value).getValue());
            status = 0;
        } else if (name == GADefs.gaNscoreFilename || name == GADefs.gaSNscoreFilename) {
            final String tmpname = (String) value;
            stats.scoreFilename(tmpname);
            status = 0;
        }
        return status;
    }

    final int set(final String s, final int v) throws clCancelException {
        return setptr(s, new TObjInt(v));
    }

    final int set(final String s, final String v) throws clCancelException {
        return setptr(s, v);
    }

    final int set(final String s, final Object v) throws clCancelException {
        return setptr(s, v);
    }

    /**
     * This is a pretty ugly little hack to make doubles/floats work transparently.
     */
    final int set(final String name, final double v) throws clCancelException {
        int status = 1;
        for (int i = 0; i < params.size(); i++) {
            if (name == params.getAt(i).fullname() ||
                name == params.getAt(i).shrtname()) {
                if (params.getAt(i).type() == GAParameter.FLOAT) {
                    final float fval = (float) v;
                    status = setptr(name, new TObjFloat(fval));
                } else {
                    status = setptr(name, new TObjDouble(v));
                }
            }
        }
        return status;
    }

    int minimaxi() {
        return minmax;
    }

    int minimaxi(final int m) {
        if (m == MINIMIZE) {
            pop.order(GAPopulation.SortOrder.LOW_IS_BEST);
        } else {
            pop.order(GAPopulation.SortOrder.HIGH_IS_BEST);
        }
        params.set(GADefs.gaNminimaxi, m);
        minmax = m == MINIMIZE ? MINIMIZE : MAXIMIZE;
        return minmax;
    }

    final int minimize() {
        return minimaxi(MINIMIZE);
    }

    final int maximize() {
        return minimaxi(MAXIMIZE);
    }

    private int nGenerations() {
        return ngen;
    }

    public final int nGenerations(final int n) {
        params.set(GADefs.gaNnGenerations, n);
        return ngen = n;
    }

    final int nConvergence() {
        return nconv;
    }

    private int nConvergence(final int n) {
        params.set(GADefs.gaNnConvergence, n);
        return nconv = stats.nConvergence(n);
    }

    final float pConvergence() {
        return pconv;
    }

    private float pConvergence(final float p) {
        params.set(GADefs.gaNpConvergence, p);
        return pconv = p;
    }

    final float pCrossover() {
        return pcross;
    }

    public final float pCrossover(final float p) {
        params.set(GADefs.gaNpCrossover, p);
        return pcross = p;
    }

    final float pMutation() {
        return pmut;
    }

    public final float pMutation(final float p) {
        params.set(GADefs.gaNpMutation, p);
        return pmut = p;
    }

    public final SexualCrossover crossover(final SexualCrossover f) {
        return scross = f;
    }

    final SexualCrossover sexual() {
        return scross;
    }

    final AsexualCrossover crossover(final AsexualCrossover f) {
        return across = f;
    }

    final AsexualCrossover asexual() {
        return across;
    }

    public final GAStatistics statistics() {
        return stats;
    }

    final float convergence() {
        return stats.convergence();
    }

    private int generation() {
        return stats.generation();
    }

    final void flushScores() {
        if (stats.flushFrequency() > 0) {
            stats.flushScores();
        }
    }

    final int scoreFrequency() {
        return stats.scoreFrequency();
    }

    public final int scoreFrequency(final int x) {
        params.set(GADefs.gaNscoreFrequency, x);
        return stats.scoreFrequency(x);
    }

    final int flushFrequency() {
        return stats.flushFrequency();
    }

    public final int flushFrequency(final int x) {
        params.set(GADefs.gaNflushFrequency, x);
        return stats.flushFrequency(x);
    }

    public final String scoreFilename() {
        return stats.scoreFilename();
    }

    public final String scoreFilename(final String fn) {
        params.set(GADefs.gaNscoreFilename, fn);
        return stats.scoreFilename(fn);
    }

    final int selectScores() {
        return stats.selectScores();
    }

    final int selectScores(final int w) {
        params.set(GADefs.gaNselectScores, w);
        return stats.selectScores(w);
    }

    final boolean recordDiversity() {
        return stats.recordDiversity();
    }

    final boolean recordDiversity(final boolean f) {
        params.set(GADefs.gaNrecordDiversity, new TObjBool(f));
        return stats.recordDiversity(f);
    }

    GAPopulation population() {
        return pop;
    }

    GAPopulation population(final GAPopulation p) throws clCancelException {
        if (p.size() < 1) {
            GAError.GAErr(className(), "population", GAError.gaErrNoIndividuals);
            return pop;
        }
        pop.copy(p);
        pop.geneticAlgorithm(this);
        return pop;
    }

    int populationSize() {
        return pop.size();
    }

    int populationSize(final int value) throws clCancelException {
        final int ps = value;
        params.set(GADefs.gaNpopulationSize, value);
        return pop.size(ps);
    }

    final int nBestGenomes() {
        return stats.nBestGenomes();
    }

    private int nBestGenomes(final int n) throws clCancelException {
        params.set(GADefs.gaNnBestGenomes, n);
        return stats.nBestGenomes(pop.individual(0), n);
    }

    GAScalingScheme scaling() {
        return pop.scaling();
    }

    GAScalingScheme scaling(final GAScalingScheme s) {
        return pop.scaling(s);
    }

    GASelectionScheme selector() {
        return pop.selector();
    }

    GASelectionScheme selector(final GASelectionScheme s) {
        return pop.selector(s);
    }

    void objectiveFunction(final Evaluator f) {
        for (int i = 0; i < pop.size(); i++) {
            pop.individual(i).evaluator(f);
        }
    }

    void objectiveData(final GAEvalData v) {
        for (int i = 0; i < pop.size(); i++) {
            pop.individual(i).evalData(v);
        }
    }

    /**
     * Here are a few termination functions that you can use.  Terminators return
     * true if the algorithm should finish, false otherwise.
     */
    static final class TerminateUponGeneration implements Terminator {
        public boolean terminator(final GAGeneticAlgorithm ga) {
            return ga.generation() < ga.nGenerations() ? false : true;
        }
    }
    /*
    static final class TerminateUponConvergence implements Terminator{

    }

    static final class TerminateUponPopConvergence implements Terminator{

    }*/
}