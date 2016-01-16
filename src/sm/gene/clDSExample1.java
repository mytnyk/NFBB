package sm.gene;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * User: Oleg
 * Date: 14/11/2004
 * Time: 11:26:14
 * Description: ...
 */
public class clDSExample1 implements ifDataSet{

    private double[] m_input;
    private double[] m_output;
    private double[] m_noise;
    private final int m_dim;

    final static int lag = 2; // two step lag

    public clDSExample1(final int n) {
        m_dim = n;
        final int size = n + lag;
        m_input = new double[size];
        m_output = new double[size];
        m_noise = new double[size];
        // generate input:
        Random r = new Random();
        for (int i = 0; i<size; i++) {
            m_input[i] = Math.sin(0.05*Math.PI*i)+Math.sin(0.02*Math.PI*i); // [-1, 1]
        }
        // generate noise:
        for (int i = 0; i<size; i++) {
            m_noise[i] = r.nextGaussian()*0.01; // N(0, 0.1^2)
        }
        // generate output:
        for (int i = 0; i<lag; i++) {
            m_output[i] = 0;
        }
        for (int i = lag; i<size; i++) {
            final double numerator = 2.5*m_output[i-1]*m_output[i-2]*(1 + m_input[i-1]);
            final double denominator = 1 + m_output[i-1]*m_output[i-1] + m_output[i-2]*m_output[i-2];
            final double component = 0.3*Math.cos(0.5*(m_output[i-1]-m_output[i-2])) + 1.2*m_input[i-1];
            final double noise = m_noise[i];
            m_output[i] = numerator/denominator + component + noise;
        }
    }

    public void write2File(final String name) throws IOException {

        final BufferedWriter file = new BufferedWriter(new FileWriter(name));

        file.write(Integer.toString(m_dim-1) + " 3 \n ");
        for (int i = 1; i<m_dim; i++)
        {
            final double p1 = getOutput(i-2);
            final double p2 = getOutput(i-1);
            final double p3 = getInput(i-1);
            file.write(Double.toString(p1) + "\t");
            file.write(Double.toString(p2) + "\t");
            file.write(Double.toString(p3) + "\n ");
        }
        file.write(Integer.toString(m_dim-1) + " 1 \n ");
        for (int i = 1; i<m_dim; i++)
        {
            final double p1 = getOutput(i);
            file.write(Double.toString(p1) + "\n ");
        }
        file.write("\n#Short description:\n");
        file.write("o-2, o-1, i-1\no\n");
        file.close();
    }


    public double getInput(final int t) {
        return m_input[t+lag];  // Add your code here.
    }

    public double getOutput(final int t) {
        return m_output[t+lag];  // Add your code here.
    }
}
