package org.nirmalya.hpn;

import java.io.File;

/**
 * This class will run the other class RobustnessExp in batch mode within a directory.
 * @author nirmalya
 *
 */

public class RobustnessBatch {
	
	public static void main(String[] args) {
		
		String graphName = args[0];
		String levelDir = args[1];
		
		int totalGraphs = Integer.parseInt(args[2]);
		int penaltyType = Integer.parseInt(args[3]);
		
		File levelDirFile = new File(levelDir);
		String outDir = levelDir + "/robustness/";
		if (!(new File(outDir)).mkdir()) {
			throw new RuntimeException();
		}
		
		for(File levelFile : levelDirFile.listFiles()) {
			if (!levelFile.isDirectory()) {
				String localFile = levelFile.getName();
				String outFile = outDir + localFile;
				RobustnessExp robustExp = new RobustnessExp();
				robustExp.doRobustnessExp(graphName, levelFile.getAbsolutePath(), totalGraphs, penaltyType,
						outFile);
				System.out.println("Completed for file: " + localFile);
			}
		}

		
	}

}
