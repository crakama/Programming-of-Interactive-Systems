
// BailiffFrame.java
// Fredrik Kilander, DSV
// 18-nov-2004/FK Adapted for PIS course.
// 2001-03-28/FK First version

package dsv.pis.gotag.bailiff;

import processing.core.PApplet;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;


/**
 * This class creates a rudimentary GUI for a Bailiff instance by wrapping
 * a JFrame around it and presenting a simple menu structure. The purpose
 * is to make the Bailiff visible and to provide an easy way to shut it down.
 */
public class BF2 extends JApplet {

    /**
     * The Bailiff service instance we front a GUI for.
     */
    protected Bailiff bf;

    /**
     * Creates a new Bailiff service GUI.
     * @param managedBf The Bailiff service instance we manage a GUI for.
     */
    public BF2(Bailiff managedBf) {

        // Set the title of the JFrame.
        // super (managedBf.getRoom () + " : Bailiff");
        //getContentPane().add("")
        // Copy from method argument to instance field.
        bf = managedBf;


    }//Constructor



    /**
     * The 'about' dialog.
     */
    public void showAboutDialog () {
        // Note that a new thread is created here to run the dialogue.
        // That way control returns at once to the caller, while the user
        // interacts with the dialogue. This ok since its just a read-only
        // information box.
        new Thread (new Runnable () {
            public void run () {
                JOptionPane.showMessageDialog (null,
                        bf.toString (),
                        "Bailiff information",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }).start ();
    }

}