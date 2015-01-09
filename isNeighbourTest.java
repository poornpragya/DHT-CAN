import java.util.ArrayList;

public class isNeighbourTest {
	boolean isNeighbour() {
		
		Zone z1;
		Zone z2;
		
		ArrayList<Point> zone1 = new ArrayList<Point>();
		ArrayList<Point> zone2 = new ArrayList<Point>();
		
		z1 = new Zone(new Point((float) 50.0, (float) 25.0), new Point(
				(float) 62.5, (float) 37.5));
		z2 = new Zone(new Point((float) 75.0, (float) 25.0), new Point(
				(float) 100.0, (float) 50.0));
		zone1.add(z1.bottomleft);
		zone1.add(z1.bottomRight());
		zone1.add(z1.topLeft());
		zone1.add(z1.topright);
		zone2.add(z2.bottomleft);
		zone2.add(z2.bottomRight());
		zone2.add(z2.topLeft());
		zone2.add(z2.topright);
		
		if (z1.bottomleft.equals(z2.bottomleft)
				&& z1.bottomRight().equals(z2.bottomRight())
				&& z1.topLeft().equals(z2.topLeft())
				&& z1.topright.equals(z2.topright))
			return false;
		
		else if (z1.topright.equals(z2.bottomleft)
				|| z1.topLeft().equals(z2.bottomRight())
				|| z1.bottomRight().equals(z2.topLeft())
				|| z1.bottomleft.equals(z2.topright)) // Condition 2
			return false;
		
		else if (z1.bottomleft.y == z2.topright.y
				&& ((z1.bottomleft.x >= z2.topLeft().x) && (z1.bottomRight().x <= z2.topright.x))

				|| z1.bottomleft.y == z2.topright.y
				&& ((z2.topLeft().x >= z1.bottomleft.x) && (z2.topright.x <= z1
						.bottomRight().x))

				|| z2.bottomleft.y == z1.topright.y
				&& ((z2.bottomleft.x >= z1.topLeft().x) && (z2.bottomRight().x <= z1.topright.x))

				|| z2.bottomleft.y == z1.topright.y
				&& ((z1.topLeft().x >= z2.bottomleft.x) && (z1.topright.x <= z2
						.bottomRight().x))

				|| z1.topright.x == z2.bottomleft.x
				&& ((z2.topLeft().y <= z1.topright.y) && (z2.bottomleft.y >= z1
						.bottomRight().y))

				|| z1.bottomleft.x == z2.topright.x
				&& ((z1.topLeft().y <= z2.topright.y) && (z1.bottomleft.y >= z2
						.bottomRight().y))

				|| z2.bottomleft.x == z1.topright.x
				&& ((z1.topright.y <= z2.topLeft().y) && (z1.bottomRight().y >= z2.bottomleft.y))

				|| z2.topright.x == z1.bottomleft.x
				&& ((z2.topright.y <= z1.topLeft().y && (z2.bottomRight().y >= z1.bottomleft.y))))
			return true;
		
		else
			return false;
	}

	public static void main(String[] args) {
		isNeighbourTest test = new isNeighbourTest();
		System.out.println(test.isNeighbour());
	}
}