package dsv.pis.chat.client;

public interface ChatClientInterface extends java.rmi.Remote{
    /**
     * Returns the client's user-friendly name.
     * @return The client's user-friendly name.
     */
    public String getClientName () throws java.rmi.RemoteException;
}

