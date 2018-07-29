// Bailiff.java -
// Fredrik Kilander, DSV (fk@dsv.su.se)
// 18-nov-2004/FK Adapted for the PRIS course.
// 2000-12-12/FK Rewrite for Java 1.3 and Jini 1.1
// 19 May 1999/FK 

package dsv.pis.gotag.bailiff;

import dsv.pis.gotag.util.CmdlnOption;
import dsv.pis.gotag.util.Commandline;
import dsv.pis.gotag.util.Logger;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.lookup.JoinManager;
import net.jini.lookup.ServiceIDListener;
import net.jini.lookup.entry.Location;
import net.jini.lookup.entry.Name;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;

/**
 * The Bailiff is a Jini service that provides an execution environment
 * for agents. The service it provides is this:
 * A serializable class may call the Bailiff's migrate() method to
 * transfer itself to the JVM of the Bailiff and there gain its own
 * thread of execution in a callback procedure of its own choice.
 *
 * The Bailiff is not mobile. Once started, the Jini JoinManager keeps
 * it alive.
 *
 * [bail-iff n 1 law officer who helps a sheriff in issuing writs and
 * making arrests. 2 (Brit.) landlord's agent or steward; manager of
 * an estate or farm. 3 (US) official in a lawcourt, esp one who takes
 * people to their seats and announces the arrival of the judge.]
 *
 *java.rmi.server.UnicastRemoteObject	// for RMI
 * implements dsv.pis.gotag.bailiff.BailiffInterface // for clients
 * @author Fredrik Kilander, DSV
 */
public class Bailiff extends java.rmi.server.UnicastRemoteObject implements dsv.pis.gotag.bailiff.BailiffInterface {
    protected boolean debug = false;
    protected Logger log;
    protected String user;
    protected String room;
    protected String host;
    protected Map propertyMap;
    protected  DexterFace dexterFace = null;
    protected int counter = 0;


    //protected Map dexMap = Collections.synchronizedMap (new HashMap ());

   protected Map<String,Dexter> mp = new HashMap<String,Dexter>();
   protected Map<String, Dexter> agents = Collections.synchronizedMap(mp);
    protected JoinManager bf_joinmanager;
    protected InetAddress myInetAddress;
    static JFrame bff;
    protected void debugMsg (String s) {
        if (debug) {
            System.out.println (s);
        }
    }

    /**
     * Returns the roomname string of this Bailiff.
     * @return The room name string.
     */
    public String getRoom () {
        return room;
    }

    /**
     * Returns the username string of this Bailiff.
     * @return The user name string.
     */
    public String getUser () {
        return user;
    }

    /**
     * Returns the host name string of this Bailiff
     * @return The host name string.
     */
    public String getHost () {
        return host;
    }

    /**
     * A helper class to receive callbacks from the JoinManager.
     *
     * implements ServiceIDListener	// for JoinManager
     * @author Fredrik Kilander, DSV
     * @see JoinManager
     * @see ServiceIDListener
     */
    private class IDListener implements ServiceIDListener{
        /**
         * The ServiceID returned by the JoinManager.
         */
        protected ServiceID myServiceID;

        /**
         * Creates a new IDListener.
         */
        public IDListener () {}

        /**
         * This method is called by the JoinManager once it has registered
         * the service with a Lookup server and obtained a service ID.
         * @param sidIn The service's ServiceID.
         */
        public void serviceIDNotify (ServiceID sidIn) {
            myServiceID = sidIn;
            if (debug) {
                debugMsg ("serviceIDNotify sid='" + myServiceID + "'");
                log.entry ("<serviceIDNotify sidIn=\"" + sidIn + "\"/>");
            }
        }

        /**
         * Returns the ServiceID.
         * @return The ServiceID or null if not set.
         */
        public ServiceID getServiceID () {
            return myServiceID;
        }
    } // IDListener

    /**
     * This class wraps and encapsulates the remote object to which the
     * Bailiff lends a thread of execution.
     */
    private class agitator extends Thread {

        protected Object myObj;	// The client object
        protected String myCb;	// The name of the entry point method
        protected Object [] myArgs;	// Arguments to the entry point method
        protected java.lang.reflect.Method myMethod; // Ref. to entry point method
        protected Class [] myParms; // Class reflection of arguments
        protected BailiffFrame bailiffFrame;

        protected String agentID;
        protected DexterFace dexFace;
        private String count;

        /**
         * Creates a new agitator by copying th references to the client
         * object, the name of the entry method and the arguments to
         * the entry method.
         * @param obj The client object, holding the method to execute
         * @param cb  The name of the entry point method (callback)
         * @param args Arguments to the entry point method(Agent ID and JFrame)
         * @paramdexterFace
         * @paramcounter
         * @paramdexFace
         * @parambff
         */
        public agitator(Object obj, String cb, Object[] args, int cnt) {
            myObj = obj;
            myCb = cb;
            myArgs = args;
            count = String.valueOf(cnt);
            dexFace = new DexterFace ();
            agentID = args[0].toString();

            // If the array of arguments are non-zero we must create an array
            // of Class so that we can match the entry point method's name with
            // the parameter signature. So, the myParms[] array is loaded with
            // the class of each entry point parameter.

            if (0 < args.length) {
                myParms = new Class [args.length];
                for (int i = 0; i < args.length; i++) {
                    myParms[i] = args[i].getClass ();
                }
            }
            else { myParms = null; }
        }

        /**
         * This method locates the method that is the client object's requested
         * entry point. It also sets the classloader of the current instance
         * to follow the client's classloader.
         * @throws NoSuchMethodException Thrown if the entry point specified
         * in the constructor can not be found.
         */
        public void initialize () throws java.lang.NoSuchMethodException {
            myMethod = myObj.getClass ().getMethod (myCb, myParms);
            setContextClassLoader (myObj.getClass ().getClassLoader ());
        }

        /**
         * Overrides the default run() method in class Thread (a superclass to
         * us). Then we invoke the requested entry point on the client object.
         * @myArgs contains two parameters, JFrame and Agent ID
         */
        public void run () {
            try {

                agents.put(agentID, (Dexter) myObj);

                if(myArgs.length > 0){
                    debugMsg(" RUN method" + agentID);
                    myMethod.invoke (myObj, myArgs);
                    debugMsg("Migration "+ propertyMap.get(myArgs[0]).toString()+"New Agent arrived at \n");
                    debugMsg("agentID at RUN " + agentID); }

            }
            catch (Throwable t) {
                if (debug) {
                    log.entry (t);
                }
            }finally {
                agents.remove(agentID);
                debugMsg(" Agent at Counter= "+counter+" removed from MAP" + agentID);

            }


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

    } // class agitator



    // In BailiffInterface:

    /**
     * Returns a string acknowledging the host, IP address, room and user
     * fields of this Bailiff instance. This method can be used to debug
     * the identity of the Bailiff from a client and to verify that the
     * connection is still operational.
     * @returns The ping response.
     * @throws RemoteException
     */
    public String ping () throws java.rmi.RemoteException
    {
        if (debug) {
            log.entry ("<ping/>");
        }

        return ("Ping echo from Bailiff of Room: " + " [" + room+ "] " + " user=" + user + ".");
    }

//    public String ping () throws java.rmi.RemoteException
//    {
//        if (debug) {
//            log.entry ("<ping/>");
//        }
//
//        return ("Ping echo from Bailiff on host=" + host
//                + " [" + myInetAddress.getHostAddress () + "] "
//                + " room=" + room
//                + " user=" + user
//                + ".");
//    }
    public List<String> getActiveAgents () {

        Iterator<String> activeAgents = agents.keySet().iterator();
        List listofAgents = new ArrayList();
        // Iterate over all the elements
        while (activeAgents.hasNext()) {
            String agentID = activeAgents.next();
            listofAgents.add(agentID);
        }

//        if (debug) {
//            log.entry ("<getProperty key=\"" + key + "\"/>");
//        }
//        return (String) propertyMap.get (key.toLowerCase ());
        return listofAgents;
    }


    // In BailiffInterface:

    /**
     * Returns the string property stored under key.
     * @param key The key to look up.
     * @returns The property value.
     */
    public String getProperty (String key)
    {
        if (debug) {
            log.entry ("<getProperty key=\"" + key + "\"/>");
        }
        return (String) propertyMap.get (key.toLowerCase ());
    }

    // In BailiffInterface:

    /**
     * Sets the property value to be stored under key.
     * @param key The name of the property.
     * @param value The value of the property.
     */
    public void setProperty (String key, String value)
    {
        if (debug) {
            log.entry ("<setProperty key=\""
                    + key.toLowerCase () + "\" value=\"" + value + "\"/>");
        }
        propertyMap.put (key.toLowerCase (), value);
    }

    // In BailiffInterface:

    /**
     * Entry point for remote clients who want to pass an object to be
     * executed by the Bailiff. The Bailiff starts a new thread for the
     * object and calls the specified entry (callback) method. When that
     * method returns, the thread exits and the object becomes inert.
     * @param obj The object to execute.
     * @param cb  The name of the entry (callback) method to call.
     * @param args Array of arguments to the entry method. The elements in
     * the array must match the entry method's signature.
     * @paramagentID
     * @throws NoSuchMethodException Thrown if the specified entry method
     * does not exist with the expected signature.
     */
    public ArrayList<Object> migrate(Object obj, String cb, Object[] args) throws java.rmi.RemoteException,java.lang.NoSuchMethodException {
        if (debug) { log.entry ("<migrate obj=\"" + obj + "\" cb=\"" + cb + "\" args=\"" + args + "\"/>"); }
        counter++;

        agitator agt = new agitator (obj, cb,args,counter);
        agt.initialize ();
        agt.start ();
        debugMsg(" Agitator started, COUNTER= " + counter);

        ArrayList<Object> list = new ArrayList<Object>();

        //TODO: The agent should be added to the map when agitator starts
        list.add(0,dexterFace);
        list.add(1,counter);
        return list;
    }


    @Override
    public boolean isItHere() throws RemoteException {
        Iterator<String> activeAgents = agents.keySet().iterator();
        boolean status = false;
        // Iterate over all the elements
        while (activeAgents.hasNext()) {
            String agentID = activeAgents.next();
            if ( agents.get(agentID).getStatusType()==AgentStatusType.STATUS_isIT) {
                status = true;
            }
        }

        return status;
    }

    @Override
    public boolean tagAgent(String agentID)throws RemoteException {
        boolean tagStatus = false;
        Dexter dex = agents.get(agentID);
        if(!(dex ==null) && (dex.getStatusType()== AgentStatusType.STATUS_TAGGABLE)){
            dex.setStatusType(AgentStatusType.STATUS_isIT);
            tagStatus = true;
        }else{
           tagStatus = false;
        }
        return tagStatus;
    }

    public AgentStatusType agentStatusType(String){


        return null;
    }


    /**
     * Creates a new Bailiff service instance.
     * @param room Informational text field used to designate the 'room'
     * (physical or virtual) the Bailiff is running in.
     * @param user Information text field used to designate the 'user'
     * who is associated with the Bailiff instance.
     * @param debug If true, diagnostic messages will be logged to the
     * provided Logger instance. This parameter is overridden if the
     * class local debug variable is set to true in the source code.
     * @param log If debug is true, this parameter can be a Logger instance
     * configured to accept entries. If log is null a default Logger instance
     * is created.
     * @throws RemoteException
     * @throwsUnknownHostException Thrown if the local host address can not
     * be determined.
     * @throws IOException Thrown if there is an I/O problem.
     */
    public Bailiff (String room, String user, boolean debug, Logger log)
            throws
            java.rmi.RemoteException,
            java.net.UnknownHostException,
            java.io.IOException
    {
        this.log = (log == null) ? new Logger () : log;
        this.user = user;
        this.room = room;
        myInetAddress = java.net.InetAddress.getLocalHost ();
        host = myInetAddress.getHostName ().toLowerCase ();
        this.debug = (this.debug == true) ? true : debug;

        propertyMap = Collections.synchronizedMap (new HashMap ());
        //propertyMap.put ("hostname", host);
        //propertyMap.put ("hostaddress", myInetAddress.getHostAddress ());

        log.entry ("STARTING host=" + host + ", room=" + room + ", user="
                + user + ", debug=" + debug + ".");

        // Create Jini service attributes.

        Entry [] bf_attributes =
                new Entry [] {
                        new Name ("Bailiff"),
                        new Location (host, room, user)
                        //      ,
                        //	new BailiffServiceType (host, room, user)
                };

        // Create a Jini JoinManager that will help us to register ourselves
        // with all discovered Jini lookup servers.

        bf_joinmanager = new JoinManager
                (
                        this,			// the service object
                        bf_attributes,		// the attribute sets
                        new IDListener (),	// Service ID callback
                        null,			// Default Service Discovery Manager
                        null			// Default Lease Renewal Manager
                );
        //new Thread(new ListenerThread()).start();
    }

    /**
     * Shuts down this Bailiff service.
     */
    public void shutdown () {
        bf_joinmanager.terminate ();
    }

    /**
     * Returns a string representation of this service instance.
     * @returns A string representing this Bailiff instance.
     */
    public String toString () {
        return
                "Bailiff for user " +
                        user + " in room " + room + " on host " + host + ".";
    }

    /**
     * This is the main program of the Bailiff launcher. It starts the
     * Bailiff and registers it with the Jini lookup server(s).
     * When the main routine exits the JVM will
     * keep on running because the JoinManager will be running and referring
     * to the Bailiff. There may also be agitator threads active. Some house-
     * holding counters and a shutdown method are attractive extensions.
     *
     * @param argv The array of commandline strings, Java standard.
     * @exception java.net.UnknownHostException Thrown if the name of the
     * local host cannot be obtained.
     * @exception java.rmi.RemoteException Thrown if there is a RMI problem.
     * @exception java.io.IOException Thrown if discovery/join could not start.
     * @seeBailiffServiceID
     * @seeBailiff_svc
     *
     */

    public static void main (String[] argv) throws java.net.UnknownHostException, java.rmi.RemoteException,
            java.io.IOException {
        String room = "anywhere";
        String user = System.getProperty ("user.name");
        boolean debug = false;


        CmdlnOption helpOption  = new CmdlnOption ("-help");
        CmdlnOption noFrameOption = new CmdlnOption ("-noframe");
        CmdlnOption debugOption = new CmdlnOption ("-debug");
        CmdlnOption roomOption  = new CmdlnOption ("-room",
                CmdlnOption.OPTIONAL|
                        CmdlnOption.PAR_REQ);
        CmdlnOption userOption = new CmdlnOption ("-user",
                CmdlnOption.OPTIONAL|
                        CmdlnOption.PAR_REQ);
        CmdlnOption logOption = new CmdlnOption ("-log",
                CmdlnOption.OPTIONAL|
                        CmdlnOption.PAR_OPT);

        CmdlnOption [] opts =
                new CmdlnOption [] {helpOption,
                        debugOption,
                        roomOption,
                        userOption,
                        logOption};

        String [] restArgs = Commandline.parseArgs (System.out, argv, opts);

        if (restArgs == null) {
            System.exit (1);
        }

        if (helpOption.getIsSet () == true) {
            System.out.println
                    ("Usage: [-room room][-user user][-debug][-log [logfile]]");
            System.out.print ("Where room is location of the service ");
            if (room == null) {
                System.out.println ("(no default).");
            }
            else {
                System.out.println ("(default = '" + room + "').");
            }

            System.out.print ("      -user specifies the owning user ");
            if (user == null) {
                System.out.println ("(no default available).");
            }
            else {
                System.out.println ("(default = '" + user.toLowerCase () + "').");
            }

            System.out.println ("      -debug turns on debugging mode.");
            System.out.println ("      -log turns on logging to file.");

            System.exit (0);
        }

        debug = debugOption.getIsSet ();

        if (roomOption.getIsSet () == true) {
            room = roomOption.getValue ().toLowerCase ();
        }

        if (userOption.getIsSet () == true) {
            user = userOption.getValue ().toLowerCase ();
        }

        Logger log;

        if (logOption.getIsSet () == true) {
            String lg = logOption.getValue ();
            log =
                    (lg != null) ? new Logger (lg, true) : new Logger (".", "Bailiff");
        }
        else {
            log = new Logger ();
        }

        // Set the RMI security manager.
        System.setSecurityManager (new RMISecurityManager ());
        Bailiff bf = new Bailiff (room, user, debug, log);
        if (noFrameOption.getIsSet () == false) {

            bff = new BailiffFrame (bf);

            //JFrame frame = new JFrame("Applet in Frame");

            //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            //frame.add( applet );
            //frame.pack();
            //frame.setLocationRelativeTo( null );
            //frame.setVisible( true );


        }
    } // main

    private class ListenerThread implements  Runnable{
        public ListenerThread(){ }
        @Override
        public void run(){
            try {
                for(;true;){
                    Iterator<String> agentID = agents.keySet().iterator();

                    // Iterate over all the elements
                    while (agentID.hasNext()) {
                        String key = agentID.next();
                        if (true) {
                            // TODO Stop animation
                            dexterFace.stopAnimation ();
                            agentID.remove();
                        }
                    }
                }
            } catch (Throwable Failure){
            }
        }

    }


} // public class Bailiff

