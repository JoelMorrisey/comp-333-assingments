package assg1p1;

import java.io.*;
import java.util.*;

class Pair<T,K> {
	T weight;
	K prev;
	public Pair(T weight, K prev) {
		setWieght(weight);
		setPrev(prev);
	}
	public void setWieght(T weight) {
		this.weight = weight;
	}
	public void setPrev(K prev) {
		this.prev = prev;
	}
	public T getWeight() {
		return weight;
	}
	public K getPrevious() {
		return prev;
	}
	public String toString() {
		return weight + ":" + prev;
	}
}

class stationDijkstraComparator implements Comparator<Station> {
	HashMap<Station, Pair<Integer,Station>> map;
	public stationDijkstraComparator(HashMap<Station, Pair<Integer,Station>> map) {
		//sets the hashmap
		this.map = map;
	}
	@Override
	public int compare(Station s1, Station s2) {
		if (map.get(s1).getWeight() > map.get(s2).getWeight()) {
			return 1;
		}
		if (map.get(s1).getWeight() < map.get(s2).getWeight()) {
			return -1;
		}
		return 0;
	} 
} 

public class RailNetwork {
	
	//private final double THRESHOLD = 0.000001;
	
	private TreeMap<String,Station> stationList;
	
	public RailNetwork(String trainData, String connectionData) {
		stationList = new TreeMap<>();
		
		try {	
			readStationData(trainData);
			readConnectionData(connectionData);
		}
		catch (IOException e) {
			System.out.println("Exception encountered: " + e);
		}
	}
	
	/**
	 * Reads the CSV file containing information about the stations and 
	 * populate the TreeMap<String,Station> stationList. Each row of 
	 * the CSV file contains the name, latitude and longitude coordinates
	 * of the station.
	 * 
	 * You need to make a Station object for each row and add it to the 
	 * TreeMap<String,Station> stationList where the key value is the 
	 * name of the station (as a String).
	 * 
	 * @param infile	   the path to the file
	 * @throws IOException if the file is not found
	 */
	public void readStationData(String infile) throws IOException{
		Scanner in = new Scanner(new FileReader(infile));
		in.nextLine();
		in.useDelimiter(",");
		while(in.hasNext()) {
			String station = in.next();
			stationList.put(station, new Station(station, in.nextDouble(), in.nextDouble()));
			in.nextLine();
		}
		in.close();
	}
	/**
	 * Reads the CSV file containing information about connectivity between 
	 * adjacent stations, and update the stations in stationList so that each
	 * Station object has a list of adjacent stations.
	 * 
	 * Each row contains two Strings separated by comma. To obtain the distance
	 * between the two stations, you need to use the latitude and longitude 
	 * coordinates together with the computeDistance() methods defined below
	 * 
	 * @param infile	   the path to the file
	 * @throws IOException if the file is not found
	 */	
	public void readConnectionData(String infile) throws IOException{
		Scanner in = new Scanner(new FileReader(infile));
		in.useDelimiter(",");
		while (in.hasNext()) {
			String a = in.next();
			String b = in.next();
			Station st1 = stationList.get(a);
			Station st2 = stationList.get(b);
			st1.addNeighbour(st2, this.computeDistance(st1, st2));
			st2.addNeighbour(st1, this.computeDistance(st2, st1));
			in.nextLine();
		}
		in.close();
	}
	
	/**
	 * Given the latitude and longitude coordinates of two locations x and y, 
	 * return the distance between x and y in metres using Haversine formula,
	 * rounded down to the nearest integer.
	 * 
	 * Note that two more methods are provided below for your convenience 
	 * and you should not directly call this method
	 * 
	 * source://www.geeksforgeeks.org/haversine-formula-to-find-distance-between-two-points-on-a-sphere/
	 * 
	 * @param lat1 latitude coordinate of x
	 * @param lon1 longitude coordinate of x
	 * @param lat2 latitude coordinate of y
	 * @param lon2 longitude coordinate of y
	 * @return distance betwee
	 */
	public static int computeDistance(double lat1, double lon1, double lat2, double lon2) {
        // distance between latitudes and longitudes 
        double dLat = Math.toRadians(lat2 - lat1); 
        double dLon = Math.toRadians(lon2 - lon1); 
  
        // convert to radians 
        lat1 = Math.toRadians(lat1); 
        lat2 = Math.toRadians(lat2); 
  
        // apply formulae 
        double a = Math.pow(Math.sin(dLat / 2), 2) +  
                   Math.pow(Math.sin(dLon / 2), 2) *  
                   Math.cos(lat1) *  
                   Math.cos(lat2); 
        double rad = 6371.0; 
        Double c = 2 * Math.asin(Math.sqrt(a)); 
        Double distance = rad * c * 1000;
        return distance.intValue(); 
	}	
	
	/**
	 * Compute the distance between two stations in metres, where the stations
	 * are given as String objects
	 * 
	 * @param a		the first station
	 * @param b		the second station
	 * @return		the distance between the two stations in metres
	 */
	public int computeDistance(String a, String b) {
		Station u = stationList.get(a);
		Station v = stationList.get(b);
		return computeDistance(u.getLatitude(),u.getLongitude(),
							   v.getLatitude(),v.getLongitude());
	}
	
	/**
	 * Compute the distance between two stations in metres, where the stations
	 * are given as Station objects
	 * 
	 * @param a		the first station
	 * @param b		the second station
	 * @return		the distance between the two stations in metres
	 */
	public int computeDistance(Station a, Station b) {
		return computeDistance(a.getLatitude(),a.getLongitude(),
							   b.getLatitude(),b.getLongitude());
	}
	
	/**
	 * The method finds the shortest route (in terms of distance travelled) 
	 * between the origin station and the destination station.
	 * The route is returned as an ArrayList<String> containing the names of 
	 * the stations along the route, including the origin and the destination 
	 * stations.
	 * 
	 * If the route cannot be completed (there is no path between origin and
	 * destination), then return an empty ArrayList<String>
	 * 
	 * If the origin or the destination stations are not in the list of stations,
	 * return an empty ArrayList<String>. 
	 * 
	 * If the origin and the destination stations are the same, return an 
	 * ArrayList<String> containing the station.
	 * 
	 * @param origin		the starting station
	 * @param destination	the destination station
	 * @return
	 */
	public ArrayList<String> routeMinDistance(String origin, String destination){
		if (!stationList.containsKey(origin) || !stationList.containsKey(destination)) {
			return new ArrayList<String>();
		}
		if (origin.equals(destination)) {
			ArrayList<String> ans = new ArrayList<String>();
			ans.add(origin);
			return ans;
		}
		
		Station head = stationList.get(origin);
		HashMap<Station, Pair<Integer,Station>> m = new HashMap<>();// format {Station, Cost to Station, Previous station}
		PriorityQueue<Station> q = new PriorityQueue<Station>(new stationDijkstraComparator(m));
		
		//placing source into our queue and map
		m.put(head,  new Pair<>(0,head));
		q.add(head);
		
		while(!q.isEmpty()) {
			Station a = q.poll();
			a.setMarked();
			for (Station n: a.getAdjacentStations().keySet()) {
				if (!n.isMarked()) {
					//calculate the distance to the node from source if we take the current node a
					Integer k = m.get(a).getWeight();
					Integer d = a.getAdjacentStations().get(n);
					Integer val = d + k;
					Station prev = a;
					
					if (!m.containsKey(n)) {
						m.put(n, new Pair<>(val, prev));
					} else {
						if (val < m.get(n).getWeight()) {
							m.remove(n);
							m.put(n, new Pair<>(val, prev));
						}
					}
					//Makes sure some how an item does not end up in the list
					if (!q.contains(n)) {
						q.add(n);
					}
				}
			}
		}
		
		//calculate path based on map
		ArrayList<String> path = new ArrayList<>();
		Station pos = stationList.get(destination);
		Pair<Integer, Station> pairPos;
		
		while (pos != stationList.get(origin)) {
			path.add(0,pos.getName());
			pairPos=m.get(pos);
			if (pairPos == null) {
				return new ArrayList<String>();
			}
			pos = pairPos.getPrevious();
		}
		path.add(0,pos.getName()); //add the source station to the list
		
		//reset the markings on stations
		for (String s: stationList.keySet()) {
			stationList.get(s).setUnmarked();
		}
		
		return path;
	}


	/**
	 * The method finds the shortest route (in terms of distance travelled) 
	 * between the origin station and the destination station under the 
	 * condition that the route must not pass through any stations in 
	 * TreeSet<String> failures
	 * 
	 * The route is returned as an ArrayList<String> containing the names of 
	 * the stations along the route, including the origin and the destination 
	 * stations.
	 * 
	 * If the route cannot be completed (there is no path between origin and
	 * destination), then return an empty ArrayList<String>
	 * 
	 * If the origin or the destination stations are not in the list of stations,
	 * return an empty ArrayList<String>. 
	 * 
	 * If the origin and the destination stations are the same, return an 
	 * ArrayList<String> containing the station.
	 * 
	 * @param origin		the starting station
	 * @param destination	the destination station
	 * @param failures		the list of stations that cannot be used
	 * @return
	 */
	public ArrayList<String> routeMinDistance(String origin, String destination, TreeSet<String> failures){
		if (!stationList.containsKey(origin) ||  !stationList.containsKey(destination) ||
			failures.contains(origin) || failures.contains(destination)) {
			return new ArrayList<String>();
		}
		if (origin.equals(destination)) {
			ArrayList<String> ans = new ArrayList<String>();
			ans.add(origin);
			return ans;
		}
		
		//set all stations that have failures to visited
		Station a;
		for (String s: failures) {
			a = stationList.get(s);
			if (a != null) {
				a.setMarked();
			}
		}
		
		return routeMinDistance(origin, destination);
	}
	/**
	 * The method finds the shortest route (in terms of number of stops)
	 * between the origin station and the destination station.
	 * The route is returned as an ArrayList<String> containing the names of 
	 * the stations along the route, including the origin and the destination 
	 * stations.
	 * 
	 * If the route cannot be completed (there is no path between origin and
	 * destination), then return an empty ArrayList<String>
	 * 
	 * If the origin or the destination stations are not in the list of stations,
	 * return an empty ArrayList<String>. 
	 * 
	 * If the origin and the destination stations are the same, return an 
	 * ArrayList<String> containing the station.
	 * 
	 * @param origin		the starting station
	 * @param destination	the destination station
	 * @return
	 */
	public ArrayList<String> routeMinStop(String origin, String destination){
		if (!stationList.containsKey(origin) || !stationList.containsKey(destination)) {
			return new ArrayList<String>();
		}
		if (origin.equals(destination)) {
			ArrayList<String> ans = new ArrayList<String>();
			ans.add(origin);
			return ans;
		}
		//Get the source and destination station to make things easier
		Station head = stationList.get(origin);
		Station dest = stationList.get(destination);
		
		Queue<Station> q = new LinkedList<>();
		HashMap<Station, Station> m = new HashMap<>();
		
		//add the source to the map and queue
		m.put(head, head);
		q.add(head);
		head.setMarked();
		
		while (!q.isEmpty()) {
			Station a = q.poll();
			for (Station n: a.getAdjacentStations().keySet()) {
				if (!n.isMarked()) {
					q.add(n);
					m.put(n, a);
					n.setMarked();
					if (n == dest) {
						//empties queue so that the outter loop will stop
						q = new LinkedList<>();
						break;
					}
				}
			}
		}
		
		//calculate path based on map
		Station curr = dest;
		ArrayList<String> path = new ArrayList<>();
		while (curr != head && curr != null) {
			path.add(0, curr.getName());
			curr = m.get(curr);
		}
		//if null no path exists
		if (curr == null) {
			return new ArrayList<String>();
		}
		//add source node to start
		path.add(0, head.getName());
		
		//resets the marked flags
		for (String s: stationList.keySet()) {
			stationList.get(s).setUnmarked();
		}
		
		return path;
	}

	/**
	 * The method finds the shortest route (in terms of number of stops)
	 * between the origin station and the destination station under the 
	 * condition that the route must not pass through any stations in 
	 * TreeSet<String> failures (i.e. the rail segment cannot be travelled on)
	 * 
	 * The route is returned as an ArrayList<String> containing the names of 
	 * the stations along the route, including the origin and the destination 
	 * stations.
	 * 
	 * If the route cannot be completed (there is no path between origin and
	 * destination), then return an empty ArrayList<String>
	 * 
	 * If the origin or the destination stations are not in the list of stations,
	 * return an empty ArrayList<String>. 
	 * 
	 * If the origin and the destination stations are the same, return an 
	 * ArrayList<String> containing the station.
	 * 
	 * @param origin		the starting station
	 * @param destination	the destination station
	 * @param failures		the list of stations that cannot be used
	 * @return
	 */
	public ArrayList<String> routeMinStop(String origin, String destination, TreeSet<String> failures){
		if (!stationList.containsKey(origin) || !stationList.containsKey(destination)) {
			return new ArrayList<String>();
		}
		if (origin.equals(destination)) {
			ArrayList<String> ans = new ArrayList<String>();
			ans.add(origin);
			return ans;
		}
		
		//set all stations that have failures to visited
		Station a;
		for (String s: failures) {
			if ((a = stationList.get(s)) != null) {
				a.setMarked();
			}
		}
		return routeMinStop(origin, destination);	
	}
	
	/**
	 * Given a route between two stations, compute the total distance 
	 * of this route.
	 * 
	 * @param path	the list of stations in the route (as String objects)
	 * @return		the length of the route between the first station
	 * 				and last station in the list	
	 */
	public int findTotalDistance(ArrayList<String> path) {
		//simple error checks
		if (path == null) {
			return -1;
		}
		if (path.size() <= 1) {
			return 0;
		}
		//Actually calculate the distance
		int distance = 0;
		for (int i = 1; i<path.size(); i++) {
			//If the path does not exist between two stations then return 0
			Integer newDist = stationList.get(path.get(i-1)).getAdjacentStations().get(stationList.get(path.get(i)));
			if (newDist == null) {
				return -1;
			}
			distance+= newDist;
		}
		return distance;
	}
	
	
	/** 
	 * Given a route between two stations, compute the minimum total cost 
	 * of performing an exhaustive scan on this route, as described in the 
	 * assignment specification for Stage 2.
	 * 
	 * Return 0 if there are 2 or less stations in the route. 
	 * 
	 * @param route  the list of stations in the route (as String objects)
	 * @return		 the minimum cost of performing exhaustive scans
	 */
	int[][] lookupTable;
	public int optimalScanCost(ArrayList<String> route) {
		if (route == null || route.size() <= 2) {
			return 0;
		}
		//create both tables
		lookupTable = new int[route.size()+1][route.size()+1];
		solTable = new int[route.size()+1][route.size()+1];
		for (int i = 0; i<lookupTable.length; i++) {
			for (int k = 0; k<lookupTable[i].length; k++) {
				lookupTable[i][k] = -1;
				solTable[i][k] = -1;
			}
		}
		//solve the problem
		return optimalScanCostSolver(route, 0, route.size());
	}
	private int optimalScanCostSolver(ArrayList<String> route, int start, int end) {
		if ((end-1)-start <= 1) {
			return 0;
		}
		//if the table has the solution already return it
		if (lookupTable[start][end]!=-1) {
			return lookupTable[start][end];
		}
		//get the total length from the start station to the end station
		int total = 0;
		for (int i = start+1; i<end; i++) {
			total += stationList.get(route.get(i-1)).getAdjacentStations().get(stationList.get(route.get(i)));
		}
		//if the sub array has only 3 nodes return the total
		if ((end-1)-start <= 2) { //less then is just for safety at the point (end-1)-start should be >= 2
			return total;
		}
		
		//try picking all stations to partition by between start and end exclusive
		int subTotal = Integer.MAX_VALUE;
		for (int i = start+1; i<end-1; i++) {
			if (subTotal > optimalScanCostSolver(route,start,i+1) + optimalScanCostSolver(route, i, end)) {
				subTotal = optimalScanCostSolver(route,start,i+1) + optimalScanCostSolver(route, i, end);
				solTable[start][end] = i;//update the optimal station choice
			}
		}
		lookupTable[start][end] = subTotal+total; //set the best cost possible
		return lookupTable[start][end];
	}
	
	/***
	 * Given a route between two stations, return the list of stations (in
	 * the order that they were chosen) that gives the segmentation that 
	 * leads to the minimum cost for performing an exhaustive scan on the 
	 * the route (as described in the assignment specification for Stage 2.
	 * 
	 * Return an empty ArrayList if there are 2 or less stations in the route.
	 * 
	 * @param route
	 * @return
	 */
	int[][] solTable;
	HashSet<String> res;
	public ArrayList<String> optimalScanSolution(ArrayList<String> route){
		if (route == null || route.size() <= 2) {
			return new ArrayList<String>();
		}
		
		optimalScanCost(route);
		
		//search solTable which was modified by optimalScanCost to find optimal stations
		res = new HashSet<String>();//makes sure there is not duplicate stations
		search(route, 0, route.size());
		//return the set as a arrayList
		return new ArrayList<>(res);
	}
	private void search(ArrayList<String> route, int start, int end) {
		//This if statement is more for safety then anything else
		if (start > solTable.length || end > solTable[start].length) {
			return;
		}
		//Check if we have any more splits in the sub array
		if (solTable[start][end] == -1) {
			return;
		}
		res.add(route.get(solTable[start][end]));
		
		//recurse with the two halves of the array
		search(route, solTable[start][end], end);
		search(route, start, solTable[start][end]+1);
	}
}
