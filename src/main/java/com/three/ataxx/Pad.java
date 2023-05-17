package com.three.ataxx;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.function.BiConsumer;
import javax.swing.JComponent;

public class Pad extends Widget {
    private final HashMap<String, BiConsumer<String, MouseEvent>> mouseEventMap = new HashMap<>();
    private final HashMap<String, BiConsumer<String, KeyEvent>> keyEventMap = new HashMap<>();
    private static final String[] MOUSE_EVENT_NAMES = new String[]{"press", "release", "click", "enter", "exit", "drag", "move"};
    private static final String[] KEY_EVENT_NAMES = new String[]{"keypress", "keyrelease", "keytype"};
    private Dimension minimumSize;
    private Dimension preferredSize;
    private Dimension maximumSize;

    protected Pad() {
        this.me = new PadComponent();
        this.me.setFocusable(true);
        String[] var1 = MOUSE_EVENT_NAMES;
        int var2 = var1.length;

        int var3;
        String event;
        for(var3 = 0; var3 < var2; ++var3) {
            event = var1[var3];
            this.mouseEventMap.put(event, null);
        }

        var1 = KEY_EVENT_NAMES;
        var2 = var1.length;

        for(var3 = 0; var3 < var2; ++var3) {
            event = var1[var3];
            this.keyEventMap.put(event, null);
        }

    }

    public void setSize(int width, int height) {
        this.me.setSize(width, height);
    }

    public void setPreferredSize(int width, int height) {
        this.preferredSize = new Dimension(width, height);
        this.me.invalidate();
    }

    public void setMinimumSize(int width, int height) {
        this.minimumSize = new Dimension(width, height);
        this.me.invalidate();
    }

    public void setMaximumSize(int width, int height) {
        this.maximumSize = new Dimension(width, height);
        this.me.invalidate();
    }

    public int getWidth() {
        return this.me.getWidth();
    }

    public int getHeight() {
        return this.me.getHeight();
    }

    public void repaint() {
        this.me.repaint();
    }

    public void repaint(long tm) {
        this.me.repaint(tm);
    }

    public void repaint(int x, int y, int width, int height) {
        this.me.repaint(x, y, width, height);
    }

    public void repaint(long tm, int x, int y, int width, int height) {
        this.me.repaint(tm, x, y, width, height);
    }

    public void setMouseHandler(String eventKind, BiConsumer<String, MouseEvent> func) {
        if (!this.mouseEventMap.containsKey(eventKind)) {
            throw new IllegalArgumentException("Unknown event: " + eventKind);
        } else {
            this.mouseEventMap.put(eventKind, func);
            if (!eventKind.equals("drag") && !eventKind.equals("move")) {
                if (this.me.getMouseListeners().length == 0) {
                    this.me.addMouseListener((MouseListener)this.me);
                }
            } else if (this.me.getMouseMotionListeners().length == 0) {
                this.me.addMouseMotionListener((MouseMotionListener)this.me);
            }

        }
    }

    public void setKeyHandler(String eventKind, BiConsumer<String, KeyEvent> func) {
        this.me.setFocusTraversalKeysEnabled(false);
        if (!this.keyEventMap.containsKey(eventKind)) {
            throw new IllegalArgumentException("Unknown event: " + eventKind);
        } else {
            this.keyEventMap.put(eventKind, func);
            if (this.me.getKeyListeners().length == 0) {
                this.me.addKeyListener((KeyListener)this.me);
            }

        }
    }

    protected void paintComponent(Graphics2D g) {
    }

    private void handle(String kind, MouseEvent e) {
        BiConsumer<String, MouseEvent> h = this.mouseEventMap.get(kind);
        if (h != null) {
            h.accept(kind, e);
        }
    }

    private void handle(String kind, KeyEvent e) {
        BiConsumer<String, KeyEvent> h = this.keyEventMap.get(kind);
        if (h != null) {
            h.accept(kind, e);
        }
    }

    private class PadComponent extends JComponent implements MouseListener, MouseMotionListener, KeyListener {
        private PadComponent() {
        }

        public void paintComponent(Graphics g) {
            Pad.this.paintComponent((Graphics2D)g);
        }

        public void mouseClicked(MouseEvent e) {
            Pad.this.handle("click", e);
        }

        public void mouseReleased(MouseEvent e) {
            Pad.this.handle("release", e);
        }

        public void mousePressed(MouseEvent e) {
            Pad.this.handle("press", e);
        }

        public void mouseEntered(MouseEvent e) {
            Pad.this.handle("enter", e);
        }

        public void mouseExited(MouseEvent e) {
            Pad.this.handle("exit", e);
        }

        public void mouseDragged(MouseEvent e) {
            Pad.this.handle("drag", e);
        }

        public void mouseMoved(MouseEvent e) {
            Pad.this.handle("move", e);
        }

        public void keyPressed(KeyEvent e) {
            Pad.this.handle("keypress", e);
        }

        public void keyReleased(KeyEvent e) {
            Pad.this.handle("keyrelease", e);
        }

        public void keyTyped(KeyEvent e) {
            Pad.this.handle("keytype", e);
        }

        public Dimension getPreferredSize() {
            return Pad.this.preferredSize == null ? super.getPreferredSize() : Pad.this.preferredSize;
        }

        public Dimension getMinimumSize() {
            return Pad.this.minimumSize == null ? super.getMinimumSize() : Pad.this.minimumSize;
        }

        public Dimension getMaximumSize() {
            return Pad.this.maximumSize == null ? super.getMaximumSize() : Pad.this.maximumSize;
        }
    }
}

