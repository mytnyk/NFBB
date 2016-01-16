package sm.algo;

import ga.*;
import sm.base.data.clMatrixData;
import sm.base.util.clCancelException;
import sm.base.util.clTracer;
import sm.base.util.ifProgressCallBack;
import sm.appl.StringResources;


/**
 * User: Oleg
 * Date: Jul 14, 2004
 * Time: 3:31:28 PM
 * Description: this class is developed to optimize knot positions using
 * genetic algorithm; may be used for optimizations with 2d data at all
 */
final class clGAOptimizer implements Evaluator {
    // Declare variables for the GA parameters and set them to some default values.
    private static final int popsize = 10;
    private static final int ngen = 100;
    private static final float pmut = (float) 0.01;
    private static final float pcross = (float) 0.6;

    private final double[][] m_vData; // the data set which should be optimized
    private final ifObjectiveFunction m_cOptimizer; // optimization function based on the data set
    private final GABin2DecPhenotype map;
    private final GASimpleGA ga;
    private final ifProgressCallBack m_cFeedBack;

    private int m_iProgressCounter = 0;

    /**
     * @param vData      - the data to be optimized, will be optimized also
     * @param iOptimizer - optimization function
     * @param cb         - callback
     * @throws clCancelException - can be interrupted by user
     */
    clGAOptimizer(final double[][] vData,
                  final ifObjectiveFunction iOptimizer,
                  final ifProgressCallBack cb) throws clCancelException {
        // how long Phenotype vector should be - depends on m_iArgs,
        // we represent here in Phenotype two dim array of data as one dim.!!!
        m_vData = vData;
        m_cOptimizer = iOptimizer;
        m_cFeedBack = cb;
        // Create a phenotype for the data variables.  The number of bits you can use to
        // represent any number is limited by the type of computer you are using.  In
        // this case, we use 16 bits to represent a floating point number whose value
        // can range from minData to maxData, inclusive.
        map = new GABin2DecPhenotype();
        // TODO: here we should determine the acceptable dx, dy range for varying the Data
        //float dx = (float) 0.1;
        //float dy = (float) 0.1;
        for (int i = 0; i < m_vData.length; i++)  // add here each data sample!!! with its range
        {
            for (int j = 0; j < m_vData[0].length; j++) {
                map.add(3, (float) (m_vData[i][j] - 0.24), (float) (m_vData[i][j] + 0.24));
            }
        }

        // Create the template genome using the phenotype map we just made.
        final GABin2DecGenome genome = new GABin2DecGenome(map, this, m_vData);
        // Now create the GA using the genome and run it.  We'll use sigma truncation
        // scaling so that we can handle negative objective scores.
        ga = new GASimpleGA(genome);
        // set GA parameters:
        final GASigmaTruncationScaling scaling = new GASigmaTruncationScaling();
        ga.populationSize(popsize);
        ga.nGenerations(ngen);
        ga.pMutation(pmut);
        ga.pCrossover(pcross);
        ga.scaling(scaling);
        ga.scoreFrequency(10);
        ga.flushFrequency(50);
    }

    void optimizeDataSet() throws clCancelException {
        // PROCESSING!
        ga.evolve();
        // get the best estimation:
        final GABin2DecGenome genome = (GABin2DecGenome) ga.statistics().bestIndividual();
        // read the optimal data set in the result buffer m_vData
        getDataFromGenome(genome, m_vData);
        // just show data for debug!!
        clTracer.straceln("the ga found an optimum at the :");
        (new clMatrixData(m_vData)).dumpData();
    }

    /**
     * This objective function tries to minimize the value of the covar. trace
     */
    public float evaluator(final GAGenome g) throws clCancelException {
        // get the current genome
        final GABin2DecGenome genome = (GABin2DecGenome) g;
        // get the temp. buffer data from genome
        final double[][] vData = (double[][]) g.userData();
        // read the current data set from genome
        getDataFromGenome(genome, vData);

        // build the current matrix with optimal data set!
        final float y = (float) m_cOptimizer.getObjectiveFunctionValue(vData);
        // clTracer.straceln("opt = " + y);
        // estimated number of calls :  0.8*popsize*ngen; SO:
        int p = 120 * m_iProgressCounter++ / (popsize * ngen);
        if (p > 100) p = 100;
        boolean bContinue = true;
        if (m_cFeedBack != null) {
            bContinue = m_cFeedBack.progressCallback(p, StringResources.get(StringResources.progressflow) + p);
        }
        if (!bContinue) {
            throw new clCancelException(StringResources.get(StringResources.callbackbreak));
        }
        // return the estimation:
        return -y;
    }

    /**
     * @param genome - source data to be get from
     * @param vData  - return parameter, should be filled in
     */
    private static void getDataFromGenome(final GABin2DecGenome genome, final double[][] vData) {
        int k = 0;
        for (int i = 0; i < vData.length; i++) {
            for (int j = 0; j < vData[0].length; j++) {
                vData[i][j] = (double) genome.phenotype(k++);
            }
        }
    }

    public String toString() {
        return "GA Optimizator";
    }
}
