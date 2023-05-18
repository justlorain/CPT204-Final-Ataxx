package com.three.ataxx;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Observable;
import java.util.function.Consumer;

public class GUIHelper extends Observable implements ActionListener {
    private final HashMap<String, ButtonHandler> buttonMap = new HashMap<>();
    private final HashMap<String, ButtonGroup> buttonGroups = new HashMap<>();
    private final HashMap<String, JLabel> labelMap = new HashMap<>();
    private static final HashMap<String, Integer> MESSAGE_TYPE_MAP = new HashMap<>();
    protected final JFrame frame;

    public void display(boolean visible) {
        if (visible) {
            this.frame.pack();
        }

        this.frame.setVisible(visible);
    }

    protected GUIHelper(String title, boolean exitOnClose) {
        this.frame = new JFrame(title);
        this.frame.setUndecorated(true);
        this.frame.getRootPane().setWindowDecorationStyle(1);
        this.frame.setLayout(new GridBagLayout());
        if (exitOnClose) {
            this.frame.setDefaultCloseOperation(3);
        }

    }

    public void setPreferredSize(int width, int height) {
        this.frame.setPreferredSize(new Dimension(width, height));
    }

    public void setMinimumSize(int width, int height) {
        this.frame.setMinimumSize(new Dimension(width, height));
    }

    public void setMaximumSize(int width, int height) {
        this.frame.setMaximumSize(new Dimension(width, height));
    }

    public int getWidth() {
        return this.frame.getWidth();
    }

    public int getHeight() {
        return this.frame.getHeight();
    }

    protected void addMenuButton(String label, Consumer<String> func) {
        String[] names = label.split("->");
        if (names.length <= 1) {
            throw new IllegalArgumentException("cannot parse label");
        } else {
            JMenu menu = this.getMenu(names, names.length - 2);
            JMenuItem item = new JMenuItem(names[names.length - 1]);
            item.setActionCommand(label);
            item.addActionListener(this);
            menu.add(item);
            this.buttonMap.put(label, new ButtonHandler(func, label, item));
        }
    }

    protected void addMenuCheckBox(String label, boolean selected, Consumer<String> func) {
        String[] names = label.split("->");
        if (names.length <= 1) {
            throw new IllegalArgumentException("cannot parse label");
        } else {
            JMenu menu = this.getMenu(names, names.length - 2);
            JMenuItem item = new JCheckBoxMenuItem(names[names.length - 1], selected);
            item.setActionCommand(label);
            item.addActionListener(this);
            menu.add(item);
            this.buttonMap.put(label, new ButtonHandler(func, label, item));
        }
    }

    protected void addMenuRadioButton(String label, String groupName, boolean selected, Consumer<String> func) {
        String[] names = label.split("->");
        if (names.length <= 1) {
            throw new IllegalArgumentException("cannot parse label");
        } else {
            JMenu menu = this.getMenu(names, names.length - 2);
            JMenuItem item = new JRadioButtonMenuItem(names[names.length - 1], selected);
            this.getGroup(groupName).add(item);
            item.setActionCommand(label);
            this.buttonMap.put(label, new ButtonHandler(func, label, item));
            if (func != null) {
                item.addActionListener(this);
            }

            menu.add(item);
        }
    }

    protected void addSeparator(String label) {
        String[] labels = label.split("->");
        if (labels.length == 0) {
            throw new IllegalArgumentException("invalid menu designator");
        } else {
            JMenu menu = this.getMenu(labels, labels.length - 1);
            menu.addSeparator();
        }
    }

    protected boolean isSelected(String label) {
        ButtonHandler h = (ButtonHandler)this.buttonMap.get(label);
        return h == null ? false : h.isSelected();
    }

    protected void select(String label, boolean val) {
        ButtonHandler h = (ButtonHandler)this.buttonMap.get(label);
        if (h != null) {
            h.setSelected(val);
        }

    }

    protected void setEnabled(boolean enable, String... labels) {
        String[] var3 = labels;
        int var4 = labels.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String label = var3[var5];
            ButtonHandler h = (ButtonHandler)this.buttonMap.get(label);
            if (h != null) {
                h.setEnabled(enable);
            }
        }

    }

    protected void addButton(String label, Consumer<String> func, LayoutHelper layout) {
        if (this.buttonMap.containsKey(label)) {
            throw new IllegalStateException("already have button labeled " + label);
        } else {
            JButton button = new JButton(label);
            button.setActionCommand(label);
            button.addActionListener(this);
            this.frame.add(button, layout.params());
            this.buttonMap.put(label, new ButtonHandler(func, label, button));
        }
    }

    protected void addCheckBox(String label, boolean selected, Consumer<String> func, LayoutHelper layout) {
        if (this.buttonMap.containsKey(label)) {
            throw new IllegalStateException("already have item labeled " + label);
        } else {
            JCheckBox box = new JCheckBox(label, selected);
            box.setActionCommand(label);
            box.addActionListener(this);
            this.frame.add(box, layout.params());
            this.buttonMap.put(label, new ButtonHandler(func, label, box));
        }
    }

    protected void addRadioButton(String label, String groupName, boolean selected, Consumer<String> func, LayoutHelper layout) {
        JRadioButton item = new JRadioButton(label, selected);
        this.getGroup(groupName).add(item);
        item.setActionCommand(label);
        this.buttonMap.put(label, new ButtonHandler(func, label, item));
        if (func != null) {
            item.addActionListener(this);
        }

        this.frame.add(item, layout.params());
    }

    protected void add(Widget widget, LayoutHelper layout) {
        this.frame.add(widget.me, layout.params());
    }

    protected void addLabel(String text, String id, LayoutHelper layout) {
        if (this.labelMap.containsKey(id)) {
            throw new IllegalArgumentException("duplicate label id: " + id);
        } else {
            JLabel label = new JLabel(text);
            this.labelMap.put(id, label);
            this.frame.add(label, layout.params());
        }
    }

    protected void setLabel(String id, String text) {
        JLabel label = (JLabel)this.labelMap.get(id);
        if (label == null) {
            throw new IllegalArgumentException("unknown label id: " + id);
        } else {
            label.setText(text);
        }
    }

    protected void addLabel(String text, LayoutHelper layout) {
        this.frame.add(new JLabel(text), layout.params());
    }

    public void showMessage(String text, String title, String type) {
        JOptionPane.showMessageDialog(this.frame, text, title, this.getMessageType(type), (Icon)null);
    }

    public int showOptions(String message, String title, String type, String deflt, String... labels) {
        return JOptionPane.showOptionDialog(this.frame, message, title, 0, this.getMessageType(type), (Icon)null, labels, deflt);
    }

    public String getTextInput(String message, String title, String type, String init) {
        Object input = JOptionPane.showInputDialog(this.frame, message, title, this.getMessageType(type), (Icon)null, (Object[])null, init);
        return input instanceof String ? (String)input : null;
    }

    public void setPreferredFocus(final Widget widget) {
        this.frame.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                widget.requestFocusInWindow();
            }
        });
    }

    private int getMessageType(String type) {
        if (type == null) {
            return -1;
        } else {
            type = type.toLowerCase();
            int intType = -1;
            if (type != null && MESSAGE_TYPE_MAP.containsKey(type)) {
                intType = (Integer)MESSAGE_TYPE_MAP.get(type);
            }

            return intType;
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof AbstractButton) {
            String key = e.getActionCommand();
            ButtonHandler h = (ButtonHandler)this.buttonMap.get(key);
            if (h == null) {
                return;
            }

            h.doAction();
        }

    }

    private JMenu getMenu(String[] label, int last) {
        if (this.frame.getJMenuBar() == null) {
            this.frame.setJMenuBar(new JMenuBar());
        }

        JMenuBar bar = this.frame.getJMenuBar();
        JMenu menu = null;

        int k;
        for(k = 0; k < bar.getMenuCount(); ++k) {
            menu = bar.getMenu(k);
            if (menu.getText().equals(label[0])) {
                break;
            }

            menu = null;
        }

        if (menu == null) {
            menu = new JMenu(label[0]);
            bar.add(menu);
        }

        for(k = 1; k <= last; ++k) {
            JMenu menu0 = menu;
            menu = null;

            for(int i = 0; i < menu0.getItemCount(); ++i) {
                JMenuItem item = menu0.getItem(i);
                if (item != null) {
                    if (item.getText().equals(label[k])) {
                        if (!(item instanceof JMenu)) {
                            throw new IllegalStateException("inconsistent menu label");
                        }

                        menu = (JMenu)item;
                        break;
                    }

                    menu = null;
                }
            }

            if (menu == null) {
                menu = new JMenu(label[k]);
                menu0.add(menu);
            }
        }

        return menu;
    }

    private ButtonGroup getGroup(String name) {
        ButtonGroup g = (ButtonGroup)this.buttonGroups.get(name);
        if (g == null) {
            g = new ButtonGroup();
            this.buttonGroups.put(name, g);
        }

        return g;
    }

    static {
        MESSAGE_TYPE_MAP.put("information", 1);
        MESSAGE_TYPE_MAP.put("warning", 2);
        MESSAGE_TYPE_MAP.put("error", 0);
        MESSAGE_TYPE_MAP.put("plain", -1);
        MESSAGE_TYPE_MAP.put("question", 3);
    }

    private record ButtonHandler(Consumer<String> func, String id, AbstractButton src) {

        boolean isSelected() {
                return this.src.getModel().isSelected();
            }

            void setSelected(boolean value) {
                this.src.setSelected(value);
            }

            void setEnabled(boolean value) {
                this.src.setEnabled(value);
            }

            void doAction() {
                if (this.func != null) {
                    this.func.accept(this.id);
                }

            }
        }
}

