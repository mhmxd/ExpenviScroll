package gui;

import experiment.Experiment;
import experiment.Trial;
import lombok.extern.java.Log;
import tools.Consts;
import tools.Logs;
import tools.Pair;
import tools.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ExperimentPanel extends JLayeredPane {

    private final static String NAME = "ExperimentPanel/";
    // -------------------------------------------------------------------------------------------

    // Constants
    private double LR_MARGIN_mm = 20; // (mm) Left-right margin
    private double TB_MARGIN_mm = 20; // (mm) Left-right margin

    // Experiment and trial
    private Experiment experiment;
    private Trial trial;

    // Elements
    private VerticalScrollPane vtScrollPane;
    private HorizontalScrollPane hzScrollPane;
    private TDScrollPane tdScrollPane;
    private JLabel label;

    // Experiment
    private Point panePosition; // Position of the scrolling pane
    private int blockNum = 1;
    private int trialNum = 0; // Round = 2 blocks
    private boolean isPaneSet;
    private int targetColNum, randColNum;
    private int targetLineNum, randLineNum;
    private int vtLineH;
    private boolean isScrollingEnabled = false;

    // TEMP
    Point pos = new Point(500, 300);

    // -------------------------------------------------------------------------------------------
    private final Action nextTrial = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int nTrials = experiment.getRound(blockNum).getNTrials();
            if (trialNum < nTrials) {
                trialNum++;
                trial = experiment.getRound(blockNum).getTrial(trialNum);
//                label.setBounds(850, label.getY() + 100, 1000, 400);
//

            } else {
//                removeAll();
//                label = new JLabel("Thank you for your participation!");
//                add(label, 0);
            }

//            repaint();
        }
    };

    private final Action enableScrolling = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            hzScrollPane.setWheelScrollingEnabled(enabled);
            Logs.infoAll(NAME, "Scrolling enabled");
        }
    };

    // -------------------------------------------------------------------------------------------

    /**
     * Create the panel
     *
     * @param exp Experiment to show
     */
    public ExperimentPanel(Experiment exp) {
        String TAG = NAME;
        setLayout(null);

        // Set the experiment
        experiment = exp;

        // Map the keys
        getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true),
                "SPACE");
        getActionMap().put("SPACE", nextTrial);

        // Add the start label
//        label = new JLabel("Press SPACE to start the experiment", JLabel.CENTER);
//        label.setFont(new Font("Sans", Font.BOLD, 35));
//        label.setBounds(850, 500, 1000, 400);
//        add(label, 0);

        tdScrollPane = new TDScrollPane(
                experiment.TD_N_VIS_ROWS,
                experiment.TD_CELL_SIZE_mm,
                experiment.TD_SCROLL_BAR_W_mm)
                .setTable(experiment.TD_N_ROWS)
                .setScrollBars(
                            experiment.TD_SCROLL_BAR_W_mm,
                            experiment.TD_SCROLL_THUMB_L_mm);

        Dimension d = tdScrollPane.getPreferredSize();
        tdScrollPane.setBounds(pos.x, pos.y, d.width, d.height);
        tdScrollPane.highlight(75, 100);
//        DefaultBoundedRangeModel dbm = new DefaultBoundedRangeModel();
//        dbm.setRangeProperties(0, 10, 0, 100, true);
//        tdScrollPane.getVerticalScrollBar().setModel(dbm);
        add(tdScrollPane, 1);

        // [FOR TEST]
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    isScrollingEnabled = true;
                }

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    JTable table = (JTable) tdScrollPane.getViewport().getView();
//                    ((JTable) tdScrollPane.getViewport().getView()).scrollRectToVisible(
//                            table.getCellRect(180, 100, true)
//                    );

                    Rectangle rect = table.getCellRect(75, 100, true);
                    Logs.info(TAG, "CellRect= " + rect);
                    Rectangle viewRect = tdScrollPane.getViewport().getViewRect();

//                    rect.setLocation(rect.x - viewRect.x, rect.y - viewRect.y);

                    int centerX = (viewRect.width - rect.width) / 2;
                    int centerY = (viewRect.height - rect.height) / 2;
                    if (rect.x < centerX) {
                        centerX = -centerX;
                    }
                    if (rect.y < centerY) {
                        centerY = -centerY;
                    }

//                    rect.translate(centerX, centerY);
//                    tdScrollPane.getViewport().scrollRectToVisible(rect);
                    tdScrollPane.getVerticalScrollBar().setValue(1800);
                    tdScrollPane.getHorizontalScrollBar().setValue(4000 - 9*40);
                    Logs.info(TAG, "VtValue= " + tdScrollPane.getVerticalScrollBar().getValue());
                    Logs.info(TAG, "HzValue= " + tdScrollPane.getHorizontalScrollBar().getValue());
                }

                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    scroll(Pair.of(0, Utils.mm2px(-10)));
                }

                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    scroll(Pair.of(0, Utils.mm2px(10)));
                }

                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    scroll(Pair.of(Utils.mm2px(10), 0));
                }

                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    scroll(Pair.of(Utils.mm2px(-10), 0));
                }

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    isScrollingEnabled = false;
                }
            }
        });

        addMouseWheelListener(e -> {
            // Do nothing!
        });

        // Testing target values
        Logs.info(TAG, "max = " + tdScrollPane.getVerticalScrollBar().getMaximum());
        SwingUtilities.invokeLater(() -> {
            Pair<Integer, Integer> vtTgMinMax = getVtTargetRange(5, 130);
            Logs.info(TAG, vtTgMinMax);
        });



    }


    /**
     * Generate a random position for a pane
     * Based on the size and dimensions of the displace area
     * @param paneDim Dimension of the pane
     * @return A random position
     */
    private Point getRandPosition(Dimension paneDim) {
        String TAG = NAME + "randPosition";

        int lrMargin = Utils.mm2px(LR_MARGIN_mm);
        int tbMargin = Utils.mm2px(TB_MARGIN_mm);

        int minX = lrMargin;
        int maxX = getWidth() - (lrMargin + paneDim.width);

        int minY = tbMargin;
        int maxY = getHeight() - (tbMargin + paneDim.height);

        if (minX >= maxX || minY >= maxY) return new Point();
        else return new Point(Utils.randInt(minX, maxX), Utils.randInt(minY, maxY));
    }

    /**
     * Get the min,max scroll value for the target
     * @param targetIndex Target column/line number (from 1) (!) Should ALWAYS be > offset!
     * @return Two values (sroll pixels) indicating range (x = min, y = max)
     */
    private Point getTargetRange(int targetIndex) {
        String TAG = NAME + "getTargetRange";

        Point result = new Point();

        switch (trial.scrollMode) {
        case VERTICAL -> {
            int nVisibleLines = experiment.VT_N_VISIBLE_LINES;
            vtLineH = vtScrollPane.getHeight() / nVisibleLines;

            // Number of lines to each side of the (centered) frame
            int offset = (nVisibleLines - trial.frameSize) / 2;

            if (targetIndex > nVisibleLines) {
                result.x = ((targetIndex - nVisibleLines) + offset + 1) * vtLineH;
                result.y = result.x + (trial.frameSize * vtLineH);
            } else if (offset < targetIndex && targetIndex <= (trial.frameSize + offset)) {
                result.x = 0;
                result.y = ((trial.frameSize + offset) - targetIndex) * vtLineH;
            }
        }
        case HORIZONTAL -> {
            int nVisibleCols = experiment.HZ_N_VISIBLE_COLS;
            int colW = hzScrollPane.getColWidth();

            // Number of cols/lines to each side of the (centered) frame
            int offset = (nVisibleCols - trial.frameSize) / 2;

            if (targetIndex > nVisibleCols) {
                result.x = ((targetIndex - nVisibleCols) + offset + 1) * colW;
                result.y = result.x + (trial.frameSize * colW);
            } else if (offset < targetIndex && targetIndex <= (trial.frameSize + offset)) {
                result.x = 0;
                result.y = ((trial.frameSize + offset) - targetIndex) * colW;
            }
        }
        }

        return result;
    }

    /**
     * Get the vertical scroll bar values corresponding to a target cell
     * @param frLen Length of the frame (always centered)
     * @param tgRow Target row {fRMin <= tgRow <= (TNR - VNR) + fRMax)}
     * @return Pair (min, max)
     */
    private Pair<Integer, Integer> getVtTargetRange(int frLen, int tgRow) {
        String TAG = NAME + "getVtTargetRange";

        Pair<Integer, Integer> result = Pair.of(0,0);

        int nRows = experiment.TD_N_ROWS;
        int nVisibleRows = experiment.TD_N_VIS_ROWS;
        Pair<Integer, Integer> vtSBMinMax = Pair.of(
                tdScrollPane.getVerticalScrollBar().getMinimum(),
                tdScrollPane.getVerticalScrollBar().getMaximum());
        Logs.info(TAG, vtSBMinMax);
        // ATTENTION: minimum = 0
        int rowScrollValue = vtSBMinMax.getSecond() / (nRows - nVisibleRows); // How much value for a row scroll?
        Logs.info(TAG, rowScrollValue);
        // Min/max of frame (rows)
        Pair<Integer, Integer> frMinMax = Pair.of(
                (nVisibleRows / 2) - (frLen / 2),
                (nVisibleRows / 2) + (frLen / 2));
        Logs.info(TAG, frMinMax);
        // Vertical target values
        result.setFirst(tgRow - frMinMax.getSecond() * rowScrollValue);
        result.setSecond(tgRow + frMinMax.getFirst() * rowScrollValue);

        return result;

    }

    /**
     * Get the vertical scroll bar values corresponding to a target cell
     * @param frLen Length of the frame (always centered)
     * @param tgCol Target column {fCMin <= tgCol <= (TNC - VNC) + fCMax}
     * @return Pair (min, max)
     */
    private Pair<Integer, Integer> getHzTargetRange(int frLen, int tgCol) {
        String TAG = NAME + "getHzTargetRange";

        Pair<Integer, Integer> result = Pair.of(0,0);

        int nCols = experiment.TD_N_ROWS;
        int nVisibleCols = experiment.TD_N_VIS_ROWS;
        Pair<Integer, Integer> hzSBMinMax = Pair.of(
                tdScrollPane.getHorizontalScrollBar().getMinimum(),
                tdScrollPane.getHorizontalScrollBar().getMaximum());

        // ATTENTION: minimum = 0
        int colScrollValue = hzSBMinMax.getSecond() / (nCols - nVisibleCols); // How much value for a row scroll?

        // Min/max of frame (rows)
        Pair<Integer, Integer> frMinMax = Pair.of(
                (nVisibleCols / 2) - (frLen / 2),
                (nVisibleCols / 2) + (frLen / 2));

        // Vertical target values
        result.setFirst(tgCol - frMinMax.getSecond() * colScrollValue);
        result.setSecond(tgCol + frMinMax.getFirst() * colScrollValue);

        return result;

    }

    /**
     * Scroll the 2D scrollPane
     * @param scrollAmt2D Amount of scrolling (dX, dY)
     */
    public void scroll(Pair<Integer, Integer> scrollAmt2D) {
        String TAG = NAME + "scroll";

//        if (trial != null) {
//            switch (trial.scrollMode) {
//            case VERTICAL -> {
//                int currentValue = vtScrollPane.getVerticalScrollBar().getValue();
//                vtScrollPane.getVerticalScrollBar().setValue(currentValue + delta);
//            }
//            case HORIZONTAL -> {
//                int currentValue = hzScrollPane.getHorizontalScrollBar().getValue();
//                hzScrollPane.getHorizontalScrollBar().setValue(currentValue + delta);
//            }
//            }
//        }

        Logs.info(TAG, "Scrolling: " + scrollAmt2D);

        Point vpPos = tdScrollPane.getViewport().getViewPosition();
        Dimension vpDim = tdScrollPane.getViewport().getView().getSize(); // Can be also Preferred
        int vtSBExtent = tdScrollPane.getVerticalScrollBar().getModel().getExtent();
        int hzSBExtent = tdScrollPane.getHorizontalScrollBar().getModel().getExtent();

        Logs.info(TAG, "vpPos= " + vpPos);
        Logs.info(TAG, "vpDim= " + vpDim);
        Logs.info(TAG, "Extent= " + hzSBExtent);
        // Scroll only if inside the limits
        int newX = vpPos.x + scrollAmt2D.getFirst();
        int newY = vpPos.y + scrollAmt2D.getSecond();
        if (newX != vpPos.x && newX >= 0 && newX <= (vpDim.width - hzSBExtent)) {
            Logs.info(TAG, "NewX= " + newX);
            tdScrollPane.getViewport().setViewPosition(new Point(newX, vpPos.y));
        }
        if (newY != vpPos.y && newY >= 0 && newY <= (vpDim.height - vtSBExtent)) {
            tdScrollPane.getViewport().setViewPosition(new Point(vpPos.x, newY));
        }


        tdScrollPane.invalidate();

    }

    @Override
    protected void paintComponent(Graphics g) {
        String TAG = NAME + "paintComponent";
        super.paintComponent(g);
        Logs.info(TAG, "Painting...");
        Graphics2D g2d = (Graphics2D) g;

        Rectangle vFrameRect = new Rectangle();
        vFrameRect.width = 30;
        vFrameRect.height = 5 * tdScrollPane.getRowH();
        vFrameRect.x = pos.x - vFrameRect.width;
        vFrameRect.y = pos.y + ((tdScrollPane.getHeight() - vFrameRect.height) / 2);

        g2d.setColor(Consts.COLORS.LINE_COL_HIGHLIGHT);
        g2d.fillRect(vFrameRect.x, vFrameRect.y, vFrameRect.width, vFrameRect.height);

        Rectangle hFrameRect = new Rectangle();
        hFrameRect.width = 5 * tdScrollPane.getColW();
        hFrameRect.height = 30;
        hFrameRect.x = pos.x + ((tdScrollPane.getWidth() - hFrameRect.width) / 2);
        hFrameRect.y = pos.y - hFrameRect.height;

        g2d.setColor(Consts.COLORS.LINE_COL_HIGHLIGHT);
        g2d.fillRect(hFrameRect.x, hFrameRect.y, hFrameRect.width, hFrameRect.height);
    }
}
