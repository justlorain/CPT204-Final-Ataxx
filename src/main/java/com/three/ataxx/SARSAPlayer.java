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

    /**
     * Judge whether the player is auto
     * @return true
     */
    @Override
    boolean isAuto() {
        return true;
    }

    /**
     * Get the move for the player
     * @return The move
     */
    @Override
    String getAtaxxMove() {
        Move move = findMove();
        getAtaxxGame().reportMove(move, getMyState());
        return move.toString();
    }

    /**
     * Return a move for me from the current position, assuming there is a move.
     * @return a move
     */
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

    // helper function for SARSAPlayer
    /**
     * Defines how the agent looks for possible actions
     * @param board the current board
     * @param myColor the color of the player
     * @return a list of possible moves
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

    /**
     * Defines how the agent looks for possible actions
     * @param board the current board
     * @param row the row of the piece
     * @param col the column of the piece
     * @return a list of possible moves
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

    /**
     * The function should evaluate the value of a given action in its current state.
     * In reinforcement learning, this value is usually derived from the Q table,
     * Therefore, the getQValue function may be called.
     * @param board the current board
     * @return the value of the action
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

    /**
     * The function should map a state (in this case, the chessboard) to an index of the Q table.
     * You need to decide how to encode the state of the board into a unique string or number that you can look up in the Q table.
     * @param board the current board
     * @return the index of the Q table
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
     * The function should return Q values for all possible actions in a given state.
     * Use possibleMoves to get all possible moves.
     * Use getQValue to get the Q value of each action.
     * @param board the current board
     * @return a map of moves and their Q values
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


    /**
     * The function should return the Q value of a given action in a given state.
     * @param board the current board
     * @param move the move to be evaluated
     * @return the Q value of the move
     */
    private double getQValue(Board board, Move move) {
        Map<Move, Double> qValues = getQValues(board);
        // If this move is not in the Q values, add it
        // Initialize Q value to 0
        return qValues.computeIfAbsent(move, k -> 0.0);
    }


    /***
     * Update the Q value of a state-action pair in the Q table. You need to implement SARSA's update rule, which is:
     * * Q(s,a) = Q(s,a) + alpha * (reward + gamma * Q(s',a') - Q(s,a))
     * Where 's' is the current state, 'a' is the current action, 'reward' is the reward of the current action, 's' is the next state,
     * 'a' is the action in state 's', 'alpha' is the learning rate, and 'gamma' is the discount factor.
     * @param current the current board
     * @param move the move to be evaluated
     * @param next the next board
     * @param reward the reward of the move
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
     * The function should set the Q value of a state-action pair in the Q table.
     * You need to use the mapStateToIndex function in this function to get the index of the state.
     * @param board the current board
     * @param move the move to be evaluated
     * @param value the new Q value
     */
    private void setQValue(Board board, Move move, double value) {
        Map<Move, Double> qValues = getQValues(board);
        qValues.put(move, value);
    }


    /***
     * The function should calculate the reward for an action.
     * You need to define a reward function that reflects your game strategy.
     * For example, you can give a large positive reward for winning a game,
     * a large negative reward for losing a game,
     * and a small reward or punishment for other actions.
     * @param board the current board
     * @return the reward of the action
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


    /**
     * Used to load and save the Q table.
     * You need to decide how to store Q tables in files.
     * One possible approach is to store the Q table as a CSV file,
     * with each row representing a state-action pair and corresponding Q value.
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

    /**
     * Used to load and save the Q table.
     * @return the loaded Q table
     */
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
