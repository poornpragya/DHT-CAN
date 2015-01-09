public class Distance {

	public Distance() {
		// TODO Auto-generated constructor stub
	}

	static float distance(Point p1, Point p2) {
		return (float) (Math.sqrt((Math.pow((p1.x - p2.x), 2))
				+ (Math.pow((p1.y - p2.y), 2))));
	}

	public static void main(String[] args) {
		Point p1 = new Point((float) 37.5, (float) 87.5);
		Point p2 = new Point((float) 12.5, (float)81.25);
		System.out.println(Distance.distance(p1, p2));
	}
}
