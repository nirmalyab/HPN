package org.nirmalya.hpn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;


public class RandomizeGraph {
	
	HashMap<Integer, Edge> oriGraph = Maps.newHashMap();
	Multimap<String, String> oriMap = HashMultimap.create();

	private int edgeSize;
	
	public RandomizeGraph(String oriGraphFile) {
		try {
			BufferedReader oriGraphReader = new BufferedReader(new FileReader(oriGraphFile));
			
			String regex = "^(\\S+)\\s+(\\S+)";
			Pattern pat = Pattern.compile(regex);
			
			String line = null;
			int edgeCount = 0;
			
			while (null != (line = oriGraphReader.readLine())) {
				
				Matcher mat = pat.matcher(line);
				
				if (mat.find()) {
					String first = mat.group(1);
					String second = mat.group(2);
					oriGraph.put(edgeCount, new Edge(first, second));
					oriMap.put(first, second);
					edgeCount++;
				}				
			}
			
			setEdgeSize(edgeCount);
			oriGraphReader.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	int SEED = 10102;
	Random rand = new Random(SEED);
	
	/**
	 * The edge shuffling degree preserving algorithm can be found at 
	 * https://sites.google.com/site/randomnetworkplugin/Home/randomization-of-existing-networks
	 * @param shuffleCountVec
	 * @return
	 */
	public List<Multimap<String, String>>  
	
	getEdgeShuffleRandomGraphs(List<Integer> shuffleCountVec) {
		
		List<Multimap<String, String>>  mapList = Lists.newArrayList();
		
		for (int shuffleCount : shuffleCountVec) {
			
			/* Have to test whether this line is working for our purpose */
			HashMap<Integer, Edge> localGraph = Maps.newHashMap(oriGraph);
			Multimap<String, String> localMap = HashMultimap.create(oriMap);
			
			for (int j = 0; j < shuffleCount; j++) {
				
				boolean cond = true;
				while(cond) {
					int randFirst = rand.nextInt(getEdgeSize());
					int randSec = rand.nextInt(getEdgeSize());
					Edge edgeFirst = localGraph.get(randFirst);
					Edge edgeSec = localGraph.get(randSec);
					
					String u = edgeFirst.first;
					String v = edgeFirst.second;
					
					String s = edgeSec.first;
					String t = edgeSec.second;
					
					/* Check if the first condition is violated */
					if (u.equals(v) || s.equals(t) || 
							u.equals(s) || u.equals(t) || v.equals(s) || v.equals(t)) {
						continue;
					}
					
					/* Check if the second condition is violated */
					if (localMap.containsEntry(u, t) || localMap.containsEntry(s, v)) {
						continue;
					}
					
					/* Replace the old two edges with the new two edges. */
					
					// From localGraph
					Edge edgeFirstNew = new Edge(u, t);
					Edge edgeSecNew = new Edge(s, v);
					
					//First remove
					localGraph.remove(randFirst);
					localGraph.remove(randSec);
					
					//Then add
					localGraph.put(randFirst, edgeFirstNew);
					localGraph.put(randSec, edgeSecNew);	
					
					// From localMap					
					
					// First remove the old ones.
					localMap.remove(u, v);
					localMap.remove(s, t);	
					
					//Then add the new ones.
					localMap.put(u, t);
					localMap.put(s, v);

					
					// As the edge mutation is done, break from the loop.
					break;
					
				}
				
			}
			
			mapList.add(localMap);
		}
		
		return mapList;
		
	}
	
	public static void main(String[] args) {
		RandomizeGraph rGraph = new RandomizeGraph(args[0]);
		int localEdgeCount = rGraph.getEdgeSize();
		List<Integer> shuffleCountVec = Lists.newArrayList();
		
		int division = 20;
		for (int i = 0; i < division; i++) {
			int localShuffleCount = (int)Math.floor(localEdgeCount / division) * (i + 1);
			shuffleCountVec.add(localShuffleCount);
		}
		
		List<Multimap<String, String>>  mapList = rGraph.getEdgeShuffleRandomGraphs(shuffleCountVec);
		
		String outDir = args[1];
		for (int i = 0; i < mapList.size(); i++) {
			String fileName = "outFile" + i;
			String filePath = outDir + "/" + fileName;
			try {
				PrintWriter outFile = new PrintWriter(new FileWriter(new File(filePath)));
				
				Multimap<String, String> localMap = mapList.get(i);
				for (Entry<String, String> e : localMap.entries()) {
					String first = e.getKey();
					String sec = e.getValue();
					
					outFile.println(first + " " + sec);
				}
				
				outFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setEdgeSize(int edgeSize) {
		this.edgeSize = edgeSize;
	}

	public int getEdgeSize() {
		return edgeSize;
	}

}

