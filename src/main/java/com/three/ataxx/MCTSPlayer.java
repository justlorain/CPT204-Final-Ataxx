package com.three.ataxx;

import java.util.ArrayList;
import java.util.Random;

class MCTSPlayer extends Player {
    private static final int MAX_ITERATIONS = 1000;
    private Move lastFoundMove;

    MCTSPlayer(Game game, PieceState myColor, long seed) {
        super(game, myColor);
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

    /***
     * 进行指定次数的MCTS迭代（可以基于时间，但在这里我们用固定迭代次数来简化），
     * 然后选择访问次数最多的子节点的移动作为最佳移动。
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
     * 执行MCTS的四个阶段：选择、扩展、模拟和反向传播。
     * 在选择阶段，我们会选择UCT值最大的未完全扩展的子节点。
     * 在扩展阶段，我们会对选择阶段选中的节点进行扩展，生成一个新的子节点。
     * 在模拟阶段，我们会随机模拟游戏直到结束，然后得到模拟结果。
     * 在反向传播阶段，我们会根据模拟结果更新节点的访问次数和胜利次数。
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
     * 在选择阶段，我们需要使用一种策略来从根节点开始，选择一个未完全扩展或者没有子节点的节点。
     * 在这里，我们使用Upper Confidence Bound 1 applied to Trees (UCT) 策略。
     * UCT策略选择UCT值最大的子节点，其中UCT值由节点的胜率和访问次数计算得到。
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
     * 在扩展阶段，我们会在选择的节点上添加一个新的子节点。新的子节点代表一个合法的且从未尝试过的移动。
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

    /***
     * 模拟也被称为rollout，这个过程从一个节点的状态开始，并随机地选择一个合法的行动，直到达到一个终止状态，即游戏结束。
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

    /***
     * 在反向传播过程中，
     * 需要更新所选路径中的所有节点的访问次数和获胜次数。
     * 如果模拟的结果是当前节点的玩家胜利，我们就增加该节点的获胜次数。
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
