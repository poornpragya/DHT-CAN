import java.io.Serializable;
import java.net.InetAddress;
import java.util.LinkedList;


@SuppressWarnings("serial")
public class FILE_GET_REQUEST implements Serializable {

	String filename;
	Point p;
	InetAddress sourceIp;
	LinkedList<Zone> visited;
	
	public FILE_GET_REQUEST(String filename, Point p,InetAddress sourceIp) {
		this.filename=filename;
		this.p = p;
		this.sourceIp=sourceIp;
		visited=new LinkedList<Zone>();
	}
}
