import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("serial")
public class BootStrap_Server implements Runnable,Serializable {

	static CopyOnWriteArrayList<InetAddress> IpTable;
	
	Socket s;
	Thread t;
	
	static {
		IpTable=new CopyOnWriteArrayList<InetAddress>();
	}

	public BootStrap_Server(Socket s) {
		this.s = s;
		t=new Thread(this);
		t.start();
	}

	void addNewNode(InetAddress ip) {
		BootStrap_Server.IpTable.add(ip);
	}

	InetAddress lookUp() {
		if (BootStrap_Server.IpTable.isEmpty())
			return null;
		else {
			Random rand = new Random();
			return IpTable.get(rand.nextInt(BootStrap_Server.IpTable.size()));
		}
	}

	void printIPTable() {
		for(InetAddress I:IpTable) {
			System.out.println(I.getHostName()+"  "+I.getHostAddress());
		}
	}
	
	public void run() {
		synchronized (BootStrap_Server.IpTable) {
			try {
			ObjectInputStream in=new ObjectInputStream(s.getInputStream());
			Object receivedObject=in.readObject();
			
			if(receivedObject instanceof REQUEST_ENTRY_BOOTSTRAP) {
				
				REQUEST_ENTRY_BOOTSTRAP reb=(REQUEST_ENTRY_BOOTSTRAP) receivedObject;
				System.out.println("Node Joining: " + reb.joiningIP.getHostName());
				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
				InetAddress dest = reb.joiningIP;
				out.writeObject(this.lookUp());
				this.addNewNode(dest);
				out.close();
				printIPTable();
				System.out.println();
			}
			
			else if(receivedObject instanceof REMOVE_BOOTSTRAP_ENTRY_REQUEST) {
				
				REMOVE_BOOTSTRAP_ENTRY_REQUEST rbe=(REMOVE_BOOTSTRAP_ENTRY_REQUEST) receivedObject;
				System.out.println("Node leaving:" + rbe.leavingIP.getHostName());
				for(InetAddress ip:IpTable) {
					if(ip.equals(rbe.leavingIP))
						IpTable.remove(ip);
				}
				printIPTable();
				System.out.println();
			}
			
			else {
				System.out.println("Unable to classify the request");
			}
			in.close();
			s.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	public static void main(String[] args) throws IOException {
		@SuppressWarnings("resource")
		ServerSocket ss = new ServerSocket(20000);
		System.out.println("Server running");
		while (true) {
			new BootStrap_Server(ss.accept());
			//System.out.println("client trying to access BootStrap server");
		}
	}
}
