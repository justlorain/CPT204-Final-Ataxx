/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package com.three.ataxx;


import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.concurrent.ArrayBlockingQueue;

import static com.three.ataxx.PieceState.*;


/**
 *  Widget for displaying an Ataxx board.
 *  游戏面板
 */
class GamePad extends Pad  {

    /** Length of side of one square, in pixels. */
    static final int SQDIM = 50;
    /** Number of squares on a side. */
    static final int SIDE = 7;
    /** Radius of circle representing a piece. */
    static final int PIECE_RADIUS = 15;
    /** Dimension of a block. */
    static final int BLOCK_WIDTH = 40;
    /** Magic number 20. */
    private static final int MAGICNUM20 = 20;
    /** Magic number 25. */
    private static final int MAGICNUM25 = 25;

    /** Color of red pieces. */
    private static final Color RED_COLOR = Color.RED;
    /** Color of blue pieces. */
    private static final Color BLUE_COLOR = Color.BLUE;
    /** Color of painted lines. */
    private static final Color LINE_COLOR = Color.BLACK;
    /** Color of blank squares. */
    private static final Color BLANK_COLOR = Color.WHITE;
    /** Color of selected squared. */
    private static final Color SELECTED_COLOR = new Color(150, 150, 150);
    /** Color of blocks. */
    private static final Color BLOCK_COLOR = Color.BLACK;

    /** Stroke for lines. */
    private static final BasicStroke LINE_STROKE = new BasicStroke(1.0f);
    /** Stroke for blocks. */
    private static final BasicStroke BLOCK_STROKE = new BasicStroke(5.0f);

    /** Dimension of current drawing surface in pixels. */
    private final int dimension;

    /** Model being displayed. */
    private static Board board;

    /** Coordinates of currently selected square, or '\0' if no selection. */
    private char selectedCol, selectedRow;

    /** True iff in block mode. */
    private boolean blockMode;

    /** Destination for commands derived from mouse clicks. */
    private final ArrayBlockingQueue<String> commandQueue;

    /** A new widget sending commands resulting from mouse clicks
     *  to COMMAND_QUEUE. */
    GamePad(ArrayBlockingQueue<String> commandQueue) {
        this.commandQueue = commandQueue;
        setMouseHandler("click", this::handleClick);
        dimension = SQDIM * SIDE;
        blockMode = false;
        setPreferredSize(dimension, dimension);
        setMinimumSize(dimension, dimension);
    }

    /** Indicate that SQ (of the form CR) is selected, or that none is
     *  selected if SQ is null. */
    void selectSquare(String sq) {
        if (sq == null) {
            selectedCol = selectedRow = 0;
        } else {
            selectedCol = sq.charAt(0);
            selectedRow = sq.charAt(1);
        }
        repaint();
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        g.setColor(BLANK_COLOR);
        g.fillRect(0, 0, dimension, dimension);
        for (char r = '7'; r >= '1'; r--) {
            for (char c = 'a'; c <= 'g'; c++) {
                int locRow = ('7' - r) * SQDIM;
                int locCol = (c - 'a') * SQDIM;
                g.setStroke(LINE_STROKE);
                g.setPaint(LINE_COLOR);
                Rectangle square
                        = new Rectangle(locCol, locRow, SQDIM, SQDIM);
                g.draw(square);
                if (selectedCol == c && selectedRow == r) {
                    g.setColor(SELECTED_COLOR);
                    g.fillRect(locCol + 1, locRow + 1,
                            SQDIM - 1, SQDIM - 1);
                }
                if (board.getContent(c, r) == BLUE) {
                    g.setColor(BLUE_COLOR);
                    g.fillOval(locCol + 10, locRow + 10,
                            2 * PIECE_RADIUS, 2 * PIECE_RADIUS);
                } else if (board.getContent(c, r) == RED) {
                    g.setColor(RED_COLOR);
                    g.fillOval(locCol + 10, locRow + 10,
                            2 * PIECE_RADIUS, 2 * PIECE_RADIUS);
                } else if (board.getContent(c, r) == BLOCKED) {
                    drawBlock(g, locCol + MAGICNUM25, locRow + MAGICNUM25);
                }
            }
        }
    }

    /** Draw a block centered at (CX, CY) on G. */
    void drawBlock(Graphics2D g, int cx, int cy) {
        int leftX = cx - MAGICNUM20;
        int upY = cy - MAGICNUM20;
        int rightX = cx + MAGICNUM20;
        int downY = cy + MAGICNUM20;
        g.setStroke(BLOCK_STROKE);
        g.setPaint(BLOCK_COLOR);
        Rectangle square = new Rectangle(leftX, upY, BLOCK_WIDTH, BLOCK_WIDTH);
        g.draw(square);
        g.drawLine(leftX, upY, rightX, downY);
        g.drawLine(leftX, downY, rightX, upY);
    }

    /** Clear selected block, if any, and turn off block mode. */
    void reset() {
        selectedRow = selectedCol = 0;
        setBlockMode(false);
    }

    /** Set block mode on iff ON. */
    void setBlockMode(boolean on) {
        blockMode = on;
    }

    /** Issue move command indicated by mouse-click event WHERE. */
    private void handleClick(String unused, MouseEvent where) {
        int x = where.getX(), y = where.getY();
        char mouseCol, mouseRow;
        if (where.getButton() == MouseEvent.BUTTON1) {
            mouseCol = (char) (x / SQDIM + 'a');
            mouseRow = (char) ((SQDIM * SIDE - y) / SQDIM + '1');
            if (mouseCol >= 'a' && mouseCol <= 'g'
                && mouseRow >= '1' && mouseRow <= '7') {
                if (blockMode) {
                    commandQueue.offer("block " + mouseCol + mouseRow);
                } else {
                    if (selectedCol != 0) {
                        commandQueue.offer(String.valueOf(selectedCol)
                                + selectedRow
                                + '-'
                                + mouseCol
                                + mouseRow);
                        selectedRow = selectedCol = 0;
                    } else {
                        selectedCol = mouseCol;
                        selectedRow = mouseRow;
                    }
                }
            }
        }
        repaint();
    }

    public synchronized void update(Board board) {
        GamePad.board = new Board(board);
        repaint();
    }

}
