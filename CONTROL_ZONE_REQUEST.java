import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class CONTROL_ZONE_REQUEST implements Serializable {

	Zone leavingZone;
	ArrayList<Zone> leavingZoneNeighbours;
	ArrayList<controlledZones> leavingZoneControlledZone;

	public CONTROL_ZONE_REQUEST(Zone leavingZone,
			ArrayList<Zone> leavingZoneNeighbours,
			ArrayList<controlledZones> leavingZoneControlList) {
		this.leavingZone = leavingZone;
		this.leavingZoneNeighbours = leavingZoneNeighbours;
		this.leavingZoneControlledZone = leavingZoneControlList;
	}

}
