package sm.gene;

import java.io.IOException;

/**
 * User: Oleg
 * Date: 14/11/2004
 * Time: 11:08:48
 * Description: application for generating test data samples (files)
 */
public class clDataGenerator {


    public static void main(final String[] args) throws IOException {

        final int n1 = 100;
        final String name1 = new String("C:\\Projects\\Test Server\\example1.txb");
        ifDataSet ds1 = new clDSExample1(n1);
        ds1.write2File(name1);
                           /*
        final int n2 = 50;
        final String name2 = new String("C:\\Projects\\Test Server\\example2.txb");
        ifDataSet ds2 = new clDSExample2(n2);
        ds2.write2File(name2);     */
    }

}
