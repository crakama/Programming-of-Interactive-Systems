// Dexter.java
// Bailiff excerciser and demo.
// Fredrik Kilander, DSV
// 30-jan-2009/FK Replaced f.show() (deprecated) with f.setVisible();
// 07-feb-2008/FK Code smarted up a bit.
// 18-nov-2004/FK Adapted for PRIS course.
// 2000-12-18/FK Runs for the first time.
// 2000-12-13/FK

package dsv.pis.gotag.bailiff;

import java.io.*;
import java.lang.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.util.UUID;

import net.jini.core.lookup.*;
import net.jini.lookup.*;

import dsv.pis.gotag.util.*;

/**
 * Dexter jumps around randomly among the Bailiffs. He is can be used
 * test that the system is operating, or as a template for more
 * evolved agents.
 */
public class Dexter implements Serializable
{
    /**
     * The string name of the Bailiff service interface, used when
     * querying the Jini lookup server.
     */
    protected static final String bfi =
            "dsv.pis.gotag.bailiff.BailiffInterface";

    /**
     * The debug flag controls the amount of diagnostic info we put out.
     */
    protected boolean debug = false;

    /**
     * The noFace flag disables the graphical frame when true.
     */
    protected boolean noFace = false;

    protected boolean itStatus;
    private  AgentStatusType statusType;

    /**
     * Dexter uses a ServiceDiscoveryManager to find Bailiffs.
     * The SDM is not serializable so it must recreated on each new Bailiff.
     * That is why it is marked as transient.
     */
    protected transient ServiceDiscoveryManager SDM;

    /**
     * This service template is created in Dexter's constructor and used
     * in the topLevel method to find Bailiffs. The service
     * template IS serializable so Dexter only needs to instantiate it once.
     */
    protected ServiceTemplate bailiffTemplate;
    private int code;


    /**
     * Outputs a diagnostic message on standard output. This will be on
     * the host of the launching JVM before Dexter moves. Once he has migrated
     * to another Bailiff, the text will appear on the console of that Bailiff.
     * @param msg The message to print.
     */
    protected void debugMsg (String msg) {
        if (debug) System.out.println (msg);
    }

    /**
     * This creates a new Dexter. All the constructor needs to do is to
     * instantiate the service template.
     * @param debug True if this instance is being debugged.
     * @param itPlayer
     * @param id
     * @throws ClassNotFoundException Thrown if the class for the Bailiff
     * service interface could not be found.
     */
    public Dexter(boolean debug, boolean noFace, boolean itPlayer, String id) throws java.lang.ClassNotFoundException {
        if (this.debug == false) this.debug = debug;

        this.noFace = noFace;
        if(itPlayer){
            setStatusType(AgentStatusType.STATUS_IS_IT);
            debugMsg(" TagGame: " + id +" : Status: "+getStatusType()+"    Initialised");

        }else {
            setStatusType(AgentStatusType.STATUS_TAGGABLE);
            debugMsg(" TagGame: " + id +" : Status: "+getStatusType()+"    Initialised");
        }


        // This service template is used to query the Jini lookup server
        // for services which implement the BailiffInterface. The string
        // name of that interface is passed in the bfi argument. At this
        // point we only create and configure the service template, no
        // query has yet been issued.

        bailiffTemplate = new ServiceTemplate (null, new Class [] {java.lang.Class.forName (bfi)},null);
    }

    /**
     * Sleep snugly and safely not bothered by interrupts.
     * @param ms  The number of milliseconds to sleep.
     */
    protected void snooze (long ms) {
        try {
            Thread.currentThread ().sleep (ms);
        }
        catch (java.lang.InterruptedException e) {}
    }

    public AgentStatusType getStatusType() {
        return statusType;
    }

    public void setStatusType(AgentStatusType statusType) {
        this.statusType = statusType;
    }



    /**
     * This is Dexter's main program once he is on his way. In short, he
     * gets himself a service discovery manager and asks it about Bailiffs.
     * If the list is long enough, he then selects one randomly and pings it.
     * If the ping returned without a remote exception, Dexter then tries
     * to migrate to that Bailiff. If the ping or the migrates fails, Dexter
     * gives up on that Bailiff and tries another.
     * @param agentID
     */
    public void topLevel(String agentID) throws java.io.IOException {
        Random rnd = new Random ();
        boolean agentLocationStaus;
        String targetID = null;
        int numOfActiveAgents;

        // Create a Jini service discovery manager to help us interact with
        // the Jini lookup service.
        SDM = new ServiceDiscoveryManager (null, null);

        String bfiRoom = null;
        for (;;) {

            ServiceItem [] svcItems;

            long retryInterval = 0;

            // The restraint sleep is just there so we don't get hyperactive
            // and confuse the slow human beings.
            debugMsg(" TagGame: " + agentID +" : Status: "+getStatusType()+"    Entering restraint sleep. ");

            //snooze (5000);
            snooze(10000);
            debugMsg(" TagGame: " + agentID +" : Status: "+getStatusType()+"    Leaving restraint sleep. ");

            // Enter a loop in which Dexter tries to find some Bailiffs.

            do {

                if (0 < retryInterval) {
                    debugMsg(" TagGame: " + agentID +" : Status: "+getStatusType()+"    No Bailiffs detected - sleeping.");

                    snooze (retryInterval);
                    debugMsg(" TagGame: " + agentID +" : Status: "+getStatusType()+"    Waking up.");

                }

                // Put our query, expressed as a service template, to the Jini
                // service discovery manager.

                svcItems = SDM.lookup (bailiffTemplate, 8, null);
                retryInterval = 20 * 1000;

                // If no lookup servers are found, go back up to the beginning
                // of the loop, sleep a bit and then try again.
            } while (svcItems.length == 0);

            // We have the Bailiffs.
            debugMsg(" TagGame: " + agentID +" : Status: "+getStatusType()+"    Found "+ svcItems.length + " Bailiffs.");

            // Now enter a loop in which we try to ping and migrate to them.

            int nofItems = svcItems.length;

            // While we still have at least one Bailiff service to try...

            while (nofItems > 0) {

                // Select one Bailiff randomly.
                //              int idx = 0;
//                if (nofItems > 1) {
//
//
//                    idx = rnd.nextInt (nofItems);
//                }
                boolean accepted = false;	    // Assume it will fail
                for (int idx = 0; idx < nofItems ; idx++) { //Loop through all Bailiff
                    Object obj = svcItems[idx].service; // Get the service object
                    BailiffInterface bfi = null;

                    // Try to ping the selected Bailiff.
                    try {
                        if (obj instanceof BailiffInterface) {
                            bfi = (BailiffInterface) obj;
                            bfiRoom = bfi.getRoom();
                            debugMsg(" TagGame: " + agentID +" : Status: "+getStatusType()+"    Pings Bailiff-"+ bfiRoom);
                            String response = bfi.ping (); // Ping it
                            debugMsg (" TagGame: " +response);
                            accepted = true;	// Oh, it worked!
                        }
                    }
                    catch (java.rmi.RemoteException e) { // Ping failed
                        if (debug) {
                            e.printStackTrace ();
                        }
                    }

                    debugMsg (accepted ? "Ping Accepted." : "Ping Rejected.");
                    List activeAgents = bfi.getActiveAgents();
                    debugMsg(" TagGame: Room" + bfiRoom +" : has: "+activeAgents.size()+"    ACTIVE agents");
                    if(!activeAgents.isEmpty() && activeAgents.contains(agentID)){
                        agentLocationStaus = true;
                        //debugMsg ("Agent's "+agentID+" Current location is in Bailiff "+ bfiRoom +" with \n"+getStatusType());
                    }else {
                        agentLocationStaus = false;
                        //debugMsg ("Agent's "+agentID+ "Current location NOT in Bailiff: "+ bfiRoom + " with\n" + getStatusType());
                    }
                    // If the ping failed, delete that Bailiff from the array and try another.
                    //  The current (idx) entry in the list of service items
                    // is replaced by the last item in the list, and the list length
                    // is decremented by one.

                    if (accepted == false ) {
                        svcItems[idx] = svcItems[nofItems - 1];
                        nofItems -= 1;
                        debugMsg(" TagGame: " + agentID +" : Status: "+getStatusType()+"    Ping Rejected !!");
                        continue;		// Leaves the current loop iteration and goes to next element(BFF) in the list
                    } //TODO, if no agents found in the Bailiff

                    else {// This is the spot where Dexter tries to Tag or Migrate
                        if(getStatusType().equals(AgentStatusType.STATUS_IS_IT)){ //I am IT-PLAYER
                            int randomAgent = 0;
                            numOfActiveAgents = activeAgents.size();
                            if(numOfActiveAgents==0)
                                continue;

                            randomAgent = rnd.nextInt(numOfActiveAgents);
                            targetID = (String) activeAgents.get(randomAgent);
                            if(((activeAgents.size() > 0)) && agentLocationStaus==true){ //THis Bailiff is my current location and has Agents
                                debugMsg(" TagGame: " + agentID +" : Status: "+getStatusType()+"    Found Local Bailiff-"+bfiRoom+" with " +numOfActiveAgents+" agents,Trying to Tag" );
                               bfi.tagAgent(this,agentID,targetID);
                                debugMsg(" TagGame: " + agentID +" : Status: "+getStatusType()+"    Tagging-Attempt on Bailiff-"+bfiRoom +" finished, trying another Bailiff");
                               continue;
                            }else if(((numOfActiveAgents > 0)) && agentLocationStaus==false){ //This Bailiff has agents but not my current location
                                try {
                                    code = 1;
                                    debugMsg(" TagGame: " + agentID +" : Status: "+getStatusType()+"    Found Remote Bailiff-"+bfiRoom+" with " +numOfActiveAgents+" agents,Trying to Migrate" );
                                    bfi.migrate (this, "topLevel", new Object [] {agentID}, code,targetID);
                                    debugMsg(" TagGame: " + agentID +" : Status: "+getStatusType()+"    Successfully Migrated to Bailiff-"+bfiRoom+" to TAG a victim");
                                    SDM.terminate ();
                                    return;// SUCCESS, break code execution
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                debugMsg(" TagGame: " + agentID +" : Status: "+getStatusType()+"    Failed to Migrate to Bailiff-"+bfiRoom +" NoofAgents= "+numOfActiveAgents+" trying another Bailiff");
                                continue;

                            }
                        }else { //Agent Not IT-PLAYER
                            try {
                                code=0;
                                debugMsg(" TagGame: " + agentID +" : Status: "+getStatusType()+"    Trying to Migrate to Bailiff-"+bfiRoom);
                                boolean isitHere = bfi.isItHere();
                                if(isitHere){
                                    debugMsg(" TagGame: " + agentID +" : Status: "+getStatusType()+"    Avoided Migration to Bailiff-"+bfiRoom +" IT-PLAYER PRESENT!!!");
                                    continue;
                                }else {
                                    bfi.migrate (this, "topLevel", new Object [] {agentID}, code,targetID);
                                    debugMsg(" TagGame: " + agentID +" : Status: "+getStatusType()+"    Successfully Migrated to Bailiff-"+bfiRoom);
                                    SDM.terminate ();
                                    return;// SUCCESS, break code execution
                                }

                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }


                        }

                    }
                }

            }	// while there are candidates left
            debugMsg(" TagGame: " + agentID +" : Status: "+getStatusType()+"    Exhausted all Bailiffs trying to find MORE Bailiffs");

        } // for ever // go back up and try to find more Bailiffs
    }

    /**
     * The main program of Dexter. It is only used when a Dexter is launched.
     */
    public static void main (String [] argv) throws java.lang.ClassNotFoundException, java.io.IOException {
        CmdlnOption helpOption  = new CmdlnOption ("-help");
        CmdlnOption debugOption = new CmdlnOption ("-debug");
        CmdlnOption noFaceOption = new CmdlnOption ("-noface");
        CmdlnOption itPlayerOption= new CmdlnOption ("-it");


        CmdlnOption [] opts =
                new CmdlnOption [] {helpOption, debugOption, noFaceOption,itPlayerOption};

        String [] restArgs = Commandline.parseArgs (System.out, argv, opts);

        if (restArgs == null) {
            System.exit (1);
        }

        if (helpOption.getIsSet () == true) {
            System.out.println ("Usage: [-help]|[-debug][-noface]");
            System.out.println ("where -help shows this message");
            System.out.println ("      -debug turns on debugging.");
            System.out.println ("      -noface disables the GUI.");
            System.exit (0);
        }

        boolean debug = debugOption.getIsSet ();
        boolean noFace = noFaceOption.getIsSet ();
        boolean itPlayer = itPlayerOption.getIsSet();

        // We will try without it first
        // System.setSecurityManager (new RMISecurityManager ());
        // TODO Agent generates its own ID when it starts
        final String agentID = UUID.randomUUID().toString();

        Dexter dx = new Dexter (debug, noFace,itPlayer,agentID);
        dx.topLevel (agentID);
        System.exit (0);
    }
}
