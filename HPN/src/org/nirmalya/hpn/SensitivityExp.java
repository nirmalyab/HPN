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
							String workDir) {

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
			createLevelFile(codePath, graphFile, levelNum, oriLevelFile, penaltyType);
			
			ZScore zscore = new ZScore(graphFile, oriLevelFile, penaltyType);
			Scores scores = zscore.getZScore(totalGraphs, penaltyType);
			
			PrintWriter outWriter = new PrintWriter(new FileWriter(new File(outFile)));
			
			String outStr = " Shuffle Count: 0 " + " Penalty: " 
						+ scores.penalty + " ZScore: " + scores.ZScore;
			
			//System.out.println(outStr);				
			outWriter.println(outStr);		
			
			
			for (int j = 0; j < mapList.size(); j++) {
				
				//get localGraphPath				
				String localGraphFile = graphFilePrefix + "." + shuffleCountVec.get(j);	
				
				dumpLocalGraph(mapList.get(j), localGraphFile);
				// get the levelFile path
				String localLevelFile = levelFilePrefix + "." + shuffleCountVec.get(j);					

				createLevelFile(codePath, localGraphFile, levelNum, 
								localLevelFile, penaltyType);				
				
				zscore = new ZScore(localGraphFile, localLevelFile, penaltyType);
				scores = zscore.getZScore(totalGraphs, penaltyType);
				
				outStr = " Shuffle Count: " + shuffleCountVec.get(j)
								+ " Penalty: " + scores.penalty 
								+ " ZScore: " + scores.ZScore;
				
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
	
	private void dumpLocalGraph(Multimap<String, String> multimap,
			String localGraphFile) {
		
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(localGraphFile));
			
			for (Entry<String, String> e : multimap.entries()) {
				String first = e.getKey();
				String sec = e.getValue();
				
				writer.println(first + "  " + sec);
			}
			
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}

	private void  createLevelFile (String codePath, 
						String graphFile, 
						int level, 
						String outFile, 
						int penaltyType) {		
		try {

			Runtime rt = Runtime.getRuntime();
			
			String command = codePath + " " +
							graphFile + " " +
							level + " " +
							outFile + " " +
							penaltyType;
			
			Process pro = rt.exec(command);
			
			int status;
			if (0==(status = pro.waitFor())) {
				String errStr = "The process " + command +
				" exited abnormally, whith exist status: " + status;
				throw new RuntimeException(errStr);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}	

}
