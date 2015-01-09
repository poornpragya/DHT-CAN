import java.io.Serializable;
import java.util.Set;

@SuppressWarnings("serial")
public class MERGE_REQUEST implements Serializable {
	
	Zone leavingZone;
	Set<Zone> leavingZoneNeighbourList;

	public MERGE_REQUEST(Zone leavingZone, Set<Zone> leavingZoneNeighbourList) {
		this.leavingZone=leavingZone;
		this.leavingZoneNeighbourList = leavingZoneNeighbourList;
	}
}
