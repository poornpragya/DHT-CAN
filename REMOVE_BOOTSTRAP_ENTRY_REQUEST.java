import java.io.Serializable;
import java.net.InetAddress;

@SuppressWarnings("serial")
public class REMOVE_BOOTSTRAP_ENTRY_REQUEST implements Serializable {
	InetAddress leavingIP;

	public REMOVE_BOOTSTRAP_ENTRY_REQUEST(InetAddress leavingIP) {
		this.leavingIP = leavingIP;
	}
}
