package org.nirmalya.hpn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class Graph {
	
	int INVALID_LEVEL = -1;
	
	Multimap<String, String> map = HashMultimap.create();
	Multimap<String, String> TCGraph = HashMultimap.create();
	List<String> levelKeys = null;
	
	public Graph(String oriGraphFile) {

		readOriGraphFile(oriGraphFile);
		//populateTCGraph();
		
	}
	
	public Graph(Multimap<String, String> map) {
		this.map = map;
		//populateTCGraph();
	}

	public void setLevelKeys(List<String> levelKeys) {
		this.levelKeys = levelKeys;
		
	}

	void readOriGraphFile(String oriGraphFile) {
		try {
			BufferedReader oriGraphReader 
					= new BufferedReader(new FileReader(oriGraphFile));
			
			// This change has been made due to change in the file format of the network.
			String regex = "^(\\S+)\\s+(\\S+)";
			//String regex = "^\\S+\\s+(\\S+)\\s+\\S+\\s+\\S+\\s+(\\S+)";
			
			Pattern pat = Pattern.compile(regex);
			
			String line = null;
			
			while (null != (line = oriGraphReader.readLine())) {
				
				Matcher mat = pat.matcher(line);
				
				if (mat.find()) {
					String first = mat.group(1);
					String second = mat.group(2);
					map.put(first, second);
				}				
			}
			
			oriGraphReader.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}	
	
	public void populateTCGraph() {
		
		HashSet<String> localSet = Sets.newHashSet();
		
		for (String element : map.keys()) {
			if (!localSet.contains(element))
				localSet.add(element);
		}
		
		for (String vertex : localSet) {
			Multimap<String, String> localConnectivity = BFS(vertex, map);
			TCGraph.putAll(localConnectivity);
		}	
		
	}	
	
	Multimap<String, String> BFS(String source, Multimap<String, String> localMap) {
		
		// Keeps track of all the nodes that has been visited.
		Set<String> visitedNodes = Sets.newHashSet();
		Multimap<String, String> connectivity = HashMultimap.create();
		
		LinkedList<String> queue = Lists.newLinkedList();
		//push the source gene on the stack
		queue.add(source);

		while (!queue.isEmpty()) {
			
			String newNode = queue.remove();	
			
			visitedNodes.add(newNode);	
			connectivity.put(source, newNode);
			
			Collection<String> neighSet = localMap.get(newNode);
			for (String neigh : neighSet) {
				if (!visitedNodes.contains(neigh)) {
					queue.add(neigh);
				
				}
			}
			
		}
		
		/*
		if (connectivity.containsEntry(source, source)) {
			connectivity.remove(source, source);
		}
		*/
		
		return connectivity;
				
	}
	
	public int calculateAdjacencyPenalty(List<Integer> levelVals) {
		return calculatePenalty(levelVals, map);
	}
	
	public int calcualteReachabilityPenalty(List<Integer> levelVals) {
		return calculatePenalty(levelVals, TCGraph);
	}
	
	private int calculatePenalty(List<Integer> levelVals, 
								Multimap<String, String> localMap) {
		int penalty = 0;
		int localCount = 0;
		for (Entry<String, String> edge : localMap.entries()) {
			String left = edge.getKey();
			String right = edge.getValue();			
			
			
			if (left.equals(right)) {
				continue;
			}
			
					
			
			if (!levelKeys.contains(left))
				continue;
			int leftPos = levelKeys.indexOf(left);
			
			if(!levelKeys.contains(right))
				continue;
			int rightPos = levelKeys.indexOf(right);
			
			int leftLevel = levelVals.get(leftPos);
			int rightLevel = levelVals.get(rightPos);
			

			if (leftLevel == INVALID_LEVEL || rightLevel == INVALID_LEVEL) {
				continue;
			}
			
			localCount++;
			
			
			if (rightLevel >= leftLevel) {
				penalty++;
			}
		}
		/*
		System.out.println(
				"Total Count: " +
				localMap.size() +
				" Useful Entry: " +
				localCount +
				" Penalty: " + 
				penalty);
		*/
		return penalty;
	}

	public void cleanData(List<String> levelKeys2) {
		// First clean all the entries that are non transcription factors, i.e.
		// does not come on the left side of the graph.
		
		Multimap<String, String> localMap = HashMultimap.create();
		
		for (Entry<String, String> entry : map.entries()) {
			String left = entry.getKey();
			String right = entry.getValue();
			
			if (!map.keySet().contains(right)) {
				continue;
			}
			

			if (!levelKeys2.contains(left) && !levelKeys2.contains(right)) {
				//System.out.println("I am here.");
				continue;
			}

			localMap.put(left, right);
		}
		
		map = localMap;		
		
	}

}
