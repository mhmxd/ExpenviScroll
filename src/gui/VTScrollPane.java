package gui;

import control.Logger;
import experiment.Experiment;
import tools.*;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static tools.Consts.*;
import static experiment.Experiment.*;

public class VTScrollPane extends JScrollPane implements MouseListener, MouseWheelListener {
    private final static String NAME = "VTScrollPane/";
    //-------------------------------------------------------------------------------------------------
    public static final int WRAP_CHARS_COUNT = 70;
//    private final int WRAP_CHARS_COUNT = 67;
    private final String WRAPPED_FILE_NAME = "./res/wrapped.txt";

    private final Dimension mDim; // in px
    private ArrayList<Integer> mLCharCountInLines = new ArrayList<>();

    private JTextPane mLinesTextPane;
    private JTextPane mBodyTextPane;
    private MyScrollBarUI mScrollBarUI;

    private MinMax mTargetMinMax = new MinMax();
    private int mNumLines;

    private boolean mCursorIn;

    // For logging
    private Logger.InstantInfo mInstantInfo = new Logger.InstantInfo();
    private boolean mEntered;
    private boolean mScrolled;

    //-------------------------------------------------------------------------------------------------

    /**
     * Constructor
     * @param ddMM Dimention of scroll pane (W/H in mm)
     */
    public VTScrollPane(DimensionD ddMM) {
        mDim = new Dimension(Utils.mm2px(ddMM.getWidth()), Utils.mm2px(ddMM.getHeight()));
        setPreferredSize(mDim);
    }

    public VTScrollPane(Dimension d) {
        mDim = d;
        setPreferredSize(mDim);

        setBorder(BorderFactory.createLineBorder(COLORS.VIEW_BORDER));
    }

    /**
     * Set the text file for displayed text
     * @param fileName Name of the file
     * @return Instance
     */
    public VTScrollPane setText(String fileName) {
        String TAG = NAME + "setText";

        try {
            if (!(new File(WRAPPED_FILE_NAME).isFile())) {
                mLCharCountInLines = Utils.wrapFile(
                        fileName,
                        WRAPPED_FILE_NAME, WRAP_CHARS_COUNT);
            } else {
                countLines(WRAPPED_FILE_NAME);
            }

            // Set the number of lines
            mNumLines = mLCharCountInLines.size();

            // Body of text
            mBodyTextPane = new CustomTextPane(false);
            mBodyTextPane.read(new FileReader(WRAPPED_FILE_NAME), "wrapped");
            mBodyTextPane.setEditable(false);
            final Font bodyFont = Consts.FONTS.SF_LIGHT.deriveFont(FONTS.TEXT_FONT_SIZE);
            mBodyTextPane.setFont(bodyFont);
            mBodyTextPane.setSelectionColor(Color.WHITE);

            SimpleAttributeSet bodyStyle = new SimpleAttributeSet();
            StyleConstants.setLineSpacing(bodyStyle,FONTS.TEXT_LINE_SPACING);
//            StyleConstants.setFontSize(bodyStyle, FONTS.TEXT_FONT_SIZE_INT);
//            StyleConstants.setFontFamily(bodyStyle, Font.SANS_SERIF);

            final int len = mBodyTextPane.getStyledDocument().getLength();
            mBodyTextPane.getStyledDocument().setParagraphAttributes(0, len, bodyStyle, false);

            getViewport().add(mBodyTextPane);

        } catch (IOException e) {
            Logs.d(TAG, "Problem createing VTScrollPane -> setText");
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set the line numbers (H is the same as the scroll pane)
     * @param lineNumsPaneW Width of the line num pane (mm)
     * @return Current instance
     */
    public VTScrollPane setLineNums(double lineNumsPaneW) {

        // Set dimention
        Dimension lnpDim = new Dimension(Utils.mm2px(lineNumsPaneW), mDim.height);

        // Set up Line numbers
        mLinesTextPane = new JTextPane();
        mLinesTextPane.setPreferredSize(lnpDim);
        mLinesTextPane.setBackground(COLORS.LINE_NUM_BG);
        mLinesTextPane.setEditable(false);
        final Font linesFont = Consts.FONTS.SF_LIGHT
                .deriveFont(FONTS.TEXT_FONT_SIZE)
                .deriveFont(Consts.FONTS.ATTRIB_ITALIC);
        mLinesTextPane.setFont(linesFont);
        mLinesTextPane.setForeground(Color.GRAY);
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setAlignment(attributeSet, StyleConstants.ALIGN_CENTER);
        StyleConstants.setLineSpacing(attributeSet,FONTS.TEXT_LINE_SPACING);
        final int len = mBodyTextPane.getStyledDocument().getLength();
        mLinesTextPane.
                getStyledDocument().
                setParagraphAttributes(0, len, attributeSet, false);

        mLinesTextPane.setText(getLineNumbers(mLCharCountInLines.size()));

        // Show the line nums
        setRowHeaderView(mLinesTextPane);

        return this;
    }

    /**
     * Set the scroll bar
     * @param scrollBarW Scroll bar width (mm)
     * @param thumbH Scroll thumb height (mm)
     * @return Current instance
     */
    public VTScrollPane setScrollBar(double scrollBarW, double thumbH) {
        String TAG = NAME + "setScrollBar";
        // Set dimentions
        Dimension scBarDim = new Dimension(Utils.mm2px(scrollBarW), mDim.height);
//        Dimension scThumbDim = new Dimension(scBarDim.width, Utils.mm2px(thumbH));

        // Verticall scroll bar
        mScrollBarUI = new MyScrollBarUI(
                COLORS.VIEW_BORDER,
                COLORS.SCROLLBAR_TRACK,
                COLORS.SCROLLBAR_THUMB,
                6);
        getVerticalScrollBar().setUI(mScrollBarUI);
        getVerticalScrollBar().setPreferredSize(scBarDim);

        // Scroll thumb
//        UIManager.put("ScrollBar.thumbSize", scThumbDim);

        // Policies
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return this;
    }

    /**
     * Create the pane (last method)
     * @return The VTScrollPane instance
     */
    public VTScrollPane create() {
        getViewport().getView().addMouseListener(this);
        addMouseWheelListener(this);
        setWheelScrollingEnabled(true);
//        mBodyTextPane.addMouseWheelListener(this);
//        getVerticalScrollBar().addMouseWheelListener(this);
        return this;
    }

    /**
     * Set some flags according to the technique
     * @param tech New technique
     */
    public void changeTechnique(TECHNIQUE tech) {
        if (tech == Experiment.TECHNIQUE.MOUSE) {
            setWheelScrollingEnabled(true);
        } else {
            setWheelScrollingEnabled(false);
        }
    }

    /**
     * Highlight a line indicated by targetLineInd
     * @param targetLineInd Index of the line (starting from 1)
     * @param frameSizeLines Size of the frame (in lines)
     */
    public void highlight(int targetLineInd, int frameSizeLines) {
        String TAG = NAME + "highlight";

        // Highlight line
        try {
            int stIndex = 0;
            for (int li = 0; li < targetLineInd; li++) {
                stIndex += mLCharCountInLines.get(li) + 1; // prev. lines + \n
            }
            int endIndex = stIndex + mLCharCountInLines.get(targetLineInd); // highlight the whole line
            Logs.d(TAG, mLCharCountInLines.size(), targetLineInd, frameSizeLines, stIndex, endIndex);
            DefaultHighlightPainter highlighter = new DefaultHighlightPainter(COLORS.CELL_HIGHLIGHT);
            mBodyTextPane.getHighlighter().removeAllHighlights();
            mBodyTextPane.getHighlighter().addHighlight(stIndex, endIndex, highlighter);

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // Indicator
        int nVisibleLines = getNVisibleLines();
        int frOffset = (nVisibleLines - frameSizeLines) / 2;
        int lineH = getLineHeight();

        mTargetMinMax.setMin((targetLineInd - (frameSizeLines - 1) - frOffset) * lineH);
        mTargetMinMax.setMax((targetLineInd - frOffset) * lineH);

        mScrollBarUI.setIndicator(
                COLORS.CELL_HIGHLIGHT,
                mTargetMinMax.getMin(),
                mTargetMinMax.getMax());

        final int targetPos = (targetLineInd - nVisibleLines) * lineH;
        mScrollBarUI.setVtIndicator(COLORS.SCROLLBAR_INDIC, targetPos);

        getVerticalScrollBar().setUI(mScrollBarUI);
        Logs.d(TAG, "Indicator", nVisibleLines, frameSizeLines, frOffset, lineH,
                mTargetMinMax.getMin(), mTargetMinMax.getMax());
    }

    /**
     * Scroll a certain amount
     * @param scrollAmt Amount to scroll (in px)
     */
    public void scroll(int scrollAmt) {
        final String TAG = NAME + "scroll";
        // Scroll only if cursor is inside
        Logs.d(TAG, mCursorIn);
        if (mCursorIn) {
            Dimension vpDim = getViewport().getView().getSize(); // Can be also Preferred
            int extent = getVerticalScrollBar().getModel().getExtent();

            Point vpPos = getViewport().getViewPosition();
            int newY = vpPos.y + scrollAmt;
            if (newY != vpPos.y && newY >= 0 && newY <= (vpDim.height - extent)) {
                getViewport().setViewPosition(new Point(vpPos.x, newY));
            }

            repaint();

            // Log
            if (!mScrolled) {
                mInstantInfo.firstScroll = Utils.nowInMillis();
                mScrolled = true;
            } else {
                mInstantInfo.lastScroll = Utils.nowInMillis();
            }
        }

    }

    /**
     * Put the specified line is in the center of the view
     * @param lineInd Line index (from 0)
     */
    public void centerLine(int lineInd) {
        final String TAG = NAME + "centerLine";

        // Check if the lineInd is in the range (to be able to be centered)
        final int halfViewLines = Experiment.TD_N_VIS_ROWS / 2;
        final int lastCenterLineInd = (mNumLines - Experiment.TD_N_VIS_ROWS) + halfViewLines;
        if (lineInd > halfViewLines && lineInd < lastCenterLineInd) {
            final int newPosY = (lineInd - halfViewLines) * getLineHeight(); // Centering the line

            Point vpPos = getViewport().getViewPosition();
            getViewport().setViewPosition(new Point(vpPos.x, newPosY));
        } else {
            Logs.d(TAG, "Can't center line", lineInd, halfViewLines, lastCenterLineInd);
        }

        repaint();
    }

    /**
     * Count the number of lines and chars in each line
     * Line num = number of \n
     */
    public void countLines(String filePath) {
        try {
            String content = Files.readString(Path.of(filePath));
            String[] lines = content.split("\n");
            for (String line : lines) {
                mLCharCountInLines.add(line.length());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the line numbers to show
     * @param nLines Number of lines
     * @return String of line numbers
     */
    public String getLineNumbers(int nLines) {
        Logs.d(this.getClass().getName(), "Total lines = " + nLines);
        StringBuilder text = new StringBuilder("0" + System.getProperty("line.separator"));
        for(int i = 1; i < nLines + 1; i++){
            text.append(i).append(System.getProperty("line.separator"));
        }
        return text.toString();
    }

    /**
     * Get the number of lines
     * @return Number of lines
     */
    public int getNLines() {
        return mNumLines;
    }

    /**
     * Get the height of one line
     * @return Line height (in px)
     */
    public int getLineHeight() {
        String TAG = NAME + "getLineHeight";
//        Logs.d(TAG, "", getPreferredSize().height, getNVisibleLines());
//        return getPreferredSize().height / getNVisibleLines();
        int bodyPaneH = getViewport().getView().getPreferredSize().height;
        Logs.d(TAG, "", bodyPaneH, mNumLines);
        return bodyPaneH / mNumLines;
    }

    /**
     * Get the number of visible lines
     * @return Number of visible lines
     */
    public int getNVisibleLines() {
        String TAG = NAME + "getNVisibleLines";
        return mDim.height / getLineHeight();
    }

    /**
     * Get the maximum value of the scroll bar
     * @return Maximum scroll value
     */
    public int getMaxScrollVal() {
        return getVerticalScrollBar().getMaximum();
    }

    /**
     * Check if a value is inside the frame
     * @param scrollVal Scroll value
     * @return True/false
     */
    public boolean isInsideFrames(int scrollVal) {
        final String TAG = NAME + "isInsideFrames";
        return mTargetMinMax.isWithin(scrollVal);
    }

    /**
     * Get a random line between the two values
     * @param min Min line index (inclusive)
     * @param max Max line index (exclusive)
     * @return Line number
     */
    public int getRandLine(int min, int max) {
        final String TAG = NAME + "getRandLine";

        int lineInd = 0;
        do {
            lineInd = Utils.randInt(min, max);
        } while (mLCharCountInLines.get(lineInd) == 0);

        return lineInd;
    }

    public void setInstantInfo(Logger.InstantInfo instInfo) {
        mInstantInfo = instInfo;
    }

    /**
     * Get the InstantInfo instance (to continue filling in other classes)
     * @return Logger.InstantInfo
     */
    public Logger.InstantInfo getInstantInfo() {
        mScrolled = false;
        return mInstantInfo;
    }

    // MouseListener ========================================================================================
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mCursorIn = true;
        if (!mEntered) {
            mInstantInfo.firstEntry = Utils.nowInMillis();
            mEntered = true;
        } else {
            mInstantInfo.lastEntry = Utils.nowInMillis();
        }

    }

    @Override
    public void mouseExited(MouseEvent e) {
        mCursorIn = false;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Logs.d(VTScrollPane.NAME, mScrolled);
        if (isWheelScrollingEnabled() && mCursorIn) {
            Logs.d(VTScrollPane.NAME, mScrolled);
            // Log
            if (!mScrolled) {
                mInstantInfo.firstScroll = Utils.nowInMillis();
                mScrolled = true;
            } else {
                mInstantInfo.lastScroll = Utils.nowInMillis();
            }
        }
    }

}
