package com.three.ataxx;

import javax.swing.*;
import java.util.Observable;

public abstract class Widget extends Observable {
    protected JComponent me;

    public Widget() {
    }

    public boolean requestFocusInWindow() {
        return this.me.requestFocusInWindow();
    }
}
