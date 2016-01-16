package sm.base.pm;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * User: Oleg
 * Date: Jun 25, 2004
 * Time: 12:31:27 PM
 * Description: the class is intended to read if possible data description from ports
 * in the following format:
 * e.g.
 * #Problem description:
 * VAT model. Ukraine 1998-1999.
 * #Variables:
 * VAT-1, VAT-2, RGDP-1, RGDP-2
 * VAT
 */
final class clDescriptionReader {
    private final String[] m_sDescriptions = new String[10];

    clDescriptionReader(final BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equals("#Problem description:")) {
                if ((line = reader.readLine()) != null) {
                    // do not read short problem description
                }
                continue;
            }
            if (line.equals("#Variables:")) {
                int j = 0;
                final int lines_of_description = 2;
                int description_line = 0;
                while ((line = reader.readLine()) != null && (description_line < lines_of_description)) {
                    description_line++;
                    // input description
                    final StringTokenizer tokenizer = new StringTokenizer(line, " ,", false);
                    final int n = tokenizer.countTokens();
                    for (int i = 0; i < n; i++) {
                        m_sDescriptions[j++] = tokenizer.nextToken();
                    }
                }
                continue;
            }
            if (line.equals("#Detailed description:"))
                break;
        }
    }

    public String[] getDescriptions() {
        return m_sDescriptions;
    }

    public String toString() {
        return "Reader description from port";
    }
}
