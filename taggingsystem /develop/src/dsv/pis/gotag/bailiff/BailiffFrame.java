// BailiffFrame.java
// Fredrik Kilander, DSV
// 18-nov-2004/FK Adapted for PIS course.
// 2001-03-28/FK First version

package dsv.pis.gotag.bailiff;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.*;
import javax.swing.border.*;

/**
 * This class creates a rudimentary GUI for a Bailiff instance by wrapping
 * a JFrame around it and presenting a simple menu structure. The purpose
 * is to make the Bailiff visible and to provide an easy way to shut it down.
 */
public class BailiffFrame extends JFrame
{

    /**
     * The Bailiff service instance we front a GUI for.
     */
    public Bailiff bf;
    //protected JApplet applet;

    /**
     * Creates a new Bailiff service GUI.
     * @param managedBf The Bailiff service instance we manage a GUI for.
     *
     */
    public BailiffFrame(Bailiff managedBf) {

        // Set the title of the JFrame.
        super (managedBf.getRoom () + " : Bailiff");

        // Copy from method argument to instance field.
        bf = managedBf;
        // this.applet = applet;

        // Create a menu bar to hold our menus.
        JMenuBar menuBar = new JMenuBar ();

        // Create a menu labelled 'File'.
        JMenu fileMenu = (JMenu) menuBar.add (new JMenu ("File"));
        // Bind ALT+F to the File menu.
        fileMenu.setMnemonic (KeyEvent.VK_F);

        // Create and add a menu item labelled 'Exit' to the File menu.
        JMenuItem item = (JMenuItem) fileMenu.add (new JMenuItem ("Exit"));
        // Bind ALT+X to the Exit item.
        item.setMnemonic (KeyEvent.VK_X);
        // Install the code to execute when the Exit item is selected.
        item.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent e) {
                bf.shutdown ();	// Shut down the Bailiff
                System.exit (0);	// Exit the JVM
            }
        });

        // Create a menu labelled 'Info'.
        JMenu options = (JMenu) menuBar.add (new JMenu ("Info"));
        // Bind ALT+I to the Info menu.
        options.setMnemonic (KeyEvent.VK_I);

        // Create and add a menu item labelled 'About...' to the Info menu.
        item = (JMenuItem) options.add (new JMenuItem ("About..."));
        // Bind ALT+A to the About item.
        item.setMnemonic (KeyEvent.VK_A);
        // Install the code to execute when the About item is selected.
        // Notice the use of the adapter class ActionListener which is
        // anonymously subclassed as the argument to addActionListener().
        item.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent e) {
                showAboutDialog ();
            }
        });

        // Install the menubar.
        setJMenuBar (menuBar);

        // Install code to execute for certain window events.
        // Adapter class WindowAdapter...
        addWindowListener (new WindowAdapter () {
            // If the windows is closed, shut down the Bailiff.
            public void windowClosing (WindowEvent e) {
                bf.shutdown ();
                System.exit (0);
            }
            // If we are minimized or maximized, keep working.
            public void windowDeiconified (WindowEvent e) {}
            public void windowIconified (WindowEvent e) {}
        });

        //Add Applet in Frame
        //add(applet);

        // Do qualitative layout
        pack ();

        // Determine actual sizes.
        Dimension d = Toolkit.getDefaultToolkit ().getScreenSize ();
        // The window is located 1/8th of the screen size from upper left corner.
        setLocation (d.width/8, d.height/8);
        // The window is 1/12th wide, 1/10th high, or screen size.
        //setSize (new Dimension ((d.width/12), (d.height/10)));
        setSize(400,400);

//
//        JApplet applet = new BailiffApplet();
//        applet.init();
//        add(applet);
//
//        // Show it.
      setVisible (true);
//        applet.start();
    }



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

//    public class BailiffApplet extends JApplet implements ActionListener{
//        javax.swing.Timer timer;
//        int pause;
//        BailiffAnimator bailiffAnimator;
//        protected void configureApplet(){ }
//        /**
//         * Create the GUI for Applet. For thread safety, this method should
//         * be invoked from the event-dispatching thread.
//         */
//        private void createGUI() {
//
//            //Custom component to draw the current image
//            //at a particular offset.
//            bailiffAnimator = new BailiffAnimator();
//            bailiffAnimator.setOpaque(true);
//            bailiffAnimator.setBackground(Color.white);
//            setContentPane(bailiffAnimator);
//
//            //TODO:
//            JLabel appletLabel = new JLabel( "I'm a Swing Applet" );
//            appletLabel.setHorizontalAlignment( JLabel.CENTER );
//            appletLabel.setFont(new Font("Serif", Font.PLAIN, 36));
//            add( appletLabel );
//            setSize(400, 200);
//        }
//
//
//        public void init(){
//            configureApplet();
//
//            //Execute a job on the event-dispatching thread:
//            //creating this applet's GUI.
//            try {
//                SwingUtilities.invokeAndWait(new Runnable() {
//                    public void run() {
//                        createGUI();
//                    }
//                });
//            } catch (Exception e) {
//                System.err.println("createGUI didn't successfully complete");
//            }
//
//            //Set up timer to drive animation events.
//            timer = new Timer(30, this);
//            //timer.setInitialDelay(500);
//            timer.setRepeats(true);
//            timer.start();
//
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent actionEvent) {
//            bailiffAnimator.repaint();
//
//        }
//
//        //The component that actually presents the GUI.
//        public class BailiffAnimator extends JPanel {
//            public BailiffAnimator() {
//                super(new BorderLayout());
//            }
//
//            protected void paintComponent(Graphics graphics) {
//                super.paintComponent(graphics);
//                graphics.drawString(" Agent Name",120,60);
//
//            }
//        }
//
//
//    }

}

