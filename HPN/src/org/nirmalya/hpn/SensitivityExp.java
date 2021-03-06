package org.nirmalya.hpn;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class SensitivityExp {

	
	public void doSensitivityExp(String codePath, 
							String graphFile, 
							int totalGraphs, 
							int penaltyType, 
							String outFile, 
							int levelNum,
							String workDir, 
							int partitionSize) {

		try {
			
			List<Integer> shuffleCountVec = Lists.newArrayList();
			
			/* Create shuffleCountVec. A small number of set for shuffling will be created for
			 * time constraint.
			 */
			
			for (int i = 5; i < 100; i *= 2) {				
				shuffleCountVec.add(i);
			
			}			
			
			RandomizeGraph rGraph = new RandomizeGraph(graphFile);
			List<Multimap<String, String>>  mapList 
			           = rGraph.getEdgeShuffleRandomGraphs(shuffleCountVec);			
			
			String levelFilePrefix = workDir + "/levelFile";
			String graphFilePrefix = workDir + "/graphFile";
			
			// get the levelFile path. For that run Gunhan's code on the original graphFile.
			String oriLevelFile = levelFilePrefix + ".ori";
			HPNUlilities.createLevelFile(codePath, graphFile, levelNum, oriLevelFile, penaltyType, partitionSize);
			
			ZScore zscore = new ZScore(graphFile, oriLevelFile, penaltyType);
			Scores scores = zscore.getZScore(totalGraphs, penaltyType);
			
			PrintWriter outWriter = new PrintWriter(new FileWriter(new File(outFile)));
			
			String outStr = " Shuffle Count: 0 " + " Penalty: " 
						+ scores.penalty + " ZScore: " + scores.zScore;
			
			//System.out.println(outStr);				
			outWriter.println(outStr);		
			
			
			for (int j = 0; j < mapList.size(); j++) {
				
				//get localGraphPath				
				String localGraphFile = graphFilePrefix + "." + shuffleCountVec.get(j);	
				
				HPNUlilities.dumpLocalGraph(mapList.get(j), localGraphFile);
				// get the levelFile path
				String localLevelFile = levelFilePrefix + "." + shuffleCountVec.get(j);					

				HPNUlilities.createLevelFile(codePath, localGraphFile, levelNum, 
								localLevelFile, penaltyType, partitionSize);				
				
				zscore = new ZScore(localGraphFile, localLevelFile, penaltyType);
				scores = zscore.getZScore(totalGraphs, penaltyType);
				
				outStr = " Shuffle Count: " + shuffleCountVec.get(j)
								+ " Penalty: " + scores.penalty 
								+ " ZScore: " + scores.zScore;
				
				//System.out.println(outStr);				
				outWriter.println(outStr);
				
				// Deleting the two files
				
				
				
			}
			
			outWriter.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	
}
