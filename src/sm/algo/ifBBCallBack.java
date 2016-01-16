package sm.algo;

import sm.base.util.ifProgressCallBack;

/**
 * User: Oleg
 * Date: Jul 12, 2004
 * Time: 1:57:44 PM
 * Description: complex callback for BB expansion functions
 * which provide user with progress callback and knots info callback
 */
interface ifBBCallBack extends ifKnotCallBack, ifProgressCallBack {
}
