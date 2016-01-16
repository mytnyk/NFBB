package ga;

import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 6, 2004
 * Time: 12:08:34 PM
 * Description:
 */
interface PEvaluator {
    void evaluator(GAPopulation p) throws clCancelException;
}