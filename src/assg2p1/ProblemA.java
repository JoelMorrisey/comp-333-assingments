package assg2p1;

import java.io.*;
import java.util.*;

public class ProblemA {
	
	ArrayList<String> stationList = new ArrayList<>();
	HashMap<String, Station> stationMap = new HashMap<>();

	public ProblemA(String infile) {
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
		
		while(in.hasNext()) {
			String a = in.next();
			String b = in.next();
			if (!stationMap.containsKey(a)) {
				stationMap.put(a, new Station(a));
				stationList.add(a);
			}
			if (!stationMap.containsKey(b)) {
				stationMap.put(b, new Station(b));
				stationList.add(b);
			}
			stationMap.get(a).addNeigh(stationMap.get(b));
		}
		
		in.close();
	}
	
	/**
	 * Returns the number of routes between two stations for all pairs 
	 * of stations, as described in the assignment spec. 
	 * 
	 * @return the 2D hashmap containing the number of routes
	 */
	public HashMap<String,HashMap<String,Integer>> findNumberOfRoutes(){
		HashMap<String,HashMap<String,Integer>> result = new HashMap<>();
		
		
		for (String src: stationList) {//station to use as source
			result.put(src, new HashMap<>()); //Initialise the hash map
			for (String dest: stationList) { //station to use as destination
				//if the is a direct connect to the node we know there is at least on path from source to that node
				if (stationMap.get(src).getNeigh().contains(stationMap.get(dest))) {
					result.get(src).put(dest, 1);
				} else {
					result.get(src).put(dest, 0);
				}
			}
		}
		
		//run an altered version of floyd war-shall algorithm
		for (String inter: stationList) { //station to use as intermediate
			for (String start: stationList) { //station to use as source
				for (String end: stationList) { //station to use as dest
					/*
					 * The number of paths is:
					 * number of paths to end = (current number of paths to end) + (number of path to inter * number of paths to end from inter)
					*/
					result.get(start).put(end, result.get(start).get(end)+result.get(start).get(inter)*result.get(inter).get(end));
				}
			}
		}
		
		//find and mark infiniant loops
		for (String loopStation: stationList) { //go through all stations
			if (result.get(loopStation).get(loopStation) > 0) { //only continue if can get to self
				for (String src: stationList) { //loop through all possible sources
					for (String dest: stationList) { //loop through all possible destinations
						//make sure src can get to loop station and that loop station can get to destination station
						if (result.get(src).get(loopStation) != 0 && result.get(loopStation).get(dest) != 0) {
							result.get(src).put(dest, -1); //if all of the above is true then we can get from src to dest infinitely
						}
					}
				}
			}
		}
		
		return result;
	}
}

class Station {
	private String name = "";
	private HashSet<Station> neigh = new HashSet<>();
	
	public Station(String name) {
		this.name = name;
	}
	
	public void setName(String newName) {
		this.name = newName;
	}
	
	public void addNeigh(Station s) {
		neigh.add(s);
	}
	
	public ArrayList<Station> getNeigh() {
		ArrayList<Station> ret = new ArrayList<>();
		for (Station x: neigh) {
			ret.add(x);
		}
		return ret;
	}
	
	public String toString() {
		return name;
	}
}
