package org.nirmalya.hpn;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * This class conducts the robustness experiments.
 * @author nirmalya
 *
 */
public class RobustnessExp {


	void doRobustnessExp(String graphName, String levelValFile,
			int totalGraphs, int penaltyType, String outFile) {
		RandomizeGraph rGraph = new RandomizeGraph(graphName);
		int localEdgeCount = rGraph.getEdgeSize();
		List<Integer> shuffleCountVec = Lists.newArrayList();
		
		int division = 20;
		for (int i = 0; i < division; i++) {
			int localShuffleCount = (int)Math.floor(localEdgeCount / division) * (i + 1);
			shuffleCountVec.add(localShuffleCount);
		}
		
		List<Multimap<String, String>>  mapList = rGraph.getEdgeShuffleRandomGraphs(shuffleCountVec);

		try {
			
			PrintWriter outWriter = new PrintWriter(new FileWriter(new File(outFile)));
			
			ZScore zscore = new ZScore(graphName, levelValFile, penaltyType);
			Scores scores = zscore.getZScore(totalGraphs, penaltyType);
			
			String outStr = " Shuffle Count: No shuffle " + " Penalty: " + scores.penalty + " ZScore: " + scores.ZScore;
			
			//System.out.println(outStr);				
			outWriter.println(outStr);
			
			
			
			for (int j = 0; j < mapList.size(); j++) {
				
				zscore = new ZScore(mapList.get(j), levelValFile, penaltyType);
				scores = zscore.getZScore(totalGraphs, penaltyType);
				
				outStr = " Shuffle Count: " + shuffleCountVec.get(j)+ " Penalty: " + scores.penalty + " ZScore: " + scores.ZScore;
				
				//System.out.println(outStr);				
				outWriter.println(outStr);
				
			}
			
			outWriter.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		String graphName = args[0];
		String levelValFile = args[1];
		int totalGraphs = Integer.parseInt(args[2]);
		int penaltyType = Integer.parseInt(args[3]);
		String outFile = args[4];
		
		RobustnessExp robustExp = new RobustnessExp();
		robustExp.doRobustnessExp(graphName, levelValFile, totalGraphs, penaltyType,
				outFile);
		
		
	}

}
