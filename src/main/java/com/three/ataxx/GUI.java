package com.three.ataxx;

// Optional Task: The GUI for the Ataxx Game

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import static com.three.ataxx.PieceState.*;

class GUI extends TopLevel implements View, CommandSource, Reporter {

    /** Contains the drawing logic for the Ataxx model. */
    private final GamePad gamePad;
    /** Queue for commands going to the controlling Game. */
    private final ArrayBlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(5);
    /** The model of the game. */
    private Board board;

    /** Timer */
    private Timer timer;
    private int sec;

    private boolean timerFlag;

    // Complete the codes here
    GUI(String ataxx) {
        super(ataxx, true);
        // set game
        addMenuButton("Setting->New", this::newGame);
        addMenuRadioButton("Setting->Blocks->Set Blocks", "Blocks", false, this::adjustBlockMode);
        addMenuRadioButton("Setting->Blocks->Move Pieces", "Blocks", true, this::adjustBlockMode);
//        addMenuButton("Setting->Set Seed", this::setSeed);
        addMenuRadioButton("Setting->Players->Red AI", "Red", false, (dummy) -> send("ai red"));
        addMenuRadioButton("Setting->Players->Red Manual", "Red", true, (dummy) -> send("manual red"));
        addMenuRadioButton("Setting->Players->Blue AI", "Blue", true, (dummy) -> send("ai blue"));
        addMenuRadioButton("Setting->Players->Blue Manual", "Blue", false, (dummy) -> send("manual blue"));
        addMenuButton("Setting->Quit", this::quit);

        gamePad = new GamePad(commandQueue);
        add(gamePad, new LayoutSpec("height", "1", "width", "REMAINDER", "ileft", 5, "itop", 5, "iright", 5, "ibottom", 5));
        addLabel("Red to move", "State", new LayoutSpec("y", 1, "anchor", "west"));
        addLabel("    0 sec", "Timer", new LayoutSpec("y", 1, "anchor", "west"));
        addLabel("Red 0 : 0 Blue", "Score", new LayoutSpec("y", 1, "anchor", "east"));
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
        setEnabled(false, "Setting->Blocks->Set Blocks");
        setEnabled(true, "Setting->Blocks->Move Pieces");
        gamePad.setBlockMode(false);

        // prepare timer
        timer = new Timer();
        sec = 0;
        timerFlag = false;
        doTimer();
    }

    /** Execute 'pass' command, if legal. */
    private synchronized void doPass(String unused) {
        if (board.moveLegal(Move.pass())) {
            send("-");
        }
    }

    void adjustBlockMode(String label) {
        gamePad.setBlockMode(label.equals("Setting->Blocks->Set Blocks"));
    }

    /** Set label indicating board state. */
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

    public void doTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                sec++;
                String label = String.format("    %d sec", sec);
                setLabel("Timer", label);
            }
        };

        // 定义计时器的开始时间和间隔时间（以毫秒为单位）
        long delay = 1000L; // 延迟1秒执行
        long interval = 0L; // 每隔1秒执行

        // 安排计时器任务
        timer.scheduleAtFixedRate(task, delay, interval);
    }

    public void clear() {
        sec = 0;
    }

    public void stopAndClear() {
        timer.cancel();
        timer.purge();
        clear();
    }

    /** Add the command described by FORMAT, ARGS (as for String.format) to
     *  the queue of waiting commands returned by getCommand. */
    private void send(String format, Object... args) {
        commandQueue.offer(String.format(format, args));
    }


    // These methods could be modified

    @Override
    public void update(Board board) {
        if (board == this.board) {
            updateStateLabel();
            updateScoreLabel();
        }
        this.board = board;
        gamePad.update(board);
    }

    @Override
    public String getCommand(String prompt) {
        try {
            return commandQueue.take();
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
