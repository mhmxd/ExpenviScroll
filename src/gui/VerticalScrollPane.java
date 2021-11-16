package gui;

import tools.Consts;
import tools.DimensionD;
import tools.Logs;
import tools.Utils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseWheelListener;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

import static tools.Consts.COLORS;

public class VerticalScrollPane extends JScrollPane {
    private final static String NAME = "VerticalScrollPane";
    //-------------------------------------------------------------------------------------------------

    private final String WRAPPED_FILE_NAME = "./res/wrapped.txt";

    private final Dimension dim; // in px
    private ArrayList<Integer> lineCharCounts = new ArrayList<>();

    private JTextPane linesTextPane;
    private JTextPane bodyTextPane;

    protected int targetMinScVal, targetMaxScVal;
    private int nLines;
    //-------------------------------------------------------------------------------------------------

    /**
     * Constructor
     * @param ddMM Dimention of scroll pane (W/H in mm)
     */
    public VerticalScrollPane(DimensionD ddMM) {
        dim = new Dimension(Utils.mm2px(ddMM.getWidth()), Utils.mm2px(ddMM.getHeight()));
        setPreferredSize(dim);
    }

    /**
     * Set the text file for displayed text
     * @param fileName Name of the file
     * @return Instance
     */
    public VerticalScrollPane setText(String fileName, int wrapCharCount, float bodyFontSize) {
        String TAG = NAME + "setText";

        // Wrap the file and get the char num of each line
        try {
            lineCharCounts = Utils.wrapFile(fileName, WRAPPED_FILE_NAME, wrapCharCount);

            // Body of text
            bodyTextPane = new CustomTextPane(false);
            bodyTextPane.setEditable(false);
            bodyTextPane.setFont(Consts.FONTS.SF_LIGHT.deriveFont(bodyFontSize));
            bodyTextPane.setSelectionColor(Color.WHITE);

            bodyTextPane.read(new FileReader(WRAPPED_FILE_NAME), "wrapped");

        } catch (IOException e) {
            Logs.error(TAG, "Problem createing VerticalScrollPane -> setText");
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set the line numbers (H is the same as the scroll pane)
     * @param lineNumsPaneW Width of the line num pane (mm)
     * @param lineNumsFontSize Font size of the line num pane
     * @return Current instance
     */
    public VerticalScrollPane setLineNums(double lineNumsPaneW, float lineNumsFontSize) {

        // Set dimention
        Dimension lnpDim = new Dimension(Utils.mm2px(lineNumsPaneW), dim.height);

        // Line numbers
        linesTextPane = new JTextPane();
        linesTextPane.setPreferredSize(lnpDim);
        linesTextPane.setBackground(Consts.COLORS.LINE_NUM_BG);
        linesTextPane.setEditable(false);
        Font linesFont = Consts.FONTS.SF_LIGHT
                .deriveFont(lineNumsFontSize)
                .deriveFont(Consts.FONTS.ATTRIB_ITALIC);
        linesTextPane.setFont(linesFont);
        linesTextPane.setForeground(Color.GRAY);
        StyledDocument documentStyle = linesTextPane.getStyledDocument();
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setAlignment(attributeSet, StyleConstants.ALIGN_CENTER);
        documentStyle.setParagraphAttributes(0, documentStyle.getLength(), attributeSet, false);

        linesTextPane.setText(getLineNumbers(lineCharCounts.size()));
        nLines = lineCharCounts.size();

        return this;
    }

    /**
     * Set the scroll bar
     * @param scrollBarW Scroll bar width (mm)
     * @param thumbH Scroll thumb height (mm)
     * @return Current instance
     */
    public VerticalScrollPane setScrollBar(double scrollBarW, double thumbH) {

        // Set dimentions
        Dimension scBarDim = new Dimension(Utils.mm2px(scrollBarW), dim.height);
        Dimension scThumbDim = new Dimension(scBarDim.width, Utils.mm2px(thumbH));

        // Verticall scroll bar
        getVerticalScrollBar().setUI(new CustomVScrollBarUI());
        getVerticalScrollBar().setPreferredSize(scBarDim);

        // Scroll thumb
        UIManager.put("ScrollBar.thumbSize", scThumbDim);

        // Policies
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return this;
    }

    /**
     * Final creation of the component
     * @return Current instance
     */
    public VerticalScrollPane create() {
        getViewport().add(bodyTextPane);
        setRowHeaderView(linesTextPane);

        return this;
    }

    /**
     * Highlight a line
     * @param lineInd Column index number (from 1)
     */
    public void higlight(int lineInd, int tgMinScVl, int tgMaxScVl) throws BadLocationException {
        DefaultTableCellRenderer highlightRenderer = new DefaultTableCellRenderer();
        highlightRenderer.setBackground(Consts.COLORS.LINE_COL_HIGHLIGHT);

        int stIndex = 0;
        for (int li = 0; li < lineInd - 1; li++) {
            stIndex += lineCharCounts.get(li) + 1; // prev. lines + \n
        }
        int endIndex = stIndex + lineCharCounts.get(lineInd - 1);

        DefaultHighlightPainter highlighter = new DefaultHighlightPainter(COLORS.LINE_COL_HIGHLIGHT);
        bodyTextPane.getHighlighter().removeAllHighlights();
        bodyTextPane.getHighlighter().addHighlight(stIndex, endIndex, highlighter);

        // Set min/max
        targetMinScVal = tgMinScVl;
        targetMaxScVal = tgMaxScVl;

        getVerticalScrollBar().revalidate();
    }

    /**
     * Add MouseWheelListener to every component
     * @param mwl MouseWheelListener
     */
    public void addWheelListener(MouseWheelListener mwl) {
        getVerticalScrollBar().addMouseWheelListener(mwl);
        bodyTextPane.addMouseWheelListener(mwl);
        linesTextPane.addMouseWheelListener(mwl);
    }

    /**
     * Get the line numbers to show
     * @param nLines Number of lines
     * @return String of line numbers
     */
    public String getLineNumbers(int nLines) {
        Logs.info(this.getClass().getName(), "Total lines = " + nLines);
        StringBuilder text = new StringBuilder("1" + System.getProperty("line.separator"));
        for(int i = 2; i < nLines + 2; i++){
            text.append(i).append(System.getProperty("line.separator"));
        }
        return text.toString();
    }

    public int getNLines() {
        return nLines;
    }

    //-------------------------------------------------------------------------------------------------

    private class CustomVScrollBarUI extends BasicScrollBarUI {

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(new Color(244, 244, 244));
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g.setColor(Color.BLACK);
            g.drawRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);

            // Highlight scroll bar rect
            double ratio = trackBounds.height / (getVerticalScrollBar().getMaximum() * 1.0);
            int hlY = (int) (targetMinScVal * ratio);
            int hlH = (int) ((targetMaxScVal - targetMinScVal) * ratio) + getThumbBounds().height;
            g.setColor(Consts.COLORS.SCROLLBAR_HIGHLIGHT);
            g.fillRect(trackBounds.x, hlY, trackBounds.width, hlH);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {

            // Set anti-alias
            Graphics2D graphics2D = (Graphics2D) g;
            graphics2D.setColor(Color.BLACK);
            graphics2D.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);


            graphics2D.fillRoundRect(
                    thumbBounds.x + 4, thumbBounds.y,
                    thumbBounds.width - 6, thumbBounds.height,
                    5, 5);
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        protected JButton createZeroButton() {
            JButton button = new JButton("zero button");
            Dimension zeroDim = new Dimension(0,0);
            button.setPreferredSize(zeroDim);
            button.setMinimumSize(zeroDim);
            button.setMaximumSize(zeroDim);
            return button;
        }
    }


}