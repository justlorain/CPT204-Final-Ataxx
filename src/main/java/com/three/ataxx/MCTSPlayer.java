package com.three.ataxx;

import java.util.ArrayList;
import java.util.Random;

class MCTSPlayer extends Player {
    private static final int MAX_ITERATIONS = 1000;
    private Move lastFoundMove;

    /**
     * Constructor for MCTSPlayer
     * @param game The game
     * @param myColor The color of the player
     * @param seed The seed for the random number generator
     */
    MCTSPlayer(Game game, PieceState myColor, long seed) {
        super(game, myColor);
    }

    /**
     * Whether the player is auto
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
     * Do a specified number of MCTS iterations (can be based on time, but here we simplify with a fixed number of iterations),
     * Then the move of the child node with the most visits is selected as the best move.
     * @return The best move
     */
    private Move findMove() {
        Node root = new Node(getAtaxxGame().getAtaxxBoard(), null, null, getMyState());

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            runMCTS(root);
        }

        Node bestChild = root.bestChild();
        lastFoundMove = bestChild.getMove();
        return lastFoundMove;
    }

    /***
     * Perform the four phases of MCTS: selection, extension, simulation, and back propagation.
     * In the selection phase, we select the incomplete child node with the largest UCT value.
     * In the extension phase, we extend the selected node to generate a new child node.
     * In the simulation phase, we randomly simulate the game until the end, and then we get the simulation results.
     * In the back propagation phase, we will update the number of node visits and victories according to the simulation results.
     * @param root The root node
     */
    private void runMCTS(Node root) {
        // Select
        Node node = select(root);

        // Expand
        assert node != null;
        if (node.getState().getWinner() == null) {
            expand(node);
        }

        // Simulate
        PieceState result = simulate(node);

        // Backpropagation
        backpropagate(node, result);
    }

    /***
     * In the selection phase, we need to use a strategy to select a node that is not fully extended or has no child nodes,
     * starting with the root node.
     * Here, we use the Upper Confidence Bound 1 applied to Trees (UCT) strategy.
     * The UCT policy selects the child node with the largest UCT value, which is calculated by the node's win rate and access times.
     * @param node The node
     * @return The selected node
     */
    private Node select(Node node) {
        // 如果节点没有子节点，直接返回该节点
        if (node.getChildren().isEmpty()) {
            return node;
        }

        Node selected = null;
        double bestValue = Double.MIN_VALUE;
        for (Node child : node.getChildren()) {
            if (child.getVisits() == 0) {
                return child;
            }
            double uctValue = child.getWins() / (double) child.getVisits()
                    + Math.sqrt(2 * Math.log(node.getVisits()) / (double) child.getVisits());
            if (uctValue > bestValue) {
                bestValue = uctValue;
                selected = child;
            }
        }

        // 如果所有子节点的访问次数都是 0，返回一个随机的子节点
        if (selected == null) {
            selected = node.getChildren().get(new Random().nextInt(node.getChildren().size()));
        }

        return selected;
    }


    /***
     * In the extension phase, we add a new child node on the selected node. The new child node represents a legal and unattempted move.
     * @param node The node
     */
    private void expand(Node node) {
        ArrayList<Move> possibleMoves = node.possibleMoves(node.getState(),node.getState().nextMove());
        // 如果所有可能的移动都已经被尝试过，就什么都不做，否则会陷入死循环
        if (possibleMoves.isEmpty()) {
            return;
        }
        for (Move move : possibleMoves) {
            if (!node.hasChildWithMove(move)) { // 如果该节点没有该移动的子节点，就添加一个新的子节点
                Board childState = new Board(node.getState());
                childState.createMove(move);
                node.addChild(new Node(childState, move, node, getMyState()));
            }
        }
    }

    /**
     * Simulation, also known as rollout,
     * is a process that starts with the state of a node and randomly
     * selects a legal action until a termination state is reached,
     * which is the end of the game.
     * @param node The node
     * @return The result of the simulation
     */
    private PieceState simulate(Node node) {
        Board tempNode = new Board(node.getState());
        ArrayList<Move> possibleMoves;

        while (tempNode.getWinner() == null) {
            possibleMoves = node.possibleMoves(tempNode, tempNode.nextMove());
            if (possibleMoves.isEmpty()) {
                // 如果没有合法的移动，直接返回当前状态的赢家
                return tempNode.getWinner();
            }
            Move move = possibleMoves.get((int) (Math.random() * possibleMoves.size()));
            tempNode.createMove(move);
        }

        return tempNode.getWinner();
    }

    /**
     * In the process of back propagation,
     * The number of visits and wins for all nodes in the selected path needs to be updated.
     * If the result of the simulation is a win for the player of the current node, we increase the number of wins for that node.
     * @param node The node
     * @param winner The winner
     */
    private void backpropagate(Node node, PieceState winner) {
        Node tempNode = node;
        while (tempNode != null) {
            tempNode.incrementVisits();
            if (tempNode.getState().nextMove() == winner) {
                tempNode.incrementWins();
            }
            tempNode = tempNode.getParent();
        }
    }

}
