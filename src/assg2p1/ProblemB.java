package assg2p1;

import java.io.*;
import java.util.*;

public class ProblemB {

	CapStation start;
	CapStation end;
	ArrayList<String> stationList = new ArrayList<>();
	HashMap<String, CapStation> stationMap = new HashMap<>();

	public ProblemB(String infile) {
		try {	
			processInput(infile);
		}
		catch (IOException e) {
			System.out.println("Exception encountered: " + e);
		}
	}

	/**
	 * A helper method to process the input file. 
	 * 
	 * @param infile the file containing the input
	 * @throws IOException
	 */
	public void processInput(String infile) throws IOException{
		Scanner in = new Scanner(new FileReader(infile));
		CapStation start = new CapStation(in.next());
		CapStation end = new CapStation(in.next());

		this.start = start;
		this.end = end;

		stationList.add(start.toString());
		stationList.add(end.toString());
		stationMap.put(start.toString(), start);
		stationMap.put(end.toString(), end);

		int numOfStations = in.nextInt();
		int numOfEdges = in.nextInt();
		in.nextLine();

		//Set up basic map
		HashMap<CapStation, Integer> stationCompasities = new HashMap<>();
		ArrayList<String> temp = new ArrayList<>(); //keeps track of the stations that need to be spliced
		for (int i = 0; i<numOfStations; i++) {
			String name = in.next();
			int cost = in.nextInt();
			CapStation station = new CapStation(name);
			temp.add(station.toString());
			stationList.add(station.toString());
			stationMap.put(station.toString(), station);
			stationCompasities.put(station, cost); //store the station and it's capacity
		}

		for (int i = 0; i<numOfEdges; i++) {
			String from = in.next();
			String to = in.next();
			int cost = in.nextInt();
			stationMap.get(from).addNeigh(stationMap.get(to), cost);

			//Makes a directed version of the undirected graph through adding an intermediate node to go backwards through
			CapStation backNode = new CapStation(from+to+"t");
			stationList.add(backNode.toString());
			stationMap.put(backNode.toString(), backNode);
			stationMap.get(to).addNeigh(backNode, cost);
			backNode.addNeigh(stationMap.get(from), cost);
		}


		//split every vertex that has a capacity into two stations connected by that capacity
		for (String x: temp) {
			int cost = stationCompasities.get(stationMap.get(x));
			ArrayList<Edge> edges = stationMap.get(x).getNeigh();
			stationMap.get(x).clearNeigh();
			CapStation endCapStation = new CapStation(x+"e");

			stationList.add(endCapStation.toString());
			stationMap.put(endCapStation.toString(), endCapStation);

			for (Edge e: edges) {
				endCapStation.addNeigh(e);
			}

			stationMap.get(x).addNeigh(endCapStation, cost);
		}

		in.close();
	}

	public ArrayList<Edge> findPath() {
		ArrayList<Edge> path = new ArrayList<>();
		Queue<CapStation> known = new LinkedList<CapStation>();

		HashMap<CapStation, CapStation> prevStation = new HashMap<>();
		HashMap<CapStation, Edge> prevEdge = new HashMap<>();

		known.add(start);
		start.mark();
		while(!known.isEmpty()) {
			CapStation temp = known.poll();
			for (Edge x: temp.getNeigh()) {
				if (x.canTraverse() && !x.getEndStation().getMarker()) {
					x.getEndStation().mark();
					known.add(x.getEndStation());
					prevStation.put(x.getEndStation(), temp);
					prevEdge.put(x.getEndStation(), x);
					if (x == end) {
						continue;
					}
				}
			}
		}
		CapStation temp = end;
		while (temp != start && prevStation.containsKey(end)) {
			path.add(0, prevEdge.get(temp));
			temp = prevStation.get(temp);
		}
		for (String x: stationMap.keySet()) {
			stationMap.get(x).unmark();
		}
		return path;
	}

	/**
	 * Returns the minimum number of device failures that will cause 
	 * the two stations in the input file to be disconnected 
	 * (please refer to the assignment spec for the details)
	 * 
	 * @return an integer denoting the minimum number of device failures
	 */
	public Integer computeMinDevice() {
		int cost = 0;
		ArrayList<Edge> path = findPath();
		while (!path.isEmpty()) {
			int min = path.get(0).getCost();
			for (Edge x: path) {
				min = Math.min(min, x.getCost());
			}
			for (Edge x: path) {
				x.distribute(min);
			}
			cost += min;
			path = findPath();
		}
		return cost;
	}
}

class CapStation {

	private String name = "";
	private boolean marker = false;
	private ArrayList<Edge> neigh = new ArrayList<>();

	public CapStation(String name) {
		this.name = name;
	}

	public boolean getMarker() {
		return marker;
	}

	public void mark() {
		marker = true;
	}

	public void unmark() {
		marker = false;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public void addNeigh(Edge e) {
		neigh.add(e);
	}

	public void addNeigh(CapStation s, int edgeFlow) {
		ForwardEdge forward = new ForwardEdge(s, edgeFlow);
		BackwardEdge backward = new BackwardEdge(s, 0);
		forward.attachCapacity(backward);
		backward.attachFlow(forward);
		neigh.add(forward);
		neigh.add(backward);
	}

	public ArrayList<Edge> getNeigh() {
		ArrayList<Edge> ret = new ArrayList<>();
		for (Edge x: neigh) {
			ret.add(x);
		}
		return ret;
	}

	public String toString() {
		return name;
	}

	public void clearNeigh() {
		neigh = new ArrayList<>();
	}
}


class ForwardEdge implements Edge{
	private int cost;
	private CapStation otherEnd;
	private BackwardEdge backwards;

	public ForwardEdge(CapStation otherEnd, int cost) {
		this.otherEnd = otherEnd;
		this.cost = cost;
	}

	public void attachCapacity(BackwardEdge edge) {
		this.backwards = edge;
	}

	public int getCost() {
		return cost-backwards.getCost();
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public void distribute(int dis) {
		backwards.setCost(backwards.getCost()+dis);
	}

	public boolean canTraverse() {
		return cost > backwards.getCost();
	}

	public CapStation getEndStation() {
		return otherEnd;
	}

	public String toString() {
		return "(fwd: " + otherEnd.toString() + ")" + ":" + getCost();
	}
}

class BackwardEdge implements Edge{
	private int cost;
	private CapStation otherEnd;
	private ForwardEdge forwards;

	public BackwardEdge(CapStation otherEnd, int cost) {
		this.otherEnd = otherEnd;
		this.cost = cost;
	}

	public void attachFlow(ForwardEdge edge) {
		this.forwards = edge;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public void distribute(int dis) {
		cost -= dis;
	}

	public boolean canTraverse() {
		return (cost>0) && (forwards.getCost()>0);
	}

	public CapStation getEndStation() {
		return otherEnd;
	}

	public String toString() {
		return "(bwd: " +  otherEnd.toString() + ")" + ":" + getCost();
	}
}

interface Edge {
	public int getCost();
	public void setCost(int cost);
	public void distribute(int dis);
	public boolean canTraverse();
	public CapStation getEndStation();
}