import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

@SuppressWarnings("serial")
public class Client implements Runnable, Serializable {

	static CopyOnWriteArraySet<Zone> neighbours;
	static Zone z;
	private static boolean isNotServer;
	private static boolean isPeriodicThread;
	private static InetAddress bootStrapIp;
	static CopyOnWriteArrayList<controlledZones> controlledZones;
	File sharedFloder;

	Socket s;
	Thread t;
	Thread t1;

	static {
		neighbours = new CopyOnWriteArraySet<Zone>();
		isNotServer = false;
		isPeriodicThread = true;
		controlledZones = new CopyOnWriteArrayList<controlledZones>();
		z = new Zone();
		try {
			z.clientIp = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public Client() throws IOException, InterruptedException {
		t = new Thread(this);
		t.start();
		Thread.sleep(10);
		t1 = new Thread(this);
		t1.start();

	}

	public Client(Socket s) {
		this.s = s;
		t = new Thread(this);
		t.start();

	}

	boolean isNeighbour(Zone z1, Zone z2) {

		ArrayList<Point> zone1 = new ArrayList<Point>();
		ArrayList<Point> zone2 = new ArrayList<Point>();

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

	void nodeJoin(InetAddress bsip) throws IOException, ClassNotFoundException {

		Socket s = new Socket(bsip, 20000);
		REQUEST_ENTRY_BOOTSTRAP reb = new REQUEST_ENTRY_BOOTSTRAP(
				InetAddress.getLocalHost());
		ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
		out.writeObject(reb);

		ObjectInputStream in = new ObjectInputStream(s.getInputStream());
		InetAddress receivedIp = (InetAddress) in.readObject();
		out.close();
		in.close();
		s.close();

		if (receivedIp == null) {
			System.out.println("Received Ip from Server: null");
			z.set(new Point(0, 0), new Point(100, 100));
			System.out.println("Zone obtained-->");
			System.out.println(z);

		} else {
			System.out.println("Received Ip from Server:"
					+ receivedIp.getHostName());

			Random r = new Random();
			Point p = new Point(r.nextInt(100) + 1, r.nextInt(100) + 1);
			// Scanner sc = new Scanner(System.in);
			// System.out.println("Enter random point to select zone:");
			// Point p = new Point(sc.nextInt(), sc.nextInt());
			System.out.println("Random point chosen" + p);
			s = new Socket(receivedIp, 15000);
			out = new ObjectOutputStream(s.getOutputStream());
			JOIN_REQUEST jreq = new JOIN_REQUEST(InetAddress.getLocalHost(), p);
			out.writeObject(jreq);
			out.close();
			s.close();
		}

	}

	@SuppressWarnings({ "unchecked" })
	void requestRouting(JOIN_REQUEST jreq) throws IOException {

		if (jreq.requestedPoint.liesInZone(z)) {
			Socket s = new Socket(jreq.joiningNodeIpAddress, 15000);
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			Zone newZone = z.split();
			newZone.clientIp = jreq.joiningNodeIpAddress;
			// System.out.println("Updated Zone-->");
			// System.out.println(z);
			Set<Zone> temp = new HashSet<Zone>(neighbours);
			temp.add(z);
			out.writeObject(new JOIN_REQUEST_FEEDBACK(newZone, temp));
			neighbours.add(newZone);
			this.updateNeighbours();
			out.close();
			s.close();

		} else if (!controlledZones.isEmpty()) {
			System.out.println("Inside !controlledZones.isEmpty()");

			synchronized (Client.class) {
				controlledZones emptyZone = controlledZones.get(0);
				controlledZones.remove(0);
				emptyZone.z.clientIp = jreq.joiningNodeIpAddress;
				Set<Zone> temp = new HashSet<Zone>(emptyZone.neighbours);
				temp.add(z);
				Socket s = new Socket(jreq.joiningNodeIpAddress, 15000);
				ObjectOutputStream out = new ObjectOutputStream(
						s.getOutputStream());
				out.writeObject(new JOIN_REQUEST_FEEDBACK(emptyZone.z, temp));
				neighbours.add(emptyZone.z);
				this.updateNeighbours();
				out.close();
				s.close();
			}
		} else {

			// System.out.println("Else of requestRouting");
			@SuppressWarnings("rawtypes")
			Set<Zone> temp = new HashSet(neighbours);
			float minDist = Float.MAX_VALUE;
			Zone nextNeighbour = null;
			boolean flag = true;
			for (Zone zone : temp) {
				for (Zone z1 : jreq.visitedZones) {
					if (zone.clientIp.equals(z1.clientIp)) {
						flag = false;
						break;
					}
				}
				if (z.center.distance(zone.center) < minDist && flag) {

					minDist = z.center.distance(zone.center);
					nextNeighbour = zone;
				}
				flag = true;
			}

			if (nextNeighbour != null) {
				jreq.visitedZones.add(z);
				// System.out.println("Inside");
				Socket s = new Socket(nextNeighbour.clientIp, 15000);
				ObjectOutputStream out = new ObjectOutputStream(
						s.getOutputStream());
				out.writeObject(jreq);
				out.close();
				s.close();
			} else {
				System.out.println("Backtracking");
				Zone prevVisitedZone = jreq.visitedZones.getLast();
				jreq.visitedZones.add(z);
				Socket s = new Socket(prevVisitedZone.clientIp, 15000);
				ObjectOutputStream out = new ObjectOutputStream(
						s.getOutputStream());
				out.writeObject(jreq);
				out.close();
				s.close();
			}
		}
	}

	private void updateNeighbours() {

		synchronized (Client.class) {
			Set<Zone> temp = new HashSet<Zone>();
			for (Zone zone : neighbours) {
				if (!this.isNeighbour(z, zone))
					temp.add(zone);
			}

			for (controlledZones cz : Client.controlledZones) {
				for (Zone zone : cz.neighbours) {
					for (Zone zone2 : temp) {
						if (zone.clientIp.equals(zone2.clientIp))
							temp.remove(zone2);
					}
				}
			}

			for (Zone zone : temp) {
				neighbours.remove(zone);
			}
		}

		// System.out.println();
		// System.out.println("Updating Itself");
		// this.printNeighbour();
	}

	boolean nodeExistsNeighbour(Zone zone) {
		for (Zone z1 : neighbours) {
			if (z1.clientIp.equals(zone.clientIp)) {
				// System.out.println("node exists in neighbours");
				return true;
			}

		}
		return false;
	}

	void deleteZoneFromNeighbour(Zone incomming) {
		for (Zone z1 : neighbours) {
			if (z1.clientIp.equals(incomming.clientIp))
				neighbours.remove(z1);

		}

	}

	private void updateNeighbours(UPDATE_REQUEST ureq) {

		synchronized (Client.class) {

			for (Zone z1 : ureq.n) {
				if (nodeExistsNeighbour(z1)) {
					this.deleteZoneFromNeighbour(z1);
					neighbours.add(z1);
				} else {
					neighbours.add(z1);
				}
			}

			Set<Zone> temp = new HashSet<Zone>();
			for (Zone zone : neighbours) {
				if (!this.isNeighbour(z, zone))
					temp.add(zone);
			}

			for (Zone zone : temp)
				neighbours.remove(zone);
		}
	}

	private void sendMergedZoneToLeavingZoneNeighbours(MERGE_REQUEST mreq)
			throws IOException {

		HashSet<Zone> temp = new HashSet<Zone>();
		temp.add(z);
		UPDATE_REQUEST ureq = new UPDATE_REQUEST(temp);

		for (Zone zone : mreq.leavingZoneNeighbourList) {
			if (!zone.clientIp.equals(z.clientIp)) {
				Socket s = new Socket(zone.clientIp, 15000);
				ObjectOutputStream out = new ObjectOutputStream(
						s.getOutputStream());
				out.writeObject(ureq);
				out.close();
				s.close();
			}
		}

	}

	private void addToControlledZoneSet(CONTROL_ZONE_REQUEST creq) {

		synchronized (Client.class) {

			creq.leavingZone.clientIp = null;
			Client.controlledZones.add(new controlledZones(creq.leavingZone,
					creq.leavingZoneNeighbours));

			for (controlledZones z : creq.leavingZoneControlledZone) {
				Client.controlledZones.add(z);
			}

			for (Zone zone : creq.leavingZoneNeighbours) {
				if (!z.clientIp.equals(zone.clientIp))
					Client.neighbours.add(zone);
			}
		}

	}

	private void mergeIncommingZone(MERGE_REQUEST mreq) {

		if (z.topLeft().equals(mreq.leavingZone.bottomleft)
				&& z.topright.equals(mreq.leavingZone.bottomRight())) {
			z.topright = mreq.leavingZone.topright;
			z.center = z.newZoneCenter();
		}

		else if (z.bottomleft.equals(mreq.leavingZone.topLeft())
				&& z.bottomRight().equals(mreq.leavingZone.topright)) {
			z.bottomleft = mreq.leavingZone.bottomleft;
			z.center = z.newZoneCenter();
		}

		else if (z.topright.equals(mreq.leavingZone.topLeft())
				&& z.bottomRight().equals(mreq.leavingZone.bottomleft)) {
			z.topright = mreq.leavingZone.topright;
			z.center = z.newZoneCenter();
		}

		else if (z.topLeft().equals(mreq.leavingZone.topright)
				&& z.bottomleft.equals(mreq.leavingZone.bottomRight())) {
			z.bottomleft = mreq.leavingZone.bottomleft;
			z.center = z.newZoneCenter();
		} else
			System.out
					.println("Cant merge the incomming zone in mrege request");

	}

	private void removeLeavingNode(LEAVE_REQUEST lreq) {
		synchronized (Client.class) {
			this.deleteZoneFromNeighbour(lreq.leavingZone);
		}

	}

	private void addNeighbours(Set<Zone> listOfNeighbours) throws IOException {

		for (Zone neighbour : listOfNeighbours) {
			if (this.isNeighbour(z, neighbour)) {
				neighbours.add(neighbour);
			}
		}

		// System.out.println();
		// System.out.println("Node table of new Node while Node joining-->");
		// printNeighbour();
		this.sendNeighbourTableUpdate();
	}

	void sendNeighbourTableUpdate() throws IOException {

		Set<Zone> temp = new HashSet<Zone>(neighbours);
		temp.add(z);
		UPDATE_REQUEST ureq = new UPDATE_REQUEST(temp);

		for (Zone zone : neighbours) {
			Socket s = new Socket(zone.clientIp, 15000);
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			out.writeObject(ureq);
			out.close();
			s.close();
		}
	}

	void printNeighbour() {
		for (Zone zone : neighbours) {
			System.out.println(zone);
		}
	}

	static void setZone(Zone zone) {
		z = zone;
	}

	void sendLeaveRequest() throws IOException {
		LEAVE_REQUEST lreq = new LEAVE_REQUEST(Client.z);
		for (Zone zone : neighbours) {
			Socket s = new Socket(zone.clientIp, 15000);
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			out.writeObject(lreq);
			out.close();
			s.close();
		}
	}

	private void nodeLeave() throws IOException {
		removeEntryFromBootStrap();
		sendLeaveRequest();
		giveUpZone(identifyZone());

		System.exit(0);
	}

	private void giveUpZone(Zone chosenZone) throws IOException {
		if (chosenZone != null)
			sendMergeRequest(chosenZone);
		else {
			if (!neighbours.isEmpty()) {
				ArrayList<Zone> sortedZone = sortedZone();
				controlThisZone(sortedZone.get(0));
			}
		}
	}

	private void controlThisZone(Zone chosenZone) throws IOException {
		CONTROL_ZONE_REQUEST creq = new CONTROL_ZONE_REQUEST(z,
				new ArrayList<Zone>(neighbours),
				new ArrayList<controlledZones>(Client.controlledZones));
		Socket s = new Socket(chosenZone.clientIp, 15000);
		ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
		out.writeObject(creq);
		out.close();
		s.close();
	}

	private void sendMergeRequest(Zone chosenZone) throws IOException {
		MERGE_REQUEST mreq = new MERGE_REQUEST(Client.z, new HashSet<Zone>(
				neighbours));
		Socket s = new Socket(chosenZone.clientIp, 15000);
		ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
		out.writeObject(mreq);
		out.close();
		s.close();
	}

	private void removeEntryFromBootStrap() throws IOException {
		REMOVE_BOOTSTRAP_ENTRY_REQUEST rbe = new REMOVE_BOOTSTRAP_ENTRY_REQUEST(
				z.clientIp);
		Socket s = new Socket(bootStrapIp, 20000);
		ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
		out.writeObject(rbe);
		out.close();
		s.close();
	}

	boolean isMergable(Zone zone) {
		if (((z.isSquare() && zone.isSquare()) || (!z.isSquare() && !zone
				.isSquare()))
				&& ((z.bottomleft.x == zone.bottomleft.x && z.topright.x == zone.topright.x) || (z.bottomleft.y == zone.bottomleft.y && z.topright.y == zone.topright.y)))
			return true;
		else
			return false;
	}

	public ArrayList<Zone> sortedZone() {
		HashMap<Float, Zone> map = new HashMap<Float, Zone>();
		ArrayList<Float> temp = new ArrayList<Float>();
		ArrayList<Zone> sortedZone = new ArrayList<Zone>();
		for (Zone zone : neighbours) {
			map.put(zone.area(), zone);
			temp.add(zone.area());
		}
		Collections.sort(temp);
		for (int i = 0; i < temp.size(); i++) {
			sortedZone.add(map.get(temp.get(i)));
		}
		return sortedZone;

	}

	private Zone identifyZone() {
		ArrayList<Zone> sortedZone = sortedZone();
		Zone chosenZone = null;
		for (Zone zone : sortedZone) {
			if (isMergable(zone)) {
				chosenZone = zone;
				break;
			}
		}
		return chosenZone;
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException,
			ClassNotFoundException, InterruptedException {
		Client c = new Client();
		Scanner sc = new Scanner(System.in);
		int ch;
		while (true) {
			System.out.println("1. Node Join");
			System.out.println("2. Node leave");
			System.out.println("3. Display Neighbours");
			System.out.println("4. Display Current Zone of this node");
			System.out.println("5. Share a file");
			System.out.println("6. Get a file");
			System.out.println("7. Exit");
			ch = sc.nextInt();

			if (ch == 1) {
				Client.bootStrapIp = InetAddress.getByName(args[0]);
				c.nodeJoin(bootStrapIp);
			} else if (ch == 2) {
				c.nodeLeave();
			} else if (ch == 3)
				c.printNeighbour();
			else if (ch == 4)
				System.out.println(Client.z);
			else if (ch == 5) {
				System.out.println("Enter filename to share: ");
				String filename = sc.next();
				c.shareFile(filename);
			} else if (ch == 6) {
				System.out.println("Enter filename to search: ");
				String filename = sc.next();
				c.getFile(filename);
			} else if (ch == 7)
				System.exit(1);
			else
				System.out.println("Invalid Option");

		}
	}

	@SuppressWarnings("resource")
	public void run() {
		// Client accepting connections of other clients
		try {
			if (!isNotServer) {
				// System.out.println("Server Startted");
				ServerSocket ss = new ServerSocket(15000);
				isNotServer = true;
				while (true)
					new Client(ss.accept());
			}

			else if (isPeriodicThread) {
				isPeriodicThread = false;
				while (true) {
					// System.out.println("Periodic thread Strarted");
					Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
					Thread.sleep(5000);
					Set<Zone> temp = new HashSet<Zone>();
					temp.add(z);
					UPDATE_REQUEST ureq = new UPDATE_REQUEST(temp);
					for (Zone zone : neighbours) {
						Socket s = new Socket(zone.clientIp, 15000);
						ObjectOutputStream out = new ObjectOutputStream(
								s.getOutputStream());
						out.writeObject(ureq);
						out.close();
						s.close();
					}

				}

			}

			else {

				ObjectInputStream in = new ObjectInputStream(s.getInputStream());
				Object receivedObj = in.readObject();

				if (receivedObj instanceof JOIN_REQUEST) {
					JOIN_REQUEST jreq = (JOIN_REQUEST) receivedObj;
					requestRouting(jreq);
				}

				else if (receivedObj instanceof JOIN_REQUEST_FEEDBACK) {
					JOIN_REQUEST_FEEDBACK jrfeed = (JOIN_REQUEST_FEEDBACK) receivedObj;
					setZone(jrfeed.newZone);
					this.addNeighbours(jrfeed.listOfNeighbours);

					System.out.println("Zone obtained-->");
					System.out.println(z);
				}

				else if (receivedObj instanceof UPDATE_REQUEST) {
					UPDATE_REQUEST ureq = (UPDATE_REQUEST) receivedObj;
					this.updateNeighbours(ureq);
				}

				else if (receivedObj instanceof LEAVE_REQUEST) {
					LEAVE_REQUEST lreq = (LEAVE_REQUEST) receivedObj;
					this.removeLeavingNode(lreq);
				}

				else if (receivedObj instanceof MERGE_REQUEST) {
					MERGE_REQUEST mreq = (MERGE_REQUEST) receivedObj;
					mergeIncommingZone(mreq);
					sendMergedZoneToLeavingZoneNeighbours(mreq);
				}

				else if (receivedObj instanceof CONTROL_ZONE_REQUEST) {
					CONTROL_ZONE_REQUEST creq = (CONTROL_ZONE_REQUEST) receivedObj;
					addToControlledZoneSet(creq);
				}

				else if (receivedObj instanceof FILE_SHARE_REQUEST) {
					FILE_SHARE_REQUEST fsr = (FILE_SHARE_REQUEST) receivedObj;
					forwardFileRequest(fsr);
				}

				else if (receivedObj instanceof FILE_GET_REQUEST) {
					FILE_GET_REQUEST fgr = (FILE_GET_REQUEST) receivedObj;
					forwardGetFile(fgr);
				} else {
					System.out.println("else of instanceof in thread");
				}
				in.close();
				s.close();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	float HashFunctionX(String filename) {
		long asciiSum = 1;
		for (int i = 0; i < filename.length(); i++)
			asciiSum *= filename.charAt(i);
		return asciiSum % 100;

	}

	float HashFunctionY(String filename) {
		long asciiSum = 0;
		for (int i = 0; i < filename.length(); i++)
			asciiSum += filename.charAt(i);
		return asciiSum % 100;

	}

	private void getFile(String filename) throws IOException {
		Point p = new Point(this.HashFunctionX(filename),
				this.HashFunctionY(filename));
		System.out.println("Hash value" + p);
		if (p.liesInZone(z)) {
			System.out.println("File exist on this system. No need to route");
			return;
		}
		float min = Float.MAX_VALUE;
		Zone minZone = null;
		for (Zone zone : neighbours) {
			if (z.center.distance(zone.center) < min) {
				min = z.center.distance(zone.center);
				minZone = zone;
			}
		}

		FILE_GET_REQUEST fgr = new FILE_GET_REQUEST(filename, p, z.clientIp);
		fgr.visited.add(z);
		Socket s = new Socket(minZone.clientIp, 15000);
		ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
		out.writeObject(fgr);
		out.close();
		s.close();

		ServerSocket ss = new ServerSocket(25000);
		Socket s1 = ss.accept();
		DataInputStream in = new DataInputStream(s1.getInputStream());
		DataOutputStream outstream = new DataOutputStream(s1.getOutputStream());

		FileOutputStream fout = new FileOutputStream(filename);

		outstream.writeBoolean(true);
		int c;
		while (true) {
			String c1 = in.readUTF();
			c = Integer.parseInt(c1);
			if (c == -1)
				break;
			fout.write(c);
		}
		fout.close();
		outstream.close();
		in.close();
		s.close();
		ss.close();
	}

	private void shareFile(String filename) throws IOException {

		Point putxy = new Point(this.HashFunctionX(filename),
				this.HashFunctionY(filename));
		System.out.println("Hash value" + putxy);
		if (putxy.liesInZone(z)) {
			System.out.println(" File placed");
			return;
		}
		float min = Float.MAX_VALUE;
		Zone minZone = null;
		for (Zone zone : neighbours) {
			if (z.center.distance(zone.center) < min) {
				min = z.center.distance(zone.center);
				minZone = zone;
			}
		}

		FILE_SHARE_REQUEST fsr = new FILE_SHARE_REQUEST(new String(filename),
				putxy, z.clientIp);
		fsr.visited.add(z);
		Socket s = new Socket(minZone.clientIp, 15000);
		ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
		out.writeObject(fsr);
		out.close();
		s.close();

		ServerSocket ss = new ServerSocket(25000);
		Socket s1 = ss.accept();
		DataInputStream in = new DataInputStream(s1.getInputStream());
		DataOutputStream outfile = new DataOutputStream(s1.getOutputStream());
		FileInputStream fin = new FileInputStream(filename);

		if (in.readBoolean()) {

			Integer c1;
			int c;
			while ((c = fin.read()) != -1) {
				c1 = c;
				outfile.writeUTF(new String(c1.toString()));

			}
			c1 = -1;
			outfile.writeUTF(new String(c1.toString()));
		}
		fin.close();
		outfile.close();
		in.close();
		s1.close();
		ss.close();

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void forwardGetFile(FILE_GET_REQUEST fsr) throws IOException {
		if (fsr.p.liesInZone(z)) {
			Socket s = new Socket(fsr.sourceIp, 25000);
			DataInputStream in = new DataInputStream(s.getInputStream());
			DataOutputStream outstream = new DataOutputStream(
					s.getOutputStream());
			FileInputStream fin = new FileInputStream(fsr.filename);
			if (in.readBoolean()) {

				Integer c1;
				int c;
				while ((c = fin.read()) != -1) {
					c1 = c;
					outstream.writeUTF(new String(c1.toString()));

				}
				c1 = -1;
				outstream.writeUTF(new String(c1.toString()));
			}
			fin.close();
			outstream.close();
			in.close();
			s.close();

		} else {
			Set<Zone> temp = new HashSet(neighbours);
			float minDist = Float.MAX_VALUE;
			Zone nextNeighbour = null;
			boolean flag = true;
			for (Zone zone : temp) {
				for (Zone z1 : fsr.visited) {
					if (zone.clientIp.equals(z1.clientIp)) {
						flag = false;
						break;
					}
				}
				if (z.center.distance(zone.center) < minDist && flag) {

					minDist = z.center.distance(zone.center);
					nextNeighbour = zone;
				}
				flag = true;
			}

			if (nextNeighbour != null) {
				fsr.visited.add(z);
				// System.out.println("Inside");
				Socket s = new Socket(nextNeighbour.clientIp, 15000);
				ObjectOutputStream out = new ObjectOutputStream(
						s.getOutputStream());
				out.writeObject(fsr);
				out.close();
				s.close();
			} else {
				System.out.println("Backtracking");
				Zone prevVisitedZone = fsr.visited.getLast();
				fsr.visited.add(z);
				Socket s = new Socket(prevVisitedZone.clientIp, 15000);
				ObjectOutputStream out = new ObjectOutputStream(
						s.getOutputStream());
				out.writeObject(fsr);
				out.close();
				s.close();
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void forwardFileRequest(FILE_SHARE_REQUEST fsr) throws IOException {

		if (fsr.p.liesInZone(z)) {
			Socket s = new Socket(fsr.sourceIp, 25000);
			DataInputStream in = new DataInputStream(s.getInputStream());
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			FileOutputStream fout = new FileOutputStream(fsr.filename);
			out.writeBoolean(true);
			int c;
			while (true) {
				String c1 = in.readUTF();
				c = Integer.parseInt(c1);
				if (c == -1)
					break;
				fout.write(c);
			}
			fout.close();
			out.close();
			in.close();
			s.close();

		} else {
			Set<Zone> temp = new HashSet(neighbours);
			float minDist = Float.MAX_VALUE;
			Zone nextNeighbour = null;
			boolean flag = true;
			for (Zone zone : temp) {
				for (Zone z1 : fsr.visited) {
					if (zone.clientIp.equals(z1.clientIp)) {
						flag = false;
						break;
					}
				}
				if (z.center.distance(zone.center) < minDist && flag) {

					minDist = z.center.distance(zone.center);
					nextNeighbour = zone;
				}
				flag = true;
			}

			if (nextNeighbour != null) {
				fsr.visited.add(z);
				// System.out.println("Inside");
				Socket s = new Socket(nextNeighbour.clientIp, 15000);
				ObjectOutputStream out = new ObjectOutputStream(
						s.getOutputStream());
				out.writeObject(fsr);
				out.close();
				s.close();
			} else {
				System.out.println("Backtracking");
				Zone prevVisitedZone = fsr.visited.getLast();
				fsr.visited.add(z);
				Socket s = new Socket(prevVisitedZone.clientIp, 15000);
				ObjectOutputStream out = new ObjectOutputStream(
						s.getOutputStream());
				out.writeObject(fsr);
				out.close();
				s.close();
			}
		}

	}

}
