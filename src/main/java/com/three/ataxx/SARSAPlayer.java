package com.three.ataxx;

import java.io.FileOutputStream;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.*;

// Final Project Part A.2 Ataxx AI Player (A group project)

/** A Player that computes its own moves. */
class SARSAPlayer extends Player { // 这是一个基于强化学习-SARSA算法的AI

    private static final double LEARNING_RATE = 0.5;
    private static final double DISCOUNT_FACTOR = 0.9;
    private static final double EXPLORATION_RATE = 0.1;

    private final Map<String, Map<Move, Double>> qTable;

    private static final String MODEL_FILE_PATH = "src/main/java/com/three/ataxx/Q.dat";

    /** A new AIPlayer for GAME that will play MYCOLOR.
     *  SEED is used to initialize a random-number generator,
     *  increase the value of SEED would make the AIPlayer move automatically.
     *  Identical seeds produce identical behaviour. */
    SARSAPlayer(Game game, PieceState myColor, long seed) {
        super(game, myColor);
        this.qTable = loadModel();
    }

    @Override
    boolean isAuto() {
        return true;
    }

    @Override
    String getAtaxxMove() {
        Move move = findMove();
        getAtaxxGame().reportMove(move, getMyState());
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */

    private Move findMove() {
        Board b = new Board(getAtaxxBoard());
        lastFoundMove = null;

        String state = mapStateToIndex(b);
        Map<Move, Double> qValues = getQValues(b);
        ArrayList<Move> possibleMoves = possibleMoves(b, getMyState());

        // Exploration vs exploitation
        if (Math.random() < EXPLORATION_RATE && !possibleMoves.isEmpty()) {
            // Randomly select a move
            lastFoundMove = possibleMoves.get((int) (Math.random() * possibleMoves.size()));
        } else {
            // Select the move with maximum Q-value
            lastFoundMove = Collections.max(qValues.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey();
        }

        // Update Q-table
        Board nextBoard = new Board(b);
        nextBoard.createMove(lastFoundMove);
        double reward = calculateReward(nextBoard);
        updateQValue(b, lastFoundMove, nextBoard, reward);

        // Please do not change the codes below
        if (lastFoundMove == null) {
            lastFoundMove = Move.pass();
        }
        // If the game is over, save the Q table
        if (nextBoard.getWinner()!= null) { // the game is over
            System.out.println("Game over, saving Q table...");
            saveModel();
        }

        return lastFoundMove;
    }

    /** The move found by the last call to the findMove method above. */
    private Move lastFoundMove;

    // 下面是实现SARSA算法的一些辅助函数
    /***
     * 定义了智能体如何寻找可能的动作。
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
                    ArrayList<Move> addMoves
                            = assistPossibleMoves(board, row, col);
                    possibleMoves.addAll(addMoves);
                }
            }
        }
        return possibleMoves;
    }

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

    /***
     *  函数应该评估一个给定的动作在当前状态下的价值。在强化学习中，这个价值通常是从Q表中获取的，
     *  因此可能会调用getQValue函数。
     * @param board
     * @return
     */
    private double evaluateMove(Board board) {
        // Evaluation function based on the difference in number of pieces
        int myPieces = 0, opponentPieces = 0;
        for (char row = '7'; row >= '1'; row--) {
            for (char col = 'a'; col <= 'g'; col++) {
                int index = Board.index(col, row);
                if (board.getContent(index) == getMyState()) {
                    myPieces++;
                } else if (board.getContent(index) == getMyState().opposite()) {
                    opponentPieces++;
                }
            }
        }
        return myPieces - opponentPieces;
    }

    /***
     *  函数应该将一个状态（在这个情况下是棋盘）映射到Q表的一个索引。
     *  需要决定如何将棋盘的状态编码为一个唯一的字符串或数字，以便在Q表中查找。
     * @param board
     * @return
     */
    private String mapStateToIndex(Board board) {
        StringBuilder sb = new StringBuilder();

        int beginIndex = Board.index('a', '1');
        int endIndex = Board.index('g', '7');

        for (int i = beginIndex; i <= endIndex; i++) {
                sb.append(board.getContent(i).ordinal());
            }
        return sb.toString();
    }


    /***
     * 函数应该返回给定状态下所有可能动作的Q值。
     * 使用possibleMoves函数来获取所有可能的动作。
     * 使用getQValue函数来获取每个动作的Q值。
     * @param board
     * @return
     */
    private Map<Move, Double> getQValues(Board board) {
        String stateIndex = mapStateToIndex(board);
        Map<Move, Double> qValues = qTable.get(stateIndex);
        if (qValues == null) {
            // If this state is not in the Q table, create a new entry
            qValues = new HashMap<>();
            for (Move move : possibleMoves(board, getMyState())) {
                qValues.put(move, 0.0); // Initialize Q values to 0
            }
            qTable.put(stateIndex, qValues);
        }
        return qValues;
    }


    private double getQValue(Board board, Move move) {
        Map<Move, Double> qValues = getQValues(board);
        // If this move is not in the Q values, add it
        // Initialize Q value to 0
        return qValues.computeIfAbsent(move, k -> 0.0);
    }


    /***
     * 更新Q表中一个状态-动作对的Q值。需要实现SARSA的更新规则，这个规则是：
     * Q(s,a) = Q(s,a) + alpha * (reward + gamma * Q(s',a') - Q(s,a))
     * 其中 `s` 是当前状态，`a` 是当前动作，`reward` 是当前动作的奖励，`s`是下一个状态，
     * `a` 是在状态 `s` 下的动作，`alpha` 是学习率，`gamma` 是折扣因子。
     * @param current
     * @param move
     * @param next
     * @param reward
     */
    private void updateQValue(Board current, Move move, Board next, double reward) {
        double maxQ;
        Map<Move, Double> nextQValues = getQValues(next);

        if (qTable.isEmpty() || nextQValues.isEmpty()) {
            maxQ = 0;
        } else {
            maxQ = Collections.max(nextQValues.values());
        }

        double oldQValue = getQValue(current, move);
        double nextMaxQValue = maxQ;
        double newQValue = oldQValue + LEARNING_RATE * (reward + DISCOUNT_FACTOR * nextMaxQValue - oldQValue);
        setQValue(current, move, newQValue);
    }



    /***
     * 函数应该在Q表中设置一个状态-动作对的Q值。
     * 需要在此函数中使用 mapStateToIndex 函数来获取状态的索引。
     * @param board
     * @param move
     * @param value
     */
    private void setQValue(Board board, Move move, double value) {
        Map<Move, Double> qValues = getQValues(board);
        qValues.put(move, value);
    }


    /***
     * 函数应该计算一个动作的奖励。你需要定义一个奖励函数，这个函数应该反映出你的游戏策略。
     * 例如，可以给赢得游戏的动作一个大的正奖励，给输掉游戏的动作一个大的负奖励，给其他动作一个小的奖励或惩罚。
     * @param board
     * @return
     */
    private double calculateReward(Board board) {
        if (board.getWinner()!= null) {
            if (board.getWinner() == getMyState()) {
                return 1.0; // Won the game
            } else {
                return -1.0; // Lost the game
            }
        }
        return 0.0; // The game is not over
    }


    /***
     * 用于加载和保存Q表。
     * 需要决定如何在文件中存储Q表。
     * 一种可能的方式是将Q表存储为一个CSV文件，每一行代表一个状态-动作对和对应的Q值。
     * @return
     */
    void saveModel() {
        try {
            FileOutputStream fileOut = new FileOutputStream(MODEL_FILE_PATH);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(qTable);
            objectOut.close();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    Map<String, Map<Move, Double>> loadModel() {
        try {
            FileInputStream fileIn = new FileInputStream(MODEL_FILE_PATH);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            Map<String, Map<Move, Double>> loadedQTable = (Map<String, Map<Move, Double>>) objectIn.readObject();
            objectIn.close();
            fileIn.close();
            return loadedQTable;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No model found. Starting with a new Q table.");
        }
        return new HashMap<>();
    }

}
