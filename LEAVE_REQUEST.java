import java.io.Serializable;


@SuppressWarnings("serial")
public class LEAVE_REQUEST implements Serializable 	{

	Zone leavingZone;
	public LEAVE_REQUEST(Zone leavingZone) {
		this.leavingZone=leavingZone;
	}

}
