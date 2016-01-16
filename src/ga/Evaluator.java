package ga;

import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 6, 2004
 * Time: 11:53:08 AM
 * Description:
 */
public interface Evaluator {
    float evaluator(GAGenome g) throws clCancelException;
}
