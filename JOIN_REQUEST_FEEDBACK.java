import java.io.Serializable;
import java.util.Set;


@SuppressWarnings("serial")
public class JOIN_REQUEST_FEEDBACK implements Serializable {

	Zone newZone;
	Set<Zone> listOfNeighbours;
	
	public JOIN_REQUEST_FEEDBACK(Zone newZone,Set<Zone> listOfNeighbours) {
		this.newZone=newZone;
		this.listOfNeighbours=listOfNeighbours;
	}

}
