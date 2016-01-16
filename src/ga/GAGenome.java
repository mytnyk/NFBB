package ga;

import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 2, 2004
 * Time: 3:07:12 PM
 * Description:
 * The base genome class just defines the genome interface - how to mutate,
 * crossover, evaluate, etc.  When you create your own genome, multiply inherit
 * from the base genome class and the data type that you want to use.  Use the
 * data type to store the information and use the genome part to tell the GA how
 * it should operate on the data.  See comments below for further details.
 */

/**
 * ----------------------------------------------------------------------------
 * Genome
 * -------------------------------------------------------------------------------
 * <p/>
 * Deriving your own genomes:
 * For any derived class be sure to define the canonical methods:  constructor,
 * copy constructor, operator= (assign), and destructor(delete).  Make sure that you check for a
 * self-copy in your copy method (it is possible that a genome will be
 * selected to cross with itself, and self-copying is not out of the question)
 * To work properly with the GAlib, you MUST define the following:
 * <p/>
 * YourGenome( -default-args-for-your-genome )
 * YourGenome(final YourGenome)
 * void delete()
 * GAGenome clone(GAGenome::CloneMethod)
 * copy(final GAGenome)
 * <p/>
 * If your genome class defines any new properties you should to define:
 * <p/>
 * int read(.)
 * int write(.)
 * int equal(final GAGenome)
 * <p/>
 * When you derive a genome, don't forget to use the _evaluated flag to
 * indicate when the state of the genome has changed and an evaluation is
 * needed.
 * Assign a default crossover method so that users don't have to assign one
 * unless they want to.  Do this in the constructor.
 * It is a good idea to define an identity for your genome (especially if
 * you will be using it in an environment with multiple genome types running
 * around).  Use the DefineIdentity/DeclareIdentity macros (defined in id.h)
 * to do this in your class definition.
 * <p/>
 * Brief overview of the member functions:
 * <p/>
 * initialize
 * Use this method to set the initial state of your genomes once they have
 * been created.  This initialization is for setting up the genome's state,
 * not for doing the basic mechanics of genome class management.  The
 * default behaviour of this method is to change randomly the contents of the
 * genome.  If you want to bias your initial population, this is where to
 * make that happen.
 * The initializer is used to initialize the genome (duh).  Notice that the
 * state of the genome is unknown - memory may or may not have been allocated,
 * and the genome may or may not have been used before.  So your initializer
 * should first clean up as needed, then do its thing.  The initializer may be
 * called any number of times (unlike a class constructor which is called only
 * once for a given instance).
 * <p/>
 * mutate
 * Mutate the genome with probability as specified.  What mutation means
 * depends upon the data type of the genome.  For example, you could have
 * a bit string in which 50% mutation means each bit has a 50% chance of
 * getting flipped, or you could have a tree in which 50% mutation means each
 * node has a 50% chance of getting deleted, or you could have a bit string
 * in which 50% mutation means 50% of the bits ACTUALLY get flipped.
 * The mutations member returns the number of mutations since the genome
 * was initialized.
 * The mutator makes a change to the genome with likeliehood determined by the
 * mutation rate parameter.  The exact meaning of mutation is up to you, as is
 * the specific meaning of the mutation rate.  The function returns the number
 * of mutations that actually occurred.
 * <p/>
 * crossover
 * Genomes don't really have any clue about other genomes, so we don't make
 * the crossover a member function.  Instead, each genome kind of knows how
 * to mate with other genomes to generate offspring, but they are not
 * capable of doing it themselves.  The crossover member function is used to
 * set the default mating mode for the genomes - it does not actually perform
 * the crossover.  This way the GA can use asexual crossover if it wants to
 * (but genomes only know how to do the default sexual crossover).
 * This also lets you do funky stuff like crossover between different data
 * types and group sex to generate new offspring.
 * We define two types of crossover:  sexual and asexual.  Most GAlib
 * algorithms use the sexual crossover, but both are available.  Each genome
 * knows the preferred crossover method, but none is capable of reproducing.
 * The genetic algorithm must actually perform the mating because it involves
 * another genome (as parent and/or child).
 * <p/>
 * evaluator
 * Set the genome's objective function.  This also sets marks the evaluated
 * flag to indicate that the genome must be re-evaluated.
 * Evaluation happens on-demand - the objective score is not calculated until
 * it is requested.  Then it is cached so that it does not need to be re-
 * calculated each time it is requested.  This means that any member function
 * that modifies the state of the genome must also set the evaluated flag to
 * indicate that the score must be recalculated.
 * The genome objective function is used by the GA to evaluate each member of
 * the population.
 * <p/>
 * comparator
 * This method is used to determine how similar two genomes are.  If you want
 * to use a different comparison method without deriving a new class, then use
 * the comparator function to do so.  For example, you may want to do phenotype-
 * based comparisons rather than genotype-based comparisons.
 * In many cases we have to compare two genomes to determine how similar or
 * different they are.  In traditional GA literature this type of function is
 * referred to as a 'distance' function, probably because bit strings can be
 * compared using the Hamming distance as a measure of similarity.  In GAlib, we
 * define a genome comparator function that does exactly this kind of
 * comparison.
 * If the genomes are identical, the similarity function should return a
 * value of 0.0, if completely different then return a value greater than 0.
 * The specific definition of what "the same" and what "different" mean is up
 * to you.  Most of the default comparators use the genotype for the comparison,
 * but you can use the phenotype if you prefer.  There is no upper limit to the
 * distance score as far as GAlib is concerned.
 * The no-op function returns a -1 to signify that the comparison failed.
 * <p/>
 * evalData
 * The evalData member is useful if you do not want to derive a new genome class
 * but want to store data with each genome.  When you clone a genome, the eval
 * data also gets cloned so that each genome has its own eval data (unlike the
 * user data pointer described next which is shared by all genomes).
 * <p/>
 * userData
 * The userData member is used to provide all genomes access to the same user
 * data.  This can be a pointer to anything you want.  Any genome cloned from
 * another will share the same userData as the original.  This means that all
 * of the genomes in a population, for example, share the same userData.
 * <p/>
 * score
 * Evaluate the 'performance' of the genome using the objective function.
 * The score is kept in the 'score' member.  The 'evaluated' member tells us
 * whether or not we can trust the score.  Be sure to set/unset the 'evaluated'
 * member as appropriate (eg cross and mutate change the contents of the
 * genome so they unset the 'evaluated' flag).
 * If there is no objective function, then simply return the score.  This
 * allows us to use population-based evaluation methods (where the population
 * method sets the score of each genome).
 * <p/>
 * clone
 * This method allocates space for a new genome and copies the original into
 * the new space.  Depending on the argument, it either copies the entire
 * original or just parts of the original.  For some data types, clone contents
 * and clone attributes will do the same thing.  If your data type requires
 * significant overhead for initialization, then you'll probably want to
 * distinguish between cloning contents and cloning attributes.
 * clone(cont)
 * Clone the contents of the genome.  Returns a pointer to a GAGenome
 * (which actually points to a genome of the type that was cloned).  This is
 * a 'deep copy' in which every part of the genome is duplicated.
 * clone(attr)
 * Clone the attributes of the genome.  This method does nothing to the
 * contents of the genome.  It does NOT call the initialization method.  For
 * some data types this is the same thing as cloning the contents.
 * ----------------------------------------------------------------------------
 */

public class GAGenome implements GAID, Initializer, Mutator, Comparator {
    private float _score;         // value returned by the objective function
    private float _fitness;       // (possibly scaled) fitness score
    boolean _evaluated;           // has this genome been evaluated?
    private int _neval;           // how many evaluations since initialization?
    private GAGeneticAlgorithm ga;// the ga that is using this genome
    private Object ud;            // pointer to user data
    private Evaluator eval;       // objective function
    private GAEvalData evd;       // evaluation data (specific to each genome)
    private Mutator mutr;         // the mutation operator to use for mutations
    private Initializer init;     // how to initialize this genome
    private Comparator cmp;       // how to compare two genomes of this type

    private SexualCrossover sexcross;   // preferred sexual mating method
    private AsexualCrossover asexcross; // preferred asexual mating method

    public final boolean sameClass(final GAID b) {
        return classID() == b.classID();
    }

    public String className() {
        return "GAGenome";
    }

    public int classID() {
        return Genome;
    }

    // no-initialization initializer
    public final void initializer(final GAGenome c) {
        GAError.GAErr(c.className(), "initializer", GAError.gaErrOpUndef);
    }

    public final int mutator(final GAGenome c, final float f) {
        GAError.GAErr(c.className(), "mutator", GAError.gaErrOpUndef);
        return 0;
    }

    public final float comparator(final GAGenome c, final GAGenome c2) {
        GAError.GAErr(c.className(), "comparator", GAError.gaErrOpUndef);
        return (float) -1.0;
    }

    interface Dimension {
        int LENGTH = 0;
        int WIDTH = 0;
        int HEIGHT = 1;
        int DEPTH = 2;
    }

    interface CloneMethod {
        int CONTENTS = 0;
        int ATTRIBUTES = 1;
    }

    interface Size {
        int FIXED_SIZE = -1;
        int ANY_SIZE = -10;
    }

    GAGenome(Initializer i, Mutator m, Comparator c) {
        if (i == null) {
            i = this;
        }
        if (m == null) {
            m = this;
        }
        if (c == null) {
            c = this;
        }
        _score = _fitness = (float) 0.0;
        _evaluated = false;
        _neval = 0;
        ga = null;
        ud = null;
        eval = null;
        evd = null;
        init = i;
        mutr = m;
        cmp = c;
        sexcross = null;
        asexcross = null;
    }

    GAGenome(final GAGenome orig) {
        evd = null;
        _neval = 0;
        copy(orig);
    }

    final GAGenome assing(final GAGenome arg) {
        copy(arg);
        return this;
    }

    void delete() {
        evd.delete();
    }

    int size() {
        return 0;
    }

    public Object clone() {
        return null;
    }

    Object clone(final int flag) {
        GAError.GAErr(className(), "clone", GAError.gaErrOpUndef);
        return new GAGenome(this);
    }

    void copy(final GAGenome orig) {
        if (orig == this) {
            return;
        }
        _score = orig._score;
        _fitness = orig._fitness;
        _evaluated = orig._evaluated;
        ga = orig.ga;
        ud = orig.ud;
        eval = orig.eval;
        init = orig.init;
        mutr = orig.mutr;
        cmp = orig.cmp;
        sexcross = orig.sexcross;
        asexcross = orig.asexcross;
        _neval = 0;

        if (orig.evd != null) {
            if (evd != null) {
                evd.copy(orig.evd);
            } else {
                evd = (GAEvalData) orig.evd.clone();
            }
        } // don't delete if c doesn't have one
    }

    boolean equal(final GAGenome g) {
        GAError.GAErr(className(), "equal", GAError.gaErrOpUndef);
        return true;
    }

    boolean notequal(final GAGenome g) {
        return !equal(g);
    }

    final int nevals() {
        return _neval;
    }

    public final float score() throws clCancelException {
        evaluate();
        return _score;
    }

    final float score(final float s) {
        _evaluated = true;
        return _score = s;
    }

    final float fitness() {
        return _fitness;
    }

    final float fitness(final float f) {
        return _fitness = f;
    }

    final GAGeneticAlgorithm geneticAlgorithm() {
        return ga;
    }

    final GAGeneticAlgorithm geneticAlgorithm(final GAGeneticAlgorithm g) {
        return ga = g;
    }

    public final Object userData() {
        return ud;
    }

    final Object userData(final Object u) {
        return ud = u;
    }

    final GAEvalData evalData() {
        return evd;
    }

    final GAEvalData evalData(final GAEvalData o) {
        evd.delete();
        evd = (GAEvalData) o.clone();
        return evd;
    }

    final float evaluate() throws clCancelException {
        return evaluate(false);
    }

    private float evaluate(final boolean flag) throws clCancelException {
        if (!_evaluated || flag) {
            final GAGenome This = this;
            if (eval != null) {
                This._neval++;
                This._score = eval.evaluator(This);
            }
            This._evaluated = true;
        }
        return _score;
    }

    final Evaluator evaluator() {
        return eval;
    }

    final Evaluator evaluator(final Evaluator f) {
        _evaluated = false;
        return eval = f;
    }

    public final void initialize() {
        _evaluated = false;
        _neval = 0;
        init.initializer(this);
    }

    public final Initializer initializer() {
        return init;
    }

    public final Initializer initializer(final Initializer op) {
        return init = op;
    }

    final int mutate(final float p) {
        return mutr.mutator(this, p);
    }

    public final Mutator mutator() {
        return mutr;
    }

    public final Mutator mutator(final Mutator op) {
        return mutr = op;
    }

    final float compare(final GAGenome g) {
        return cmp.comparator(this, g);
    }

    final Comparator comparator() {
        return cmp;
    }

    final Comparator comparator(final Comparator c) {
        return cmp = c;
    }

    final SexualCrossover crossover(final SexualCrossover f) {
        return sexcross = f;
    }

    final SexualCrossover sexual() {
        return sexcross;
    }

    final AsexualCrossover crossover(final AsexualCrossover f) {
        return asexcross = f;
    }

    final AsexualCrossover asexual() {
        return asexcross;
    }

}