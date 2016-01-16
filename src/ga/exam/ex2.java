package ga.exam;

import ga.*;
import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 8, 2004
 * Time: 12:05:02 PM
 * Description:
 * ----------------------------------------------------------------------------
 * ex2.C
 * Example program for the SimpleGA class and Bin2DecGenome class.  This
 * program generates randomly a series of numbers then tries to match those
 * values in a binary-to-decimal genome.  We use a simple GA (with linear
 * scaled fitness selection and non-steady-state population generation) and
 * binary-to-decimal, 1D genomes.  We also use the userData argument to the
 * objective function.
 * ----------------------------------------------------------------------------
 */
public final class ex2 implements Evaluator {
    public static void main(final String[] args) throws clCancelException {
        final ex2 inst = new ex2();
        inst.test(args);
    }

    private void test(final String[] args) throws clCancelException {
        System.out.println("Example 2");
        System.out.println("This program generates a sequence of random numbers then uses");
        System.out.println("a simple GA and binary-to-decimal genome to match the");
        System.out.println("sequence.");
// See if we've been given a seed to use (for testing purposes).  When you
// specify a random seed, the evolution will be exactly the same each time
// you use that seed number.
        int seed = 0;
        for (int i = 1; i < args.length; i++) {
            if (args[i++] == "seed") {
                seed = Integer.parseInt(args[i]);
            }
        }
// Declare variables for the GA parameters and set them to some default values.
        final int popsize = 25;
        final int ngen = 100;
        final float pmut = (float) 0.01;
        final float pcross = (float) 0.6;

// Generate a sequence of random numbers using the values in the min and max
// arrays.  We also set one of them to integer value to show how you can get
// explicit integer representations by choosing your number of bits
// appropriately.

        GARandom.GARandomSeed(seed);
        final int n = 7;
        final float[] target = new float[n];
        final float[] min = {0, 0, 3, -5, 100, (float) 0.001, 0};
        final float[] max = {1, 100, 3, -2, 100000, (float) 0.010, 7};
        int i;
        for (i = 0; i < n; i++) {
            target[i] = GARandom.GARandomFloat(min[i], max[i]);
        }
        target[6] = GARandom.GARandomInt((int) min[6], (int) max[6]);
// Print out the sequence to see what we got.
        System.out.println("input sequence:");
        for (i = 0; i < n; i++) {
            System.out.print(target[i] + "\t");
        }
        System.out.println();
// Create a phenotype then fill it with the phenotypes we will need to map to
// the values we read from the file.  The arguments to the add() method of a
// Bin2Dec phenotype are (1) number of bits, (2) min value, and (3) max value.
// The phenotype maps a floating-point number onto the number of bits that
// you designate.  Here we just make everything use 8 bits and use the max and
// min that were used to generate the target values.  You can experiment with
// the number of bits and max/min values in order to make the GA work better
// or worse.
        final GABin2DecPhenotype map = new GABin2DecPhenotype();
        for (i = 0; i < n; i++) {
            map.add(8, min[i], max[i]);
        }
// Create the template genome using the phenotype map we just made.  The
// GA will use this genome to clone the population that it uses to do the
// evolution.  We pass the objective function to create the genome.  We
// also use the user data function in the genome to keep track of our
// target values.
        GABin2DecGenome genome = new GABin2DecGenome(map, this, target);

// Now create the GA using the genome, set the parameters, and run it.

        final GASimpleGA ga = new GASimpleGA(genome);
        ga.populationSize(popsize);
        ga.nGenerations(ngen);
        ga.pMutation(pmut);
        ga.pCrossover(pcross);
        ga.scoreFilename("bog.dat");
        ga.flushFrequency(50);  // dump scores to disk every 50th generation
        ga.evolve(seed);

// Dump the results of the GA to the screen.  We print out first what a random
// genome looks like (so we get a bit of a feel for how hard it is for the
// GA to find the right values) then we print out the best genome that the
// GA was able to find.

        genome.initialize();
        System.out.println("random values in the genome:");
        for (i = 0; i < map.nPhenotypes(); i++) {
            System.out.print(genome.phenotype(i) + "\t");
        }
        System.out.println();

        genome = (GABin2DecGenome) ga.statistics().bestIndividual();
        System.out.println("the ga generated:");
        for (i = 0; i < map.nPhenotypes(); i++) {
            System.out.print(genome.phenotype(i) + "\t");
        }
        System.out.println();
// We could print out the genome directly, like this:
// cout << genome << "\n";

// Clean up by freeing the memory we allocated.

        //delete [] target;
    }

    /**
     * For this objective function we try to match the values in the array of float
     * that is passed to us as userData.  If the values in the genome map to
     * values that are close, we return a better score.  We are limited to positive
     * values for the objective value (because we're using linear scaling - the
     * default scaling method for SimpleGA), so we take the reciprocal of the
     * absolute value of the difference between the value from the phenotype and
     * the value in the sequence.
     */
    public float evaluator(final GAGenome g) {
        final GABin2DecGenome genome = (GABin2DecGenome) g;
        final float[] sequence = (float[]) g.userData();

        float value = genome.nPhenotypes();
        for (int i = 0; i < genome.nPhenotypes(); i++) {
            value += 1.0 / (1.0 + Math.abs(genome.phenotype(i) - sequence[i]));
        }
        return value;
    }
}
