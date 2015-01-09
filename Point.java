import java.io.Serializable;

@SuppressWarnings("serial")
public class Point implements Serializable {

	float x, y;

	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Point() {
		this.x = -1;
		this.y = -1;
	}

	void set(float x, float y) {
		this.x = x;
		this.y = y;
	}

	boolean liesInZone(Zone z) {
		// System.out.println("In liesInZone");
		if (this.x == 0 && z.bottomleft.x == 0 && this.y > z.bottomleft.y
				&& this.y <= z.topright.y)
			return true;

		if (this.x > z.bottomleft.x && this.x <= z.topright.x && this.y == 0
				&& z.bottomleft.y == 0)
			return true;

		else if (this.x > z.bottomleft.x && this.x <= z.topright.x
				&& this.y > z.bottomleft.y && this.y <= z.topright.y)
			return true;

		else
			return false;

	}

	boolean equals(Point p) {
		if (this.x == p.x && this.y == p.y)
			return true;
		else
			return false;
	}

	static Point copyPoint(Point p) {
		return new Point(p.x, p.y);
	}

	float distance(Point p) {
		return (float) (Math.sqrt((Math.pow((this.x - p.x), 2))
				+ (Math.pow((this.y - p.y), 2))));
	}

	public String toString() {
		return "(" + x + "," + y + ")";
	}
}
