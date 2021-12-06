package gui;

import control.Server;
import experiment.Experiment;
import experiment.Trial;
import tools.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static tools.Consts.STRINGS.MODE;

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
    private VTScrollPane vtScrollPane;
    private HorizontalScrollPane hzScrollPane;
    private TDScrollPane tdScrollPane;
    private JLabel label;

    // Experiment
    private Point panePosition; // Position of the scrolling pane
    private int blockNum = 1; // Round = 2 blocks
    private int trialNum = 1;
    private boolean isPaneSet;
    private int targetColNum, randColNum;
    private int targetLineNum, randLineNum;
    private boolean isScrollingEnabled = false;
    private int frameSize = 3;

    // TEMP
    Point panePos = new Point();
    Dimension paneDim = new Dimension();
    int hlR = 67;
    int hlC = 100;
    boolean isVt;

    // -------------------------------------------------------------------------------------------
    private final Action nextTrial = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int nTrials = experiment.getRound(blockNum).getNTrials();
            if (trialNum < nTrials) {
                trialNum++;
                trial = experiment.getRound(blockNum).getTrial(trialNum);
            } else {
                removeAll();
                label = new JLabel("Thank you for your participation!");
                add(label, 0);
            }
        }
    };

    private final Action randomTrial = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            removeAll();
            // Get a random-generated trial from experiment and show it
            if (isVt) {
                trial = experiment.randVtTrial();
                isVt = false;
            } else {
                trial = experiment.randTdTrial();
                isVt = true;
            }
            showTrial();
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
        getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_SPACE, 0, true),
                "SPACE");
//        getActionMap().put("SPACE", nextTrial);
        getActionMap().put("SPACE", randomTrial);

        // Add the start label
        label = new JLabel("Press SPACE to start the experiment", JLabel.CENTER);
        label.setFont(new Font("Sans", Font.BOLD, 35));
        label.setBounds(850, 500, 1000, 400);
        add(label, 0);

        // Create the instances of each scrollPane
        vtScrollPane = new VTScrollPane(experiment.VT_PANE_DIM_mm)
                .setText("lorem.txt", experiment.VT_WRAP_CHARS_COUNT, Consts.FONTS.TEXT_BODY_FONT_SIZE)
                .setScrollBar(experiment.VT_SCROLL_BAR_W_mm, experiment.VT_SCROLL_THUMB_H_mm)
                .setLineNums(experiment.VT_LINENUMS_W_mm, Consts.FONTS.LINE_NUM_FONT_SIZE)
                .create();

        tdScrollPane = new TDScrollPane(
                experiment.TD_N_VIS_ROWS,
                experiment.TD_CELL_SIZE_mm,
                experiment.TD_SCROLL_BAR_W_mm)
                .setTable(experiment.TD_N_ROWS)
                .setScrollBars(
                        experiment.TD_SCROLL_BAR_W_mm,
                        experiment.TD_SCROLL_THUMB_L_mm);

//        repaint();


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

                    Rectangle rect = table.getCellRect(1, 13, true);
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
//                    tdScrollPane.getVerticalScrollBar().setValue(1800);
                    int cellSize = Utils.mm2px(experiment.TD_CELL_SIZE_mm);
                    int frOffset = (experiment.TD_N_VIS_ROWS - frameSize) / 2;
                    int frStartInd = frOffset + 1;
                    int frEndInd = frOffset + frameSize;
                    int maxNCells = frOffset + frameSize - 1;
                    int minNCelss = frOffset;
                    tdScrollPane.getHorizontalScrollBar().setValue(
                            (hlC - maxNCells) * cellSize);
//                    tdScrollPane.getVerticalScrollBar().setValue(
//                            (hlR - maxNCells) * cellSize);
//                    tdScrollPane.getVerticalScrollBar().setValue((hlR - frOffset) * cellSize);
                    Logs.d(TAG, hlR, frOffset, cellSize, (hlR - frOffset) * cellSize);
                    tdScrollPane.getVerticalScrollBar().setValue((hlR - (frameSize - 1) - frOffset) * cellSize);
                }

                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    scroll(0, -10);
                }

                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    scroll(0, 10);
                }

                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    scroll(10, 0);
                }

                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    scroll(-10, 0);
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

    }

    /**
     * Filled later...
     */
    private void showTrial() {
        String TAG = NAME + "showTrial";
        Logs.d(TAG, "Mode", trial.scrollMode().toString());
        // Send the mode to Moose
//        Server.get().send(new Memo(MODE, trial.scrollMode().toString(), 0, 0));

        switch (trial.scrollMode()) {
            case VERTICAL -> {
                paneDim = vtScrollPane.getPreferredSize();
                panePos = getRandPosition(paneDim);

                vtScrollPane.setBounds(panePos.x, panePos.y, paneDim.width, paneDim.height);
                vtScrollPane.highlight(randVtLineInd());

                add(vtScrollPane, 0);
            }
            case TWO_DIM -> {
                paneDim = tdScrollPane.getPreferredSize();
                panePos = getRandPosition(paneDim);

                tdScrollPane.setBounds(panePos.x, panePos.y, paneDim.width, paneDim.height);
                tdScrollPane.highlight(hlR, hlC, frameSize);

                add(tdScrollPane, 0);
            }
        }

        repaint();
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

        int midY = (getHeight() - paneDim.height) / 2;
//        int minY = tbMargin;
//        int maxY = getHeight() - (tbMargin + paneDim.height);

        if (minX >= maxX) return new Point(); // Invalid dimensions
        else return new Point(Utils.randInt(minX, maxX), midY);
    }

    /**
     * Get a random line index
     * NOTE: Indexes start from 1
     * @return A random line index
     */
    private int randVtLineInd() {
        String TAG = NAME + "randVtScrollValue";
        int offset = (vtScrollPane.getNVisibleLines() - trial.frame()) / 2;
        int minInd = offset + 1;
        int maxInd = vtScrollPane.getNLines() - offset;
        Logs.d(TAG, "values", minInd, maxInd);
        return Utils.randInt(minInd, maxInd);
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
        Logs.d(TAG, vtSBMinMax);
        // ATTENTION: minimum = 0
        int rowScrollValue = vtSBMinMax.getSecond() / (nRows - nVisibleRows); // How much value for a row scroll?
        Logs.d(TAG, rowScrollValue);
        // Min/max of frame (rows)
        Pair<Integer, Integer> frMinMax = Pair.of(
                (nVisibleRows / 2) - (frLen / 2),
                (nVisibleRows / 2) + (frLen / 2));
        Logs.d(TAG, frMinMax);
        // Vertical target values
        result.setFirst(tgRow - frMinMax.getSecond() * rowScrollValue);
        result.setSecond(tgRow + frMinMax.getFirst() * rowScrollValue);

        return result;

    }

    /**
     * Scroll the 2D scrollPane
     * @param vtScrollMM Vertical scroll amount (mm)
     * @param hzScrollMM Horizontal scroll amount (mm)
     */
    public void scroll(double vtScrollMM, double hzScrollMM) {
        String TAG = NAME + "scroll";

        boolean isScrolled = false;

        int vtScrollAmt = Utils.mm2px(vtScrollMM);
        int hzScrollAmt = Utils.mm2px(hzScrollMM);
        Logs.d(TAG, "Scrolling", vtScrollMM, hzScrollMM);

        switch (trial.scrollMode()) {
        case VERTICAL -> {
            isScrolled = vtScrollPane.scroll(vtScrollAmt);

            if (isScrolled) vtScrollPane.revalidate();
        }
        case TWO_DIM -> {
            Dimension vpDim = tdScrollPane.getViewport().getView().getSize(); // Can be also Preferred
            int vtSBExtent = tdScrollPane.getVerticalScrollBar().getModel().getExtent();
            int hzSBExtent = tdScrollPane.getHorizontalScrollBar().getModel().getExtent();

            // Scroll only if inside the limits
            Point vpPos = tdScrollPane.getViewport().getViewPosition();
            int newX = vpPos.x + hzScrollAmt;
            if (newX != vpPos.x && newX >= 0 && newX <= (vpDim.width - hzSBExtent)) {
                Logs.d(TAG, "NewX", newX);
                tdScrollPane.getViewport().setViewPosition(new Point(newX, vpPos.y));
                isScrolled = true;
            }

            int newY = vpPos.y + vtScrollAmt;
            vpPos = tdScrollPane.getViewport().getViewPosition();
            if (newY != vpPos.y && newY >= 0 && newY <= (vpDim.height - vtSBExtent)) {
                tdScrollPane.getViewport().setViewPosition(new Point(vpPos.x, newY));
                isScrolled = true;
            }

            if (isScrolled)  tdScrollPane.revalidate();
        }
        }


    }

    public void stopScroll() {
        Logs.d(NAME, "stopScroll", 0);
        invalidate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        String TAG = NAME + "paintComponent";
        super.paintComponent(g);
        Logs.info(TAG, "Painting...");
        Graphics2D g2d = (Graphics2D) g;

        // Draw frames
        if (trial != null) {
            int frameH = Utils.mm2px(experiment.TD_FRAME_H_mm);

            switch (trial.scrollMode()) {
            case VERTICAL -> {
                int lineH = vtScrollPane.getLineHeight();
                Logs.d(TAG, "lineH", lineH);
                Rectangle vtFrameRect = new Rectangle();
                vtFrameRect.width = frameH;
                vtFrameRect.height = trial.frame() * lineH;
                vtFrameRect.x = panePos.x - vtFrameRect.width;
                vtFrameRect.y = panePos.y + ((vtScrollPane.getNVisibleLines() - trial.frame()) / 2) * lineH;

                g2d.setColor(Consts.COLORS.CELL_HIGHLIGHT);
                g2d.fillRect(vtFrameRect.x, vtFrameRect.y, vtFrameRect.width, vtFrameRect.height);

            }
            case TWO_DIM -> {
                int cellSize = Utils.mm2px(experiment.TD_CELL_SIZE_mm);

                Rectangle vtFrameRect = new Rectangle();
                vtFrameRect.width = frameH;
                vtFrameRect.height = trial.frame() * cellSize;
                vtFrameRect.x = panePos.x - vtFrameRect.width;
                vtFrameRect.y = panePos.y + ((experiment.TD_N_VIS_ROWS - trial.frame()) / 2) * cellSize;

                Rectangle hzFrameRect = new Rectangle();
                hzFrameRect.width = frameSize * cellSize;
                hzFrameRect.height = frameH;
                hzFrameRect.x = panePos.x + ((experiment.TD_N_VIS_ROWS - trial.frame()) / 2) * cellSize;
                hzFrameRect.y = panePos.y - hzFrameRect.height;

                g2d.setColor(Consts.COLORS.CELL_HIGHLIGHT);
                g2d.fillRect(vtFrameRect.x, vtFrameRect.y, vtFrameRect.width, vtFrameRect.height);
                g2d.fillRect(hzFrameRect.x, hzFrameRect.y, hzFrameRect.width, hzFrameRect.height);
            }
            }
        }

    }
}
