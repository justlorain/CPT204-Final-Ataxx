package com.three.ataxx;

import java.util.ArrayList;

import static java.lang.Math.max;
import static java.lang.Math.min;

// Final Project Part A.2 Ataxx AI Player (A group project)

/** A Player that computes its own moves. */
class AIPlayer extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 5;

    /** A position magnitude indicating a win (for red if positive, blue if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;

    /** A magnitude greater than a normal value. */
    private static final int INFINITY = Integer.MAX_VALUE;

    /** The move found by the last call to the findMove method above. */
    private Move lastFoundMove;

    /**
     * Constructs a new AIPlayer for GAME that will play MYCOLOR.
     * SEED is used to initialize a random-number generator,
	 * increase the value of SEED would make the AIPlayer move automatically.
     * Identical seeds produce identical behaviour.
     *
     * @param game the game
     * @param myColor the color of the player
     * @param seed the seed
     */
    AIPlayer(Game game, PieceState myColor, long seed) {
        super(game, myColor);
    }

    /**
     * Return true iff I am an automated player that automatically
     *
     * @return true iff I am an automated player that automatically
     */
    @Override
    boolean isAuto() {
        return true;
    }

    /**
     * Return my next move from the current position in getAtaxxGame().
     *
     * @return my next move from the current position in getAtaxxGame().
     */
    @Override
    String getAtaxxMove() {
        Move move = findMove();
        getAtaxxGame().reportMove(move, getMyState());
        return move.toString();
    }

    /**
     * Return a move for me from the current position, assuming there is a move.
     * The move's row and column values must be in the range 0..Board.SIDE - 1.
     *
     * @return a move for me from the current position, assuming there is a move.
     */
    private Move findMove() {
        Board b = new Board(getAtaxxBoard());
        lastFoundMove = null;

        minMax(b, MAX_DEPTH, true, -1, -INFINITY, INFINITY);

        // Please do not change the codes below
        if (lastFoundMove == null) {
            lastFoundMove = Move.pass();
        }
        return lastFoundMove;
    }

    /**
     * Return a heuristic value for BOARD.
     *
     * @param board the board
     * @return a heuristic value for BOARD.
     */
    private int staticScore(Board board) {
        PieceState winner = board.getWinner();
        if (winner != null) {
            return switch (winner) {
                case RED -> AIPlayer.WINNING_VALUE; // RED wins
                case BLUE -> -AIPlayer.WINNING_VALUE; // BLUE wins
                default -> 0;
            };
        }
        int totalScore;

        // 计算分差
        int myColor = board.getColorNums(board.nextMove());
        int oppColor = board.getColorNums(board.nextMove().opposite());
        int diff = myColor - oppColor;

//        // 计算期盼中心控制力
//        int myCenterScore = board.CountWeightedScoreByColor(board.nextMove());
//        int oppCenterScore = board.CountWeightedScoreByColor(board.nextMove().opposite());
//        int centerDiff = myCenterScore - oppCenterScore;

        // 计算总分
        // totalScore = diff * 10 + centerDiff * 5 + possibleMoves * 7 + currentDepth;
        totalScore = diff * 10;
        return totalScore;
    }

    /**
     *  Implementation of minMax and Alpha Beta pruning algorithm
     *
     *  @param board the board
     *  @param depth the depth
     *  @param saveMove whether to save the move
     *  @param sense the sense
     *  @param alpha the alpha
     *  @param beta the beta
     *  @return the value
     */
    private int minMax(Board board, int depth, boolean saveMove, int sense, int alpha, int beta) {
        if (depth == 0 || board.getWinner() != null) {
            return staticScore(board);
        }
        int bestValue;
        if (sense == 1) {
            bestValue = -INFINITY;
            ArrayList<Move> listOfMoves = possibleMoves(board, board.nextMove());
            for (Move move : listOfMoves) {
                Board copyBoard = new Board(board); // copy the board each time
                copyBoard.createMove(move);
                int possible = minMax(copyBoard, depth - 1, false, -1, alpha, beta);
                if (saveMove && possible > bestValue) { // save the best move
                    lastFoundMove = move;
                }
                bestValue = max(bestValue, possible); // update the best value
                alpha = max(alpha, bestValue);
                if (beta <= alpha) { // alpha cut-off
                    break;
                }
            }
            if (bestValue == -INFINITY) { // no possible moves
                return 0;
            }
        } else {
            bestValue = INFINITY;
            ArrayList<Move> listOfMoves = possibleMoves(board, board.nextMove());
            for (Move move : listOfMoves) {
                Board copyBoard = new Board(board);
                copyBoard.createMove(move);
                int possible = minMax(copyBoard, depth - 1, false, 1, alpha, beta);
                if (saveMove && possible < bestValue) {
                    lastFoundMove = move;
                }
                bestValue = min(bestValue, possible);
                beta = min(beta, bestValue);
                if (beta <= alpha) { // beta cut-off
                    break;
                }
            }
            if (bestValue == INFINITY) { // no possible moves
                return 0;
            }
        }
        return bestValue;
    }

    /**
     * possibleMoves returns an ArrayList of all possible moves for the specified color.
     *
     * @param board the current board.
     * @param myColor the specified color.
     * @return an ArrayList of all possible moves for the specified color.
     */
    private ArrayList<Move> possibleMoves(Board board, PieceState myColor) {
        ArrayList<Move> possibleMoves = new ArrayList<>();

//        int beginIndex = Board.index('a', '1');
//        int endIndex = Board.index('g', '7');
//        for (int i = beginIndex; i <= endIndex; i++) {
//            if (board.getContent(i) == myColor) {
//                ArrayList<Move> addMoves = assistPossibleMoves(board, i);
//                possibleMoves.addAll(addMoves);
//            }
//        }

        for (char row = '7'; row >= '1'; row--) { // iterate the board
            for (char col = 'a'; col <= 'g'; col++) {
                int index = Board.index(col, row); // get the index
                if (board.getContent(index) == myColor) { // if it is my color then find all possible moves around
                    ArrayList<Move> addMoves = assistPossibleMoves(board, row, col);
                    possibleMoves.addAll(addMoves);
                }
            }
        }
        return possibleMoves;
    }

    /**
     * assist possible moves for a color.
     *
     * @param board the board for testing
     * @param row the row coordinate of the center
     * @param col the col coordinate of the center
     * @return an ArrayList of legal moves.
     */
    private ArrayList<Move> assistPossibleMoves(Board board, char row, char col) {
        ArrayList<Move> assistPossibleMoves = new ArrayList<>();
        for (int i = -2; i <= 2; i++) { // search all possible moves around two steps
            for (int j = -2; j <= 2; j++) {
                if (i != 0 || j != 0) {
                    char row2 = (char) (row + j); // get the row and col of the possible move
                    char col2 = (char) (col + i);
                    Move currMove = Move.move(col, row, col2, row2); // create a move
                    if (board.moveLegal(currMove)) { // if it is legal then add it to the list
                        assistPossibleMoves.add(currMove);
                    }
                }
            }
        }
        return assistPossibleMoves;
    }
}
