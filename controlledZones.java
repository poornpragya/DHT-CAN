import java.io.Serializable;
import java.util.ArrayList;


@SuppressWarnings("serial")
public class controlledZones implements Serializable {

	Zone z;
	ArrayList<Zone> neighbours;
	
	public controlledZones(Zone z,ArrayList<Zone> neighbours) {
		this.z=z;
		this.neighbours=neighbours;
	}
	
}
