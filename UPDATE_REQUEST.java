import java.io.Serializable;
import java.util.Set;


@SuppressWarnings("serial")
public class UPDATE_REQUEST implements Serializable {

	Set<Zone> n;
	public UPDATE_REQUEST(Set<Zone> n) {
		this.n=n;
	}

}
