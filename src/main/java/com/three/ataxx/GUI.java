package com.three.ataxx;

// Optional Task: The GUI for the Ataxx Game

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ArrayBlockingQueue;

import static com.three.ataxx.PieceState.*;

class GUI extends TopLevel implements View, CommandSource, Reporter {

    // Complete the codes here
    private static final int MIN_SIZE = 300;

    GUI(String ataxx) {
        super(ataxx, true);
        addMenuButton("Game->New", this::newGame);
        addMenuRadioButton("Game->Blocks->Set Blocks", "Blocks",
                false, this::adjustBlockMode);
        addMenuRadioButton("Game->Blocks->Move Pieces", "Blocks",
                true, this::adjustBlockMode);
        addMenuButton("Game->Quit", this::quit);
        addMenuButton("Options->Seed...", this::setSeed);
        addMenuRadioButton("Options->Players->Red AI", "Red",
                false, (dummy) -> send("auto red"));
        addMenuRadioButton("Options->Players->Red Manual", "Red",
                true, (dummy) -> send("manual red"));
        addMenuRadioButton("Options->Players->Blue AI", "Blue",
                true, (dummy) -> send("auto blue"));
        addMenuRadioButton("Options->Players->Blue Manual", "Blue",
                false, (dummy) -> send("manual blue"));
        addMenuButton("Info->Help", this::doHelp);
        _widget = new BoardWidget(_commandQueue);
        add(_widget,
                new LayoutSpec("height", "1",
                        "width", "REMAINDER",
                        "ileft", 5, "itop", 5, "iright", 5,
                        "ibottom", 5));
        addLabel("Red to move", "State",
                new LayoutSpec("y", 1, "anchor", "west"));
        addButton("Pass", this::doPass, new LayoutSpec("y", "1"));
    }

    // Add some codes here
    /** Execute the "Quit" button function. */
    private synchronized void quit(String unused) {
        send("quit");
    }

    /** Execute the "New Game" button function. */
    private synchronized void newGame(String unused) {
        send("new");
        setEnabled(false, "Game->Blocks->Set Blocks");
        setEnabled(true, "Game->Blocks->Move Pieces");
        _widget.setBlockMode(false);
    }

    /** Execute Seed... command. */
    private synchronized void setSeed(String unused) {
        String resp =
                getTextInput("Random Seed", "Get Seed", "question", "");
        if (resp == null) {
            return;
        }
        try {
            long s = Long.parseLong(resp);
            send("seed %d", s);
        } catch (NumberFormatException excp) {
            return;
        }
    }

    /** Execute 'pass' command, if legal. */
    private synchronized void doPass(String unused) {
        if (_board.moveLegal(Move.pass())) {
            send("-");
        }
    }

    /** Display 'help' text. */
    private void doHelp(String unused) {
        InputStream helpIn =
                Game.class.getClassLoader()
                        .getResourceAsStream("ataxx/guihelp.txt");
        if (helpIn != null) {
            try {
                BufferedReader r
                        = new BufferedReader(new InputStreamReader(helpIn));
                char[] buffer = new char[1 << 15];
                int len = r.read(buffer);
                showMessage(new String(buffer, 0, len), "Help", "plain");
                r.close();
            } catch (IOException e) {
                /* Ignore IOException */
            }
        }
    }

    /** Return true iff we are currently in block-setting mode. */
    private boolean blockMode() {
        return isSelected("Game->Blocks->Set Blocks");
    }

    void adjustBlockMode(String label) {
        _widget.setBlockMode(label.equals("Game->Blocks->Set Blocks"));
    }

    /** Set PLAYER ("red" or "blue") to be an AI iff ON. */
    private void setAIMode(String player, boolean on) {
        send("%s %s%n", on ? "auto" : "manual", player);
    }

    /** Set label indicating board state. */
    private void updateLabel() {
        String label;
        int red = _board.getColorNums(RED);
        int blue = _board.getColorNums(BLUE);
        if (_board.getWinner() != null) {
            if (red > blue) {
                label = String.format("Red wins (%d-%d)", red, blue);
            } else if (red < blue) {
                label = String.format("Blue wins (%d-%d)", red, blue);
            } else {
                label = "Drawn game";
            }
        } else {
            label = String.format("%s to move", _board.nextMove());
        }
        setLabel("State", label);
    }

    /** Add the command described by FORMAT, ARGS (as for String.format) to
     *  the queue of waiting commands returned by getCommand. */
    private void send(String format, Object... args) {
        _commandQueue.offer(String.format(format, args));
    }

    /** Contains the drawing logic for the Ataxx model. */
    private BoardWidget _widget;
    /** Queue for commands going to the controlling Game. */
    private final ArrayBlockingQueue<String> _commandQueue =
            new ArrayBlockingQueue<>(5);
    /** The model of the game. */
    private Board _board;

    // These methods could be modified
	
    @Override
    public void update(Board board) {
        if (board == _board) {
            updateLabel();
        }
        _board = board;
        _widget.update(board);
    }

    @Override
    public String getCommand(String prompt) {
        try {
            return _commandQueue.take();
        } catch (InterruptedException excp) {
            throw new Error("unexpected interrupt");
        }
    }

    @Override
    public void announceWinner(PieceState state) {
        if (state == EMPTY) {
            showMessage("Tie game.", "Outcome", "information");
        } else {
            showMessage(state.toString() + " wins.", "Outcome", "information");
        }
    }

    @Override
    public void announceMove(Move move, PieceState player) {

    }

    @Override
    public void message(String format, Object... args) {
        showMessage(String.format(format, args), "Message", "information");
    }

    @Override
    public void error(String format, Object... args) {
        showMessage(String.format(format, args), "Error", "error");
    }

    public void setVisible(boolean b) {
		display(true);
    }

    public void pack() {
		
    }
	
}
