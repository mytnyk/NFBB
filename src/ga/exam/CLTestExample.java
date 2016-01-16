package ga.exam;

import ga.*;
import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 5, 2004
 * Time: 1:58:33 PM
 * Description: DEBUGGING!!!
 */
public final class CLTestExample implements Evaluator {
    private static final int length = 15;
    private final Object[] numbers_n = new TObjInt[length];

    /**
     * just for debugging - GAAlleleSet
     */
    public static void main(final String[] args) throws clCancelException {
        final CLTestExample inst = new CLTestExample();
        inst.test();
    }

    private void test() throws clCancelException {

        for (int i = 0; i < length; i++) {
            numbers_n[i] = new TObjInt(i + 1);
        }
        final GAAlleleSet alleles = new GAAlleleSet(length, numbers_n);

        // find first maximum
        GA1DArrayAlleleGenome genome1 = new GA1DArrayAlleleGenome(1, alleles, this);

        final GASimpleGA ga1 = new GASimpleGA(genome1);
        final GASigmaTruncationScaling scaling = new GASigmaTruncationScaling();
        final int popsize = 50;
        final int ngen = 1000;
        final float pmut = (float) 0.01;
        final float pcross = (float) 0.6;

        ga1.populationSize(popsize);
        ga1.nGenerations(ngen);
        ga1.pMutation(pmut);
        ga1.pCrossover(pcross);
        ga1.scaling(scaling);
        ga1.scoreFilename("results.dat");
        ga1.scoreFrequency(10);
        ga1.flushFrequency(50);
        ga1.evolve();
        // Dump the results of the GA to the screen.

        genome1 = (GA1DArrayAlleleGenome) ga1.statistics().bestIndividual();
        final int iFirstPointAllele = ((TObjInt) genome1.gene(0)).getValue();
        final int iFirstPointIndex = iFirstPointAllele - ((TObjInt) numbers_n[0]).getValue();

        System.out.print(iFirstPointIndex);
        genome1.delete();

        alleles.delete();
    }

    public float evaluator(final GAGenome c) {
        final GA1DArrayAlleleGenome genome = (GA1DArrayAlleleGenome) c;
        final int iFirstPointAllele = ((TObjInt) genome.gene(0)).getValue();
        final int iFirstPointIndex = iFirstPointAllele - ((TObjInt) numbers_n[0]).getValue();

        final float y = 5 * iFirstPointIndex - iFirstPointIndex * iFirstPointIndex;//numbers_y[iFirstPointIndex];// y[n]

        return y;
    }
}
