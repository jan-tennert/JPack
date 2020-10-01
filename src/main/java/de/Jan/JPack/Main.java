package de.Jan.JPack;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        if(!System.getProperty("os.name").toLowerCase().startsWith("win")) {
            JOptionPane.showMessageDialog(null, "Currently you can only use this program on windows!");
            return;
        }
        App.main(args);
    }
}
