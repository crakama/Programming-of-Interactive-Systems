//package dsv.pis.gotag.bailiff;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
//public class BailiffApplet extends JApplet {
//    Timer timer;
//    public void init(){
//        try
//        {
//            SwingUtilities.invokeAndWait(new Runnable() {
//                public void run()
//                {
//                    createGUI();
//                }
//            });
//        }
//        catch (Exception e)
//        {
//            System.err.println("createGUI didn't successfully complete: " + e);
//        }
//        ActionListener actionDisplayAgent = new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent actionEvent) {
//                //TODO: Add update methods here e.g update UI on newly migrated agents.
//
//                repaint();
//
//            }
//        };
//
//        timer = new Timer(30,actionDisplayAgent);
//        timer.start();
//
//    }
//    /**
//     * Create the GUI for Applet. For thread safety, this method should
//     * be invoked from the event-dispatching thread.
//     */
//    private void createGUI() {
//        //TODO:
//        JLabel appletLabel = new JLabel( "I'm a Swing Applet" );
//        appletLabel.setHorizontalAlignment( JLabel.CENTER );
//        appletLabel.setFont(new Font("Serif", Font.PLAIN, 36));
//        add( appletLabel );
//        setSize(400, 200);
//    }
//
//    public void paint(Graphics graphics){
//        graphics.drawString(" Agent Name",120,60);
//
//    }
//
//}
