import java.io.Serializable;
import java.net.InetAddress;

@SuppressWarnings("serial")
public class Zone implements Serializable {
	
	InetAddress clientIp;
	Point center;
	Point bottomleft;
	Point topright;

	public Zone() {
		this.clientIp = null;
		this.center = null;
		this.bottomleft = null;
		this.topright = null;
	}

	public Zone(Point bottomleft, Point topright) {

		this.bottomleft = bottomleft;
		this.topright = topright;
		this.center = this.newZoneCenter();
	}

	public void set(Point bottomleft, Point topright) {

		this.bottomleft = bottomleft;
		this.topright = topright;
		this.center = this.newZoneCenter();
	}

	public float length() {

		return Math.abs(bottomleft.x - topright.x);
	}

	public float breadth() {

		return Math.abs(bottomleft.y - topright.y);
	}

	public boolean isSquare() {

		if (this.length() == this.breadth())
			return true;
		else
			return false;

	}

	Point leftVerticalMid() {
		Point p = new Point();
		p.x = bottomleft.x;
		p.y = (float) (bottomleft.y + (this.breadth() / 2.0));
		return p;
	}

	Point rightVerticalMid() {
		Point p = new Point();
		p.x = topright.x;
		p.y = (float) (topright.y - (this.breadth() / 2.0));
		return p;
	}

	Point topHorizontalMid() {
		Point p = new Point();
		p.x = (float) (topright.x - (this.length() / 2.0));
		p.y = topright.y;
		return p;
	}

	Point bottomHorizontalMid() {
		Point p = new Point();
		p.x = (float) (bottomleft.x + (this.length() / 2.0));
		p.y = bottomleft.y;
		return p;
	}

	Point newZoneCenter() {
		Point p = new Point();
		p.x = (float) ((bottomleft.x + topright.x) / 2.0);
		p.y = (float) ((bottomleft.y + topright.y) / 2.0);
		return p;
	}

	Zone split() {
		Zone newZone;
		if (this.isSquare())
			newZone = this.splitHorizontally();
		else
			newZone = this.splitVertically();

		return newZone;

	}

	private Zone splitVertically() {

		Zone newZone = new Zone();
		newZone.bottomleft = this.bottomHorizontalMid();
		newZone.topright = Point.copyPoint(this.topright);
		newZone.center = newZone.newZoneCenter();

		this.bottomleft = Point.copyPoint(this.bottomleft);
		this.topright = Point.copyPoint(this.topHorizontalMid());
		this.center = this.newZoneCenter();

		return newZone;

	}

	private Zone splitHorizontally() {

		Zone newZone = new Zone();
		newZone.bottomleft = this.leftVerticalMid();
		newZone.topright = Point.copyPoint(this.topright);
		newZone.center = newZone.newZoneCenter();

		this.bottomleft = Point.copyPoint(this.bottomleft);
		this.topright = Point.copyPoint(this.rightVerticalMid());
		this.center = this.newZoneCenter();

		return newZone;
	}

	Point bottomRight() {
		Point p = new Point();
		p.x = topright.x;
		p.y = this.bottomleft.y;
		return p;
	}

	Point topLeft() {
		Point p = new Point();
		p.x = this.bottomleft.x;
		p.y = this.topright.y;
		return p;
	}

	void copyCoordinates(Zone z) {

		this.bottomleft = Point.copyPoint(z.bottomleft);
		this.topright = Point.copyPoint(z.topright);
		this.center = Point.copyPoint(z.center);

	}

	float area() {
		return (this.length()*this.breadth());
	}
	
	public String toString() {
		return "ClientName: " + this.clientIp.getHostName() + " " + "center:"
				+ this.center + "  " + "BottomLeft:" + this.bottomleft + "  "
				+ "TopRight:" + this.topright;
	}

}
