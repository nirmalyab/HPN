package org.nirmalya.hpn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;

/**
 * This class runs the sensitivity experiments in batch mode.
 * It reads the arguments from an argument file. This file is maintained through EGIT.
 * @author Nirmalya
 *
 */
public class SensitivityBatch {
	
	String codePath; 
	String graphFile; 
	String organism;
	int totalGraphs; 
	int penaltyType; 
	String outDir; 
	TreeSet<Integer> levelNums = Sets.newTreeSet();
	private int partitionSize;

	
	public static void main(String[] args) {
		
		String argFile = args[0];
		
		SensitivityBatch sBatch = new SensitivityBatch();		
		sBatch.processArguments(argFile);
		sBatch.doExpsInbatch();
		
	}
	
	public void doExpsInbatch() {
		
		String localOutDir = outDir + "/" + organism + "." + penaltyType;

		if (!(new File(localOutDir).mkdirs())) {
			String errStr = "Warning! Directory creation failed localOutDir: " + localOutDir;
		}
		
		for (int level : levelNums) {
			
			
			String localWorkDir = localOutDir + "/workDir." + level;
			if (!(new File(localWorkDir).mkdirs())) {
				String errStr = "Directory creation failed localWorkDir: " + localWorkDir;
			}
			
			SensitivityExp sExp = new SensitivityExp();
			
			/* Create name of outFile */
			String outFile = localOutDir + "/resultFile." + level;									 
			
			sExp.doSensitivityExp(codePath, graphFile, totalGraphs, penaltyType, 
								outFile, level, localWorkDir, partitionSize);
			
		}
		
	}

	private void processArguments(String argFile) {
		try {
			BufferedReader inFile = new BufferedReader(new FileReader(argFile));
			String regex = "^(\\S+)\\s+(\\S+)";
			Pattern pat = Pattern.compile(regex);
			
			String line = null;
			
			while (null != (line = inFile.readLine())) {
				Matcher mat = pat.matcher(line);
				
				if (mat.find()) {
					
					String key = mat.group(1);
					String value = mat.group(2);
					
					if (key.equalsIgnoreCase("codePath")) {
						this.codePath = value;
					} else if (key.equalsIgnoreCase("graphFile")) {
						this.graphFile = value;
					} else if (key.equalsIgnoreCase("totalGraphs")) {
						this.totalGraphs = Integer.parseInt(value);
					} else if (key.equalsIgnoreCase("penaltyType")) {
						this.penaltyType = Integer.parseInt(value);
					} else if (key.equalsIgnoreCase("outDir")) {
						this.outDir = value;
					} else if (key.equalsIgnoreCase("levelNums")) {
						processLevelNumStr(value);
					} else if (key.equalsIgnoreCase("organism")) {
                        this.organism = value;
					} else if (key.equalsIgnoreCase("partitionSize")) {
						this.partitionSize = Integer.parseInt(value);
					}
				}
			}
			
				inFile.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private void processLevelNumStr(String value) {

		String[] ranges = value.split(",");
		
		for (String str : ranges) {
			if (str.contains("-")) {
				String[] localRange = str.split("-");
				int start = Integer.parseInt(localRange[0]);
				int end = Integer.parseInt(localRange[1]);
				
				for (int j = start; j <= end; j++) {
					levelNums.add(j);
				}
			} else {
				levelNums.add(Integer.parseInt(str));
			}
		}
		
	}

}

