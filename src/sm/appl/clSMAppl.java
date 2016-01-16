package sm.appl;

import sm.algo.ifFullCallBack;
import sm.base.data.clMatrixData;
import sm.base.data.ifMatrixData;
import sm.base.data.ifVectorData;
import sm.base.util.clCancelException;
import sm.base.util.clMathEx;
import sm.base.util.clTracer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;
import java.applet.AppletStub;
import java.applet.AppletContext;
import java.net.URL;

public final class clSMAppl extends JApplet implements Runnable, ActionListener {
    private String m_sSourceData = "";
    private clSMCommonControls m_clSMCommonControls = null;
    private JTabbedPane m_cBranchPane = null;

    public void init() {
        m_sSourceData = getParameter("sourceData");
        String locale = getParameter("locale");
        if (locale != null)
            StringResources.g_locale = locale;

        // preload list of available data sets
        Vector list = (new clPreloader(getParameter("sourceDataList") + "_list_")).getDataSets();

        final Container co = getContentPane();
        co.setLayout(new BorderLayout());
        co.add(m_clSMCommonControls = new clSMCommonControls(this, list), BorderLayout.NORTH);
        co.add(m_cBranchPane = new JTabbedPane(), BorderLayout.CENTER);

        new Thread(this, "Starting main thread.").start();
    }

    public void run() {
        /*try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();  // Add your code here.
        }*/
    }

    public void actionPerformed(final ActionEvent e) {
        final String label = e.getActionCommand();
        if (label.equals(StringResources.get(StringResources.loaddata))) {
            final clSMBranch jp = new clSMBranch(m_sSourceData + m_clSMCommonControls.getLoadChoice());
            m_cBranchPane.add(m_clSMCommonControls.getLoadChoice(), jp);
            m_cBranchPane.setSelectedIndex(m_cBranchPane.getTabCount() - 1);
            jp.start();
        }
    }

    public static void main(String[] argv)
    {
        new AppletFrame(StringResources.get(StringResources.appletname),new clSMAppl(),1000,800);
    }

}

class AppletFrame extends JFrame {
	AppletFrame(String title, JApplet applet, int width, int height)
	{
		super(title);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
        applet.setStub(new IdentStub());
		applet.init();
		//applet.setSize(width,height);
		applet.start();
		this.getContentPane().add(applet);
		//this.pack();
        this.setSize(width,height);
		this.setVisible(true);
        this.setTitle(StringResources.get(StringResources.appletname));
	}
}

class IdentStub implements AppletStub {

    public boolean isActive() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void appletResize(int width, int height) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public AppletContext getAppletContext() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public URL getCodeBase() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public URL getDocumentBase() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getParameter(String name) {
        if (name.equals("sourceDataList"))
            return ".\\";
        if (name.equals("sourceData"))
            return ".\\_data_\\";
        if (name.equals("locale"))
            return "uk";
        return null;
    }

}

final class clSMKnotsSet extends JPanel implements ActionListener {
    private final clSMKnotCanvas m_clKnotCanvas;
    private final JScrollPane m_cScrollPane;
    private final clSMKnotControls m_clKnotControls;

    clSMKnotsSet() {
        setLayout(new BorderLayout());
        add(m_clKnotControls = new clSMKnotControls(this), BorderLayout.NORTH);
        m_cScrollPane = new JScrollPane(m_clKnotCanvas = new clSMKnotCanvas(m_clKnotControls.getSubscription()));
        add(m_cScrollPane, BorderLayout.CENTER);
    }

    void addData(final int iIndex, final ifMatrixData data) {
        m_clKnotCanvas.addData(iIndex, data);
        m_cScrollPane.doLayout();
        m_clKnotCanvas.repaint();
    }

    public void actionPerformed(final ActionEvent e) {
        final String label = e.getActionCommand();
        if (label.equals(StringResources.get(StringResources.showsubscripts))) {
            m_clKnotCanvas.showSubscription(m_clKnotControls.getSubscription());
        }
    }

    public void refresh() {
        m_clKnotCanvas.repaint();
    }
}

final class clSMDataSet extends JPanel implements ActionListener {
    private final clSMDataCanvas m_clDataCanvas;
    private final JScrollPane m_cScrollPane;
    private final clSMDataControls m_clDataControls;

    clSMDataSet() {
        setLayout(new BorderLayout());
        add(m_clDataControls = new clSMDataControls(this), BorderLayout.NORTH);
        m_cScrollPane =
        new JScrollPane(m_clDataCanvas
                        = new clSMDataCanvas(m_clDataControls.getSubscription(), m_clDataControls.getActiveTestRate()));
        add(m_cScrollPane, BorderLayout.CENTER);
    }

    public void actionPerformed(final ActionEvent e) {
        final String label = e.getActionCommand();
        if (label.equals(StringResources.get(StringResources.showcurve))) {
            m_clDataCanvas.showCurve(Integer.parseInt(m_clDataControls.getText().trim()));
        } else if (label.equals(StringResources.get(StringResources.clearcurve))) {
            m_clDataCanvas.clear(Integer.parseInt(m_clDataControls.getText().trim()));
        } else if (label.equals(StringResources.get(StringResources.showsubscripts))) {
            m_clDataCanvas.showSubscription(m_clDataControls.getSubscription());
        } else if (label.equals(StringResources.get(StringResources.showtestrate))) {
            m_clDataCanvas.showTestRate(m_clDataControls.getActiveTestRate());
        }
    }

    void addData(final ifVectorData vector, final double dModelEstimation, final double dQuality, final double dTRate) {
        m_clDataCanvas.addData(vector, dModelEstimation, dQuality, dTRate);
        m_cScrollPane.doLayout();
    }

    void enableControls(final boolean b) {
        m_clDataControls.enableControls(b);
    }
}

final class clSMBranch extends JPanel implements Runnable, ActionListener, ifFullCallBack {
    private final ifDataLoader m_cDataLoader;
    private final clSMBranchControls m_clBranchControls;
    private transient boolean m_bContinue = true;
    private ifSystemModeler m_cSystemModeler = null;
    private JTabbedPane m_cViewSetPane = null;
    private clSMDataSet m_clDataSet = null;
    private clSMKnotsSet m_clKnotsSet = null;

    clSMBranch(final String sSourceData) {
        m_cDataLoader = new clDataLoader(sSourceData);
        setLayout(new BorderLayout());
        add(m_clBranchControls = new clSMBranchControls(this), BorderLayout.NORTH);
        m_clBranchControls.enableControls(false);
        add(m_cViewSetPane = new JTabbedPane(), BorderLayout.CENTER);
        m_cViewSetPane.add(StringResources.get(StringResources.dataset), m_clDataSet = new clSMDataSet());
        m_cViewSetPane.add(StringResources.get(StringResources.knotset), m_clKnotsSet = new clSMKnotsSet());
        m_cViewSetPane.setSelectedIndex(0);
        m_clDataSet.enableControls(false);
    }

    void start() {
        new Thread(this, "Starting branch thread.").start();
    }

    public void run() {
        m_clBranchControls.setStatusInfo(StringResources.get(StringResources.loadingdata));
        m_cDataLoader.start();
        try {
            synchronized (m_cDataLoader) {
                if (!m_cDataLoader.isDataLoaded()) {
                    m_cDataLoader.wait();
                }
            }
        } catch (InterruptedException e) {
            m_clBranchControls.setStatusInfo(e.getMessage());
            return;
        }
        final clCancelException cel = m_cDataLoader.getCancelled();
        if (cel != null) {
            m_clBranchControls.setStatusInfo(cel.getMessage());
            return;
        }

        m_clBranchControls.setStatusInfo(StringResources.get(StringResources.dataloaded));
        m_cSystemModeler = new clSystemModeler(m_cDataLoader, this);

        final int iRows = m_cDataLoader.getNubmerOfInputs();
        for (int i = 0; i < iRows; i++) {
            m_clDataSet.addData(m_cDataLoader.getInput(i), -1, 999, 0.0);
        }

        m_clDataSet.addData(m_cDataLoader.getOutput(), -1, 999, 0.0);

        m_clDataSet.enableControls(true);
        // here should organize the work loop
        while (true) {
            m_clBranchControls.enableControls(true);
            try {
                synchronized (m_cSystemModeler) {
                    m_cSystemModeler.wait();
                    final clCancelException ce = m_cSystemModeler.getCancelled();
                    if (ce == null) {
                        m_clDataSet.addData(m_cSystemModeler.getEstimatedOutput(),
                                            m_cSystemModeler.getModelEstimation(),
                                            m_cSystemModeler.getQualityCriterion(),
                                            m_clBranchControls.getRate());
                        m_clBranchControls.setStatusInfo(StringResources.get(StringResources.identfinished));
                    } else {
                        clTracer.straceln(ce.toString());
                        m_clBranchControls.setStatusInfo(ce.getMessage());
                    }
                }
            } catch (InterruptedException e) {
                clTracer.strace("Finish branch thread: " + e.toString());
                m_clBranchControls.setStatusInfo("Finish branch thread: " + e.toString());
            }
        }
    }

    public void actionPerformed(final ActionEvent e) {
        final String label = e.getActionCommand();
        if (label.equals(StringResources.get(StringResources.identify))) {
            m_bContinue = true;
            m_clBranchControls.setStatusInfo(StringResources.get(StringResources.identificationprocess));
            m_cSystemModeler.start(m_clBranchControls.getMethodChoice(), m_clBranchControls.getRate(),
                                   m_clBranchControls.getDegree(),
                                   m_clBranchControls.getOptimization(), m_clBranchControls.getFullExpansion());
            m_clBranchControls.enableControls(false);
        } else if (label.equals(StringResources.get(StringResources.cancel))) {
            m_bContinue = false;
        }
    }

    public boolean progressCallback(final int iInfo, final String sInfo) {
        m_clBranchControls.setProgress(iInfo, sInfo);
        return m_bContinue;
    }

    public boolean infoCallback(final String sInfo) {
        m_clBranchControls.setAlgInfo(sInfo);
        return m_bContinue;
    }

    public void knotCallback(final int iIndex, final ifMatrixData data) {
        m_clKnotsSet.addData(iIndex, data);
    }

    public void refresh() {
        m_clKnotsSet.refresh();
    }
}


final class clSMKnotCanvas extends JPanel {
    private static final Font c_fSubscripts = new Font(null, Font.ITALIC, 10);
    private static final int c_iKnotBoxX = 10;
    private static final int c_iKnotBoxY = 10;
    private static final int c_iKnotBoxW = 200;
    private static final int c_iKnotBoxH = 200;
    private static final int c_iReduction = 50;
    private static final int c_iMaxEntries = 200;
    private static final int c_iMaxGroups = 20;
    private int m_iAreaWidth = 900;
    private int m_iAreaHeight = 500;

    private final ifMatrixData[] m_cKnotValues = new clMatrixData[c_iMaxEntries];
    private final int[] m_cGroupValues = new int[c_iMaxGroups];

    private int m_iNums = 0;
    private int m_iGroups = 0;
    private boolean m_bShowSubscripts = true;

    clSMKnotCanvas(final boolean bShowSubscripts) {
        m_bShowSubscripts = bShowSubscripts;
    }

    public void paint(final Graphics g) {
        g.setColor(Color.gray);
        g.fillRect(0, 0, m_iAreaWidth, m_iAreaHeight);

        if (m_bShowSubscripts) g.setFont(c_fSubscripts);
        int iKnotCounter = 0;
        // TODO: think about more flexible calculation
        // suppose: x: -1..2, y: -0.5..2.5
        final int iXRange = 3;
        final int iYRange = 3;
        final int iXscale = (c_iKnotBoxW - c_iReduction) / iXRange;
        final int iYscale = (c_iKnotBoxH - c_iReduction) / iYRange;
        for (int j = 0; j < m_iGroups; j++) {
            int iNumsInGroup = m_cGroupValues[j];
            int iOffsetY = j * (c_iKnotBoxY + c_iKnotBoxH);
            int iDataOffsetY = iOffsetY + c_iKnotBoxY + (c_iReduction >> 1) +
                               (c_iKnotBoxH - c_iReduction) / (2 * iYRange);
            for (int k = 0; k < iNumsInGroup; k++) {
                int iOffsetX = k * (c_iKnotBoxW + c_iKnotBoxX);
                int iDataOffsetX = iOffsetX + c_iKnotBoxX + (c_iReduction >> 1) +
                                   (c_iKnotBoxW - c_iReduction) / iXRange;
                g.setColor(Color.white);
                g.fillRect(c_iKnotBoxX + iOffsetX, c_iKnotBoxY + iOffsetY, c_iKnotBoxW, c_iKnotBoxH);
                g.setColor(Color.black);
                g.drawRect(c_iKnotBoxX + iOffsetX, c_iKnotBoxY + iOffsetY, c_iKnotBoxW, c_iKnotBoxH);
                final double[][] arrKnot = m_cKnotValues[iKnotCounter].get2DArrayPtr();
                iKnotCounter += 1;
                if (arrKnot[0].length > 1) {
                    for (int i = 0; i < arrKnot.length; i++) {
                        final double xd = arrKnot[i][0];
                        final double yd = arrKnot[i][1];
                        final int x = (int) (iXscale * xd) + iDataOffsetX;
                        final int y = (int) (iYscale * yd) + iDataOffsetY;
                        g.drawOval(x - 1, y - 1, 2, 2);
                        if (m_bShowSubscripts) {
                            g.drawString(clMathEx.formatDouble(xd) + ";" + clMathEx.formatDouble(yd), x, y);
                        }
                    }
                } else {
                    for (int i = 0; i < arrKnot.length; i++) {
                        final double xd = arrKnot[i][0];
                        final int x = (int) (iXscale * xd) + iDataOffsetX;
                        final int y = iDataOffsetY;
                        g.drawOval(x - 1, y - 1, 2, 2);
                        if (m_bShowSubscripts) {
                            g.drawString(clMathEx.formatDouble(xd), x, y);
                        }
                    }
                }
            }
        }
    }

    void addData(final int iIndex, final ifMatrixData data) {
        if (iIndex >= 0 && iIndex < c_iMaxGroups) {
            m_cGroupValues[iIndex] += 1;
            if (m_iGroups <= iIndex) {
                m_iGroups = iIndex + 1;
            }
        }
        m_cKnotValues[m_iNums] = data;
        m_iNums += 1;
        // calculate new sizes of drawing area!

        int iAreaWidth = c_iKnotBoxX + (c_iKnotBoxW + c_iKnotBoxX) * m_cGroupValues[iIndex];
        if (m_iAreaWidth < iAreaWidth) m_iAreaWidth = iAreaWidth;
        int iAreaHeight = c_iKnotBoxY + (c_iKnotBoxH + c_iKnotBoxY) * m_iGroups;
        if (m_iAreaHeight < iAreaHeight) m_iAreaHeight = iAreaHeight;
        setSize(m_iAreaWidth, m_iAreaHeight);
        setPreferredSize(new Dimension(m_iAreaWidth, m_iAreaHeight));
    }

    void showSubscription(final boolean bSubscription) {
        m_bShowSubscripts = bSubscription;
        repaint();
    }
}

final class clSMDataCanvas extends JPanel {
    private static final Font c_fSubscripts = new Font(null, Font.ITALIC, 10);
    private static final Font c_fDataInfo = new Font(null, Font.BOLD, 10);
    private static final int c_iCurveBoxX = 10;
    private static final int c_iCurveBoxY = 10;
    private static final int c_iCurveBoxW = 900;
    private static final int c_iCurveBoxH = 280;

    private static final int c_iDataBelowCurve = 50;
    private static final int c_iDataCellW = 30;
    private static final int c_iDataDescW = 150;
    private static final int c_iDataCellH = 20;
    private static final int c_iTextOffY = -5;
    private static final int c_iTextOffX = 4;
    private static final int c_iScrollOffset = 40;
    private static final int c_iMaxEntries = 20;

    private final String[] m_sDescription = new String[c_iMaxEntries];
    private final ifVectorData[] m_lValues = new ifVectorData[c_iMaxEntries];
    private final double[] m_dModelEstimation = new double[c_iMaxEntries];
    private final double[] m_dModelQuality = new double[c_iMaxEntries];
    private final boolean[] m_bVisibleData = new boolean[c_iMaxEntries];
    private final double[] m_iTestRate = new double[c_iMaxEntries];

    private int m_iAreaWidth = 1000;
    private int m_iAreaHeight = 500;

    private int m_iCols = 0;
    private int m_iNums = 0;

    private double m_iMax = 0.0;
    private double m_iMin = 0.0;
    private boolean m_bShowSubscripts = true;
    private boolean m_bActiveTestRate = true;

    clSMDataCanvas(final boolean bShowSubscripts, final boolean bActiveTestRate) {
        m_bShowSubscripts = bShowSubscripts;
        m_bActiveTestRate = bActiveTestRate;
    }

    void addData(final ifVectorData vector, final double dModelEstimation, final double dQuality, final double dTRate) {
        if (m_iNums == c_iMaxEntries) {
            clTracer.straceln("Cannot add data to canvas anymore!");
            return;
        }
        m_iCols = vector.getArraySize();
        m_lValues[m_iNums] = vector;
        m_sDescription[m_iNums] = vector.getDataDescription();
        m_bVisibleData[m_iNums] = false;
        m_iTestRate[m_iNums] = dTRate;
        m_dModelEstimation[m_iNums] = dModelEstimation;
        m_dModelQuality[m_iNums] = dQuality;
        // enlarge min max region with new data
        double iMin;
        double iMax;
        iMin = iMax = vector.getValue(0);
        for (int i = 0; i < m_iCols; i++) {
            final double d = vector.getValue(i);
            if (d > iMax) {
                iMax = d;
            } else if (d < iMin) {
                iMin = d;
            }
        }
        if (m_iNums == 0) { // first time
            m_iMin = iMin;
            m_iMax = iMax;
        } else {
            if (m_iMin > iMin) {
                m_iMin = iMin;
            }
            if (m_iMax < iMax) {
                m_iMax = iMax;
            }
        }
        m_iNums += 1;
        // calculate new sizes of drawing area!
        m_iAreaWidth = c_iCurveBoxX + c_iDataDescW + c_iScrollOffset + m_iCols * c_iDataCellW;
        m_iAreaHeight = c_iCurveBoxY + c_iCurveBoxH + c_iScrollOffset + c_iDataBelowCurve + m_iNums * c_iDataCellH;
        setSize(m_iAreaWidth, m_iAreaHeight);
        setPreferredSize(new Dimension(m_iAreaWidth, m_iAreaHeight));
    }

    public void paint(final Graphics g) {
        g.setColor(Color.gray);
        g.fillRect(0, 0, m_iAreaWidth, m_iAreaHeight);

        if (m_iNums > 0) {
            g.setColor(Color.white);
            g.fillRect(c_iCurveBoxX, c_iCurveBoxY, c_iCurveBoxW, c_iCurveBoxH);
            g.setColor(Color.black);
            g.drawRect(c_iCurveBoxX, c_iCurveBoxY, c_iCurveBoxW, c_iCurveBoxH);
        }

        for (int i = 0; i < m_iNums; i++) {
            if (m_bVisibleData[i]) {
                line(g, i);
            }
        }
        white(g);
    }

    private void white(final Graphics g) {

        final int iDataBoxX = c_iCurveBoxX;
        final int iDataBoxY = c_iCurveBoxY + c_iCurveBoxH + c_iDataBelowCurve;
        final int iDataBoxW = c_iDataDescW + c_iDataCellW * m_iCols;
        final int iDataBoxH = c_iDataCellH * m_iNums;

        g.setColor(Color.white);
        g.fillRect(iDataBoxX, iDataBoxY, iDataBoxW, iDataBoxH);
        g.setColor(Color.black);
        g.drawRect(iDataBoxX, iDataBoxY, iDataBoxW, iDataBoxH);
        g.setFont(c_fDataInfo);
        for (int j = 0; j < m_iNums; j++) {
            setColor(g, j);
            int x = iDataBoxX;
            final int y = iDataBoxY + (j + 1) * c_iDataCellH;
            // set description:
            g.drawString(String.valueOf(j + 1) + ". " + m_sDescription[j], c_iTextOffX + x, c_iTextOffY + y);
            g.setColor(Color.black);
            x += c_iDataDescW;
            // horizontal line
            g.drawLine(iDataBoxX, y, x + c_iDataCellW * m_iCols, y);
            for (int i = 0; i < m_iCols; i++) {
                g.drawString(clMathEx.formatDouble(m_lValues[j].getValue(i)), c_iTextOffX + x, c_iTextOffY + y);

                // vertical line
                g.drawLine(x, y - c_iDataCellH, x, y);
                x += c_iDataCellW;
            }
        }
    }

    private void line(final Graphics g, final int iCurve) {

        if (iCurve > m_iNums + 1) {
            g.drawString("Please Input Number under " + (m_iNums + 1), 100, 100);
        } else {
            final int iOffR = c_iCurveBoxX + 10;
            final int iOffT = c_iCurveBoxY + 10;
            final int iOffL = c_iCurveBoxH - 20;
            final int iOffB = c_iCurveBoxW - 100;

            // Draw description:
            g.setColor(Color.white);
            g.fillRect(iOffB, iOffT, 100, 16);
            setColor(g, iCurve);
            g.drawString(StringResources.get(StringResources.MSEestimation), iOffB, iOffT + 12);
            g.drawString(StringResources.get(StringResources.modelquality), iOffB + 50, iOffT + 12);

            g.setFont(c_fDataInfo);
            if (m_dModelEstimation[iCurve] >= 0) {
                g.drawString(String.valueOf(clMathEx.formatDouble(m_dModelEstimation[iCurve], 4)),
                             iOffB, iOffT + iCurve * 12);
            }
            if (m_dModelQuality[iCurve] != 999) {
                g.drawString(String.valueOf(clMathEx.formatDouble(m_dModelQuality[iCurve], 4)),
                             iOffB + 50, iOffT + iCurve * 12);
            }
            if (m_bShowSubscripts) g.setFont(c_fSubscripts);
            int x = iOffR;
            final int y = iOffL + iOffT;
            final int xstep = iOffB / m_iCols;
            final double dDecode = (double) iOffL / (m_iMax - m_iMin);
            int iYCoordPrev = y - (int) ((double) (m_lValues[iCurve].getValue(0) - m_iMin) * dDecode);
            String sDesc = clMathEx.formatDouble(m_lValues[iCurve].getValue(0));
            if (m_bShowSubscripts) {
                g.drawString(sDesc, x, iYCoordPrev);
            }

            for (int i = 1; i < m_iCols; i++) {
                x += xstep;
                sDesc = clMathEx.formatDouble(m_lValues[iCurve].getValue(i));
                final int iYCoord = y - (int) ((double) (m_lValues[iCurve].getValue(i) - m_iMin) * dDecode);
                if (m_bShowSubscripts) {
                    g.drawString(sDesc, x, iYCoord);
                }

                g.drawLine(x - xstep, iYCoordPrev, x, iYCoord);

                iYCoordPrev = iYCoord;
            }

            if (m_bActiveTestRate) {
                // set test rate separator
                final int iTestSamples = (int) ((double) m_lValues[iCurve].getArraySize() * m_iTestRate[iCurve] + 0.5);
                if (iTestSamples > 0) {
                    x = iOffR + (m_iCols - iTestSamples) * xstep;
                    g.drawLine(x, iOffT, x, iOffL + iOffT);
                }
            }
        }
    }

    private static void setColor(final Graphics g, final int iCurve) {
        //int iStep = 0x33; // 5 steps on sample
        final int iStepR = 0x3F; // 4 steps on sample
        final int iStepG = 0x55; // 3 steps on sample
        final int iStepB = 0x33; // 5 steps on sample
        final int iRed = iStepR * (iCurve % 4);
        final int iGreen = iStepG * (iCurve % 3);
        final int iBlue = iStepB * (iCurve % 5);
        g.setColor(new Color(iRed, iGreen, iBlue));
    }

    void showCurve(final int which) {
        if (which > c_iMaxEntries) return;
        m_bVisibleData[which - 1] = true;
        repaint();
    }

    void clear(final int which) {
        if (which > c_iMaxEntries) return;
        m_bVisibleData[which - 1] = false;
        repaint();
    }

    void showSubscription(final boolean bSubscription) {
        m_bShowSubscripts = bSubscription;
        repaint();
    }

    void showTestRate(final boolean bActiveTestRate) {
        m_bActiveTestRate = bActiveTestRate;
        repaint();
    }
}

final class clSMCommonControls extends JPanel {
    private final JComboBox m_cLoadChoice;

    clSMCommonControls(final ActionListener parentListener, Vector list) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        final JPanel p0 = new JPanel();
        p0.setLayout(new FlowLayout(FlowLayout.LEFT));
        m_cLoadChoice = new JComboBox();

        int items = list.size();
        for (int i = 0; i < items; i++) {
            m_cLoadChoice.addItem(list.get(i));
        }

        final JButton b0 = new JButton(StringResources.get(StringResources.loaddata));
        p0.add(m_cLoadChoice);
        p0.add(b0);
        add(p0);
        b0.addActionListener(parentListener);
    }

    String getLoadChoice() {
        return (String) m_cLoadChoice.getSelectedItem();
    }
}

final class clSMBranchControls extends JPanel {
    private final JComboBox m_cMethodChoice;
    private final JTextField m_cTestRate;
    private final JTextField m_cPolyDegree;
    private final JCheckBox m_cFullExpansion;
    private final JLabel m_cStatusInfo;
    private final JLabel m_cAlgInfo;
    private final JProgressBar m_cProgressBar;
    private final JButton m_cIdentifyButton;
    private final JButton m_cCancelButton;
    private final ButtonGroup m_cButtonGroup;
    private final JRadioButton m_cWithoutOptimization;
    private final JRadioButton m_cUseFastMethod;
    private final JRadioButton m_cAOptimization;
    private final JRadioButton m_cDOptimization;

    clSMBranchControls(final ActionListener parentListener) {
        super(new GridLayout(2, 1, 0, 0));

        final JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p1.add(new JLabel(StringResources.get(StringResources.algorithm)));
        m_cMethodChoice = new JComboBox();
        m_cMethodChoice.addItem(clMethodKind.enGMDHLinear.getName());
        m_cMethodChoice.addItem(clMethodKind.enGMDHParabolic.getName());
        m_cMethodChoice.addItem(clMethodKind.enGMDHBB.getName());
        m_cMethodChoice.addItem(clMethodKind.enHarrisBB.getName());
        m_cMethodChoice.addItem(clMethodKind.enHarrisOptimal.getName());
        m_cMethodChoice.addItem(clMethodKind.enGMDHOptimal.getName());

        p1.add(m_cMethodChoice);
        p1.add(new JLabel(StringResources.get(StringResources.testrate)));
        p1.add(m_cTestRate = new JTextField("0.2", 3));
        p1.add(new JLabel(StringResources.get(StringResources.polydegree)));
        p1.add(m_cPolyDegree = new JTextField("3", 3));
        p1.add(m_cFullExpansion = new JCheckBox(StringResources.get(StringResources.fullexp), false));

        m_cButtonGroup = new ButtonGroup();
        m_cButtonGroup.add(m_cWithoutOptimization = new JRadioButton(StringResources.get(StringResources.withoutopt), true));
        m_cButtonGroup.add(m_cUseFastMethod = new JRadioButton(StringResources.get(StringResources.fastopt), false));
        m_cButtonGroup.add(m_cAOptimization = new JRadioButton(StringResources.get(StringResources.Aopt), false));
        m_cButtonGroup.add(m_cDOptimization = new JRadioButton(StringResources.get(StringResources.Dopt), false));

        final JPanel p11 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p11.add(m_cWithoutOptimization);
        p11.add(m_cUseFastMethod);
        p11.add(m_cAOptimization);
        p11.add(m_cDOptimization);
        p11.setBorder(BorderFactory.createEtchedBorder());
        p1.add(p11);

        add(p1);

        final JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p2.add(m_cIdentifyButton = new JButton(StringResources.get(StringResources.identify)));
        p2.add(m_cCancelButton = new JButton(StringResources.get(StringResources.cancel)));
        p2.add(m_cProgressBar = new JProgressBar(0, 100));
        m_cProgressBar.setStringPainted(true);
        m_cProgressBar.setString("");
        p2.add(m_cStatusInfo = new JLabel(""));
        p2.add(m_cAlgInfo = new JLabel(""));
        add(p2);

        m_cIdentifyButton.addActionListener(parentListener);
        m_cCancelButton.addActionListener(parentListener);
    }

    void setProgress(final int i, final String sInfo) {
        m_cProgressBar.setValue(i);
        m_cProgressBar.setString(sInfo);
    }

    void setStatusInfo(final String s) {
        m_cStatusInfo.setText(s);
    }

    void setAlgInfo(final String s) {
        m_cAlgInfo.setText(s);
    }

    void enableControls(final boolean bo) {
        m_cIdentifyButton.setEnabled(bo);
        // m_cCancelButton.setEnabled(bo);
    }

    String getMethodChoice() {
        return (String) m_cMethodChoice.getSelectedItem();
    }

    double getRate() {
        return Double.parseDouble(m_cTestRate.getText().trim());
    }

    int getDegree() {
        return Integer.parseInt(m_cPolyDegree.getText().trim());
    }

    int getOptimization() {
        if (m_cUseFastMethod.isSelected()) {
            return 1;
        } else if (m_cAOptimization.isSelected()) {
            return 2;
        } else if (m_cDOptimization.isSelected()) {
            return 3;
        }
        return 0;
    }

    public boolean getFullExpansion() {
        return m_cFullExpansion.isSelected();
    }
}

final class clSMKnotControls extends JPanel {
    private final JCheckBox m_cActiveSubscription;

    clSMKnotControls(final ActionListener parentListener) {
        super(new GridLayout(1, 1, 0, 0));
        final JPanel p1 = new JPanel(new BorderLayout());
        p1.add(m_cActiveSubscription = new JCheckBox(StringResources.get(StringResources.showsubscripts), false), BorderLayout.EAST);
        p1.add(new JLabel(StringResources.get(StringResources.usingknots)), BorderLayout.WEST);
        m_cActiveSubscription.addActionListener(parentListener);
        add(p1);
    }

    public boolean getSubscription() {
        return m_cActiveSubscription.isSelected();
    }
}

final class clSMDataControls extends JPanel {
    private final JCheckBox m_cActiveSubscription;
    private final JCheckBox m_cActiveTestRate;
    private final JTextField m_cActiveCurve;
    private final JButton m_cShowButton;
    private final JButton m_cClearButton;

    clSMDataControls(final ActionListener parentListener) {
        super(new GridLayout(1, 1, 0, 0));

        final JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p1.add(new JLabel(StringResources.get(StringResources.curvenumber)));
        p1.add(m_cActiveCurve = new JTextField("1", 3));
        p1.add(m_cShowButton = new JButton(StringResources.get(StringResources.showcurve)));
        p1.add(m_cClearButton = new JButton(StringResources.get(StringResources.clearcurve)));

        p1.add(m_cActiveSubscription = new JCheckBox(StringResources.get(StringResources.showsubscripts), false));
        m_cActiveSubscription.addActionListener(parentListener);
        p1.add(m_cActiveTestRate = new JCheckBox(StringResources.get(StringResources.showtestrate), false));
        m_cActiveTestRate.addActionListener(parentListener);
        add(p1);

        m_cShowButton.addActionListener(parentListener);
        m_cClearButton.addActionListener(parentListener);
    }

    void enableControls(final boolean bo) {
        m_cShowButton.setEnabled(bo);
        m_cClearButton.setEnabled(bo);
    }

    boolean getSubscription() {
        return m_cActiveSubscription.isSelected();
    }

    boolean getActiveTestRate() {
        return m_cActiveTestRate.isSelected();
    }

    String getText() {
        return m_cActiveCurve.getText();
    }
}

