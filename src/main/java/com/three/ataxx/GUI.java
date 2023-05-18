package com.three.ataxx;

// Optional Task: The GUI for the Ataxx Game

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import static com.three.ataxx.PieceState.*;

class GUI extends GUIHelper implements View, CommandSource, Reporter {

    /** Contains the drawing logic for the Ataxx model. */
    private final GamePad gamePad;
    /** Queue for commands going to the controlling Game. */
    private final ArrayBlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(5);
    /** The model of the game. */
    private Board board;
    /** Timer */
    private Timer timer;
    /** Seconds */
    private int sec;
    /** Timer flag */
    private boolean timerFlag;

    // Complete the codes here

    /**
     * Constructor of GUI.
     * @param ataxx the title of the GUI.
     */
    GUI(String ataxx) {
        super(ataxx, true);
        // set game
        addMenuButton("Setting->New", this::newGame);
        addMenuRadioButton("Setting->Blocks->Set Blocks", "Blocks", false, this::adjustBlockMode);
        addMenuRadioButton("Setting->Blocks->Move Pieces", "Blocks", true, this::adjustBlockMode);
        addMenuRadioButton("Setting->Players->Red AI", "Red", false, (dummy) -> send("ai red"));
        addMenuRadioButton("Setting->Players->Red Manual", "Red", true, (dummy) -> send("manual red"));
        addMenuRadioButton("Setting->Players->Blue AI", "Blue", true, (dummy) -> send("ai blue"));
        addMenuRadioButton("Setting->Players->Blue Manual", "Blue", false, (dummy) -> send("manual blue"));
        addMenuButton("Setting->Quit", this::quit);

        gamePad = new GamePad(commandQueue);
        add(gamePad, new LayoutHelper("height", "1", "width", "REMAINDER", "ileft", 5, "itop", 5, "iright", 5, "ibottom", 5));
        addLabel("Red to move", "State", new LayoutHelper("y", 1, "anchor", "west"));
        addLabel("    0 sec", "Timer", new LayoutHelper("y", 1, "anchor", "west"));
        addLabel("Red 0 : 0 Blue", "Score", new LayoutHelper("y", 1, "anchor", "east"));
        addButton("Pass", this::doPass, new LayoutHelper("y", "1"));
        prepareTimer();
    }

    // Add some codes here

    /**
     * Quit the game.
     * @param unused the label of the button.
     */
    private synchronized void quit(String unused) {
        send("quit");
    }

    /**
     * Execute the "New" button function.
     * @param unused the label of the button.
     */
    private synchronized void newGame(String unused) {
        send("new");
        setEnabled(false, "Setting->Blocks->Set Blocks");
        setEnabled(true, "Setting->Blocks->Move Pieces");
        gamePad.setBlockMode(false);
    }

    /**
     * Execute the "Pass" button function.
     * @param unused the label of the button.
     */
    private synchronized void doPass(String unused) {
        if (board.moveLegal(Move.pass())) {
            send("-");
        }
    }

    /**
     * Adjust the block mode.
     * @param label the label of the button.
     */
    void adjustBlockMode(String label) {
        gamePad.setBlockMode(label.equals("Setting->Blocks->Set Blocks"));
    }

    /**
     * Send the command to the game.
     */
    private void updateStateLabel() {
        String label;
        int red = board.getColorNums(RED);
        int blue = board.getColorNums(BLUE);
        if (board.getWinner() != null) {
            if (red > blue) {
                label = String.format("Red wins (%d-%d)", red, blue);
            } else if (red < blue) {
                label = String.format("Blue wins (%d-%d)", red, blue);
            } else {
                label = "Drawn game";
            }
        } else {
            label = String.format("%s to move", board.nextMove());
        }
        setLabel("State", label);
    }

    /**
     * Set label indicating board score.
     */
    private void updateScoreLabel() {
        String label;
        int rScore = board.getColorNums(RED);
        int bScore = board.getColorNums(BLUE);
        if (board.getWinner() != null) {
            if (rScore > bScore) {
                label = String.format("Red wins (%d-%d)", rScore, bScore);
            } else if (rScore < bScore) {
                label = String.format("Blue wins (%d-%d)", rScore, bScore);
            } else {
                label = "Drawn game";
            }
        } else {
            label = String.format("Red %d : %d Blue", rScore, bScore);
        }
        setLabel("Score", label);
    }

    /**
     * prepare the timer.
     */
    private void prepareTimer() {
        timer = new Timer();
        sec = 0;
        timerFlag = true;
    }

    /**
     * start the timer.
     */
    private void startTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                String label = String.format("    %d sec", sec);
                setLabel("Timer", label);
                sec++;
            }
        };
        long delay = 0L;
        long interval = 1000L;
        timer.scheduleAtFixedRate(task, delay, interval);
    }

    /**
     * stop the timer.
     */
    public void stopTimer() {
        timer.cancel(); // 停止计时器
    }

    /**
     * restart the timer.
     */
    public void restartTimer() {
        timer.cancel(); // 取消当前计时器
        timer = new Timer(); // 创建新的计时器
        sec = 0; // 将 count 变量归零
        startTimer(); // 重新开始计时
    }

    /** Add the command described by FORMAT, ARGS (as for String.format) to
     *  the queue of waiting commands returned by getCommand. */
    private void send(String format, Object... args) {
        commandQueue.offer(String.format(format, args));
    }


    // These methods could be modified

    /**
     * Update the board.
     * @param board the board.
     */
    @Override
    public void update(Board board) {
        if (timerFlag) {
            startTimer();
            timerFlag = false;
        }
        if (board == this.board) {
            updateStateLabel();
            updateScoreLabel();
        }
        this.board = board;
        gamePad.update(board);
        restartTimer();
    }

    /**
     * Get the command.
     * @param prompt the prompt.
     * @return the command.
     */
    @Override
    public String getCommand(String prompt) {
        try {
            return commandQueue.take();
        } catch (InterruptedException excp) {
            throw new Error("unexpected interrupt");
        }
    }

    /**
     * Announce the winner.
     * @param state the winner.
     */
    @Override
    public void announceWinner(PieceState state) {
        if (state == EMPTY) {
            showMessage("Tie game.", "Outcome", "information");
        } else {
            showMessage(state.toString() + " wins.", "Outcome", "information");
            stopTimer();
        }
    }

    /**
     * Announce the move.
     * @param move the move.
     * @param player the player.
     */
    @Override
    public void announceMove(Move move, PieceState player) {

    }

    /**
     * Announce the error.
     * @param format the format.
     * @param args the args.
     */
    @Override
    public void message(String format, Object... args) {
        showMessage(String.format(format, args), "Message", "information");
    }

    /**
     * Announce the error.
     * @param format the format.
     * @param args the args.
     */
    @Override
    public void error(String format, Object... args) {
        showMessage(String.format(format, args), "Error", "error");
    }

    /**
     * set the visible.
     * @param b the boolean.
     */
    public void setVisible(boolean b) {
        display(true);
    }

    /**
     * pack the frame.
     */
    public void pack() {

    }

}
