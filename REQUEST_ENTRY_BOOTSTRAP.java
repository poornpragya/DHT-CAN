import java.io.Serializable;
import java.net.InetAddress;

@SuppressWarnings("serial")
public class REQUEST_ENTRY_BOOTSTRAP implements Serializable {
	InetAddress joiningIP;

	public REQUEST_ENTRY_BOOTSTRAP(InetAddress joiningIP) {
		this.joiningIP = joiningIP;
	}
}
