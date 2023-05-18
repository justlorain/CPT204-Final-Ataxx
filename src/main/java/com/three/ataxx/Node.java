package com.three.ataxx;

import java.util.ArrayList;

class Node {
    private Board state;
    private Move move;
    private Node parent;
    private ArrayList<Node> children;
    private int visits;
    private int wins;
    private PieceState player;
    Node(Board state, Move move, Node parent, PieceState player) {
        this.state = new Board(state);
        this.move = move;
        this.parent = parent;
        this.children = new ArrayList<>();
        this.visits = 0;
        this.wins = 0;
        this.player = player;
    }

    /***
     * Update the node with the result of a simulation
     */
    void update(int result) {
        this.visits++;
        this.wins += result;
    }


    boolean isFullyExpanded() {
        return children.size() == possibleMoves(this.state, this.player).size();
    }

    /***
     * 选择并返回UCT（Upper Confidence Bound applied to Trees）值最高的孩子节点。
     * UCT值的计算公式是Wi/Ni + C * sqrt(ln(Np)/Ni)，
     * 其中Wi是该节点的胜利次数，Ni是该节点被访问的次数，Np是父节点被访问的次数，C是一个常数，代表探索与利用之间的权衡。
     */
    Node bestChild() {
        double maxUCT = Double.NEGATIVE_INFINITY;
        Node bestChild = null;

        for (Node child : children) {
            double childUCT = (double) child.wins / child.visits
                    + Math.sqrt(2 * Math.log(this.visits) / child.visits);

            if (childUCT > maxUCT) {
                maxUCT = childUCT;
                bestChild = child;
            }
        }

        return bestChild;
    }


    void addChild(Node child) {
        children.add(child);
    }


    public ArrayList<Move> possibleMoves(Board board, PieceState myColor) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
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

    public ArrayList<Move> assistPossibleMoves(Board board, char row, char col) {
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

    public Board getState() {
        return state;
    }

    public void setState(Board state) {
        this.state = state;
    }

    public Move getMove() {
        return move;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<Node> children) {
        this.children = children;
    }

    public int getVisits() {
        return visits;
    }

    public void setVisits(int visits) {
        this.visits = visits;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public PieceState getPlayer() {
        return player;
    }

    public void setPlayer(PieceState player) {
        this.player = player;
    }

    public boolean hasChildWithMove(Move move) {
        for (Node child : children) {
            if (child.getMove().equals(move)) {
                return true;
            }
        }
        return false;
    }
    public void incrementVisits() {
        this.visits++;
    }
    public void incrementWins() {
        this.wins++;
    }
}
