package com.three.ataxx;


import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class LayoutSpec {
    private static final HashSet<String> ALL_SPECS = new HashSet(Arrays.asList("x", "y", "fill", "height", "ht", "width", "wid", "anchor", "weightx", "weighty", "ileft", "iright", "itop", "ibottom"));
    private static final HashMap<Object, Integer> INT_NAMES = new HashMap();
    private static final Object[][] INT_NAMES_INIT = new Object[][]{{"center", 10}, {"north", 11}, {"south", 15}, {"east", 13}, {"west", 17}, {"southwest", 16}, {"southeast", 14}, {"northwest", 18}, {"northeast", 12}, {"remainder", 0}, {"rest", 0}, {"horizontal", 2}, {"horiz", 2}, {"vertical", 3}, {"vert", 3}, {"both", 1}};
    private final GridBagConstraints _params = new GridBagConstraints();

    public LayoutSpec(Object... specs) {
        this._params.weightx = 1.0;
        this._params.weighty = 1.0;
        this._params.insets = new Insets(0, 0, 0, 0);
        this.add(specs);
    }

    public void add(Object... specs) {
        if (specs.length % 2 == 1) {
            throw new IllegalArgumentException("Missing last value");
        } else {
            int i;
            for(i = 0; i < specs.length; i += 2) {
                if (!(specs[i] instanceof String) || !ALL_SPECS.contains(specs[i])) {
                    throw new IllegalArgumentException("Illegal LayoutSpec key: " + specs[i]);
                }

                if (!(specs[i + 1] instanceof Integer) && !(specs[i + 1] instanceof Double) && !(specs[i + 1] instanceof String)) {
                    throw new IllegalArgumentException("Illegal value for" + specs[i] + " key");
                }
            }

            for(i = 0; i < specs.length; i += 2) {
                Object key = specs[i];
                Object val = specs[i + 1];
                this.addKey(key, val);
            }

        }
    }

    public GridBagConstraints params() {
        return this._params;
    }

    private void addKey(Object key, Object val) {
        switch (key.toString()) {
            case "x":
                this._params.gridx = this.toInt(val);
                break;
            case "y":
                this._params.gridy = this.toInt(val);
                break;
            case "width":
                this._params.gridwidth = this.toInt(val);
                break;
            case "ht":
                this._params.gridheight = this.toInt(val);
                break;
            case "anchor":
                this._params.anchor = this.toInt(val);
                break;
            case "ileft":
                this._params.insets.left = this.toInt(val);
                break;
            case "iright":
                this._params.insets.right = this.toInt(val);
                break;
            case "itop":
                this._params.insets.top = this.toInt(val);
                break;
            case "ibottom":
                this._params.insets.bottom = this.toInt(val);
                break;
            case "fill":
                this._params.fill = this.toInt(val);
                break;
            case "weightx":
                this._params.weightx = this.toDouble(val);
                break;
            case "weighty":
                this._params.weighty = this.toDouble(val);
        }

    }

    private int toInt(Object x) {
        if (x instanceof Integer) {
            return (Integer)x;
        } else if (x instanceof Double) {
            return (int) x;
        } else if (!(x instanceof String)) {
            return -1;
        } else {
            String str = String.valueOf(x);
            String lowerCase = str.toLowerCase();
            x = (Object) lowerCase;
            return INT_NAMES.containsKey(x) ? (Integer)INT_NAMES.get(x) : -1;
        }
    }

    private double toDouble(Object x) {
        return x instanceof Double ? (Double)x : (double)this.toInt(x);
    }

    static {
        Object[][] var0 = INT_NAMES_INIT;
        int var1 = var0.length;

        for(int var2 = 0; var2 < var1; ++var2) {
            Object[] pair = var0[var2];
            INT_NAMES.put(pair[0], (Integer)pair[1]);
        }

    }
}

