import java.io.Serializable;
import java.net.InetAddress;
import java.util.LinkedList;

// request used by client only for joining
@SuppressWarnings("serial")
public class JOIN_REQUEST implements Serializable {

	InetAddress joiningNodeIpAddress;
	Point requestedPoint;
	
	LinkedList<Zone> visitedZones;
	
	public JOIN_REQUEST(InetAddress nodeAddress,Point point) {
		this.joiningNodeIpAddress=nodeAddress;
		this.requestedPoint=point;
		visitedZones=new LinkedList<Zone>();
	}
}
