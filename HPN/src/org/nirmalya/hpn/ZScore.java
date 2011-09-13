package org.nirmalya.hpn;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math.stat.StatUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * This class calculates ZScore for the adjacency and reachability penalty.
 * @author Nirmalya
 *
 */

public class ZScore {
	
	public static int ADJ_PENALTY = 0;
	public static int REACH_PENALTY = 1;
	
	int totalNode = 0;
	List<String> levelKeys = Lists.newArrayList();
	List<Integer> levelVals = Lists.newArrayList();
	
	TreeMap<Integer, Integer> freqMap = Maps.newTreeMap();
	TreeMap<Integer, Integer> probMap = Maps.newTreeMap();
	
	List<Double> adjPenaltyArr = Lists.newArrayList();
	List<Double> reachPenaltyArr = Lists.newArrayList();

	Graph localGraph;
	
	/**
	 * This constructor is used for the robustness test.
	 * @param localMap
	 * @param levelFile
	 * @param penaltyType
	 */
	public ZScore(Multimap<String, String> localMap, String levelFile, int penaltyType) {
		localGraph = new Graph(localMap);
		readLevelMap(levelFile);
		zScoreExtra(penaltyType);
	}
	
	public ZScore(String oriGraphFile, String levelFile, int penaltyType) {
		localGraph = new Graph(oriGraphFile);
		readLevelMap(levelFile);
		zScoreExtra(penaltyType);
	}
	
	
	public ZScore(String oriGraphFile, String levelValFile, int penaltyType,
			String levelKeyFile) {
		localGraph = new Graph(oriGraphFile);
		readLevelMap(levelKeyFile, levelValFile);
		zScoreExtra(penaltyType);
	}

	private void zScoreExtra(int penaltyType) {
		localGraph.setLevelKeys(levelKeys);
		localGraph.cleanData(levelKeys);
		if (penaltyType == REACH_PENALTY) {
			localGraph.populateTCGraph();
		}
	}	

	void readLevelMap(String levelFile) {
		try {
			BufferedReader levelReader = new BufferedReader(new FileReader(levelFile));
			
			String regex = "^(\\S+)\\s+(\\S+)";
			Pattern pat = Pattern.compile(regex);
			
			String line = null;
			
			int localCount = 0;
			while (null != (line = levelReader.readLine())) {
				
				if (localCount == 0) {
					localCount++;
					continue;
				}
				Matcher mat = pat.matcher(line);
				
				if (mat.find()) {
					String key = mat.group(1);
					int val = Integer.parseInt(mat.group(2));
					if (!levelKeys.contains(key) && val != -1) {
						levelKeys.add(key);
						levelVals.add(val);
					}
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	void readLevelMap(String levelKeyFile, String levelValFile) {
		try {
			BufferedReader levelKeyReader = new BufferedReader(new FileReader(levelKeyFile));
			BufferedReader levelValReader = new BufferedReader(new FileReader(levelValFile));
			
			String regex = "^\\S+\\s+(\\S+)";
			Pattern pat = Pattern.compile(regex);
			
			String keyLine = null;
			String valLine = null;
	
			// Skip the first line of val.
			
			valLine = levelValReader.readLine();
			
			while (null != (keyLine = levelKeyReader.readLine())) {
				valLine = levelValReader.readLine();

				Matcher mat = pat.matcher(keyLine);
				
				String key = null;
				if (mat.find()) {
					key = mat.group(1);					
				}
				
				mat = pat.matcher(valLine);
				Integer val = null;
				if (mat.find()) {
					val = Integer.parseInt(mat.group(1));
				}
				
				if (val != -1) 
				{
					levelKeys.add(key);
					levelVals.add(val);
				}
				
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	
	void getFrequency() {
		if (0 == levelVals.size()) {
			System.out.println("There is no element in the levelMap!");
			throw new RuntimeException();
		}
		
		for (Integer levelVal: levelVals) {
			
			int freqKey = levelVal;
			int freqVal = 0;
			
			if (freqMap.containsKey(freqKey)) {
				freqVal = freqMap.get(freqKey); 
			}
			freqMap.put(freqKey, ++freqVal);
		}
	}
	
	void calculateProbMap() {
		
		int cumuCount = 0;
		for (Entry<Integer, Integer> entry : freqMap.entrySet()) {
			
			int key = entry.getKey();
			int value = entry.getValue();

			cumuCount += value;
			probMap.put(cumuCount, key);
		}
		
		totalNode = cumuCount;
		
		if (totalNode <= 0) {
			String errStr = "Number of total node is less than zero.";
			throw new RuntimeException(errStr);
		}
	}
	
	/**
	 * These are the fields related to the function following.
	 */
	// The seed can be some arbitrary number.
	long SEED = 10101;
	// The random number to create the random labeled graphs.
	Random rand = new Random(SEED);	
	
	public void createPenalties(int totalGraphs, int penaltyType) {
		
		for (int i = 0; i < totalGraphs; i++) {
			
			List<Integer> localRandomValSet = Lists.newArrayList();
			
			for (int j = 0; j < totalNode; j++) {
			
				/**
				 * It's time to fill up the vector according to probMap. We shall generate
				 * a number between 0 and totalNode (excluding totalNode) and shall 
				 * generate the corresponding levelNumber.
				 */
				
				int localRand = rand.nextInt(totalNode) + 1;
				
				int randLevel = -2;
				
				if (localRand <= 0 || localRand > totalNode) {
					// This cannot be possible.
					String errStr = "Out of range random number " + localRand;
					throw new RuntimeException(errStr);
				} else {
					randLevel = probMap.ceilingEntry(localRand).getValue();
				}
				
				if (-2 == randLevel) {
					String errStr = "Value of randLevel has not been updated. " +
							"The present value is " + randLevel;
					throw new RuntimeException(errStr);
				}
				
				localRandomValSet.add(randLevel);
			}
			
			printFrequency(localRandomValSet);
			
			if (penaltyType == ADJ_PENALTY) {
				double localAdjPenalty = localGraph.calculateAdjacencyPenalty(localRandomValSet);
				adjPenaltyArr.add(localAdjPenalty);
			} else if (penaltyType == REACH_PENALTY) {
				double localReachPenalty = localGraph.calcualteReachabilityPenalty(localRandomValSet); 
				reachPenaltyArr.add(localReachPenalty);
			} else {
				String err = "Invalid penalty type";
				throw new RuntimeException(err);
			}
			
			
		}
		
	}
	
	@SuppressWarnings("unused")
	private void printFrequency(List<Integer> localRandomValSet) {
		TreeMap<Integer, Integer> localFreqMap = Maps.newTreeMap();
		for (Integer levelVal: localRandomValSet) {
			
			int freqKey = levelVal;
			int freqVal = 0;
			
			if (localFreqMap.containsKey(freqKey)) {
				freqVal = localFreqMap.get(freqKey); 
			}
			localFreqMap.put(freqKey, ++freqVal);
		}
		
		for (Entry<Integer, Integer> entry: localFreqMap.entrySet()) {
			int key = entry.getKey();
			int val = entry.getValue();
			
			//System.out.println(key + " -> " + val + " ");
		}
		//System.out.println("\n\n");
	}

	public Scores getZScore(int totalGraphs, int penaltyType) {

		getFrequency();
		calculateProbMap();
		createPenalties(totalGraphs, penaltyType);	
		return calculateZScore(penaltyType);
		
	}

	Scores calculateZScore(int penaltyType) {
		
		if (penaltyType == ADJ_PENALTY) {
		double[] localAdjPenaltyArr = makeDoubleArr(adjPenaltyArr);
		double adjMean = StatUtils.mean(localAdjPenaltyArr);
		double adjVariance = StatUtils.variance(localAdjPenaltyArr);
		double adjStd = Math.sqrt(adjVariance);
		
		//printFrequency(levelVals);
		double localAdjPenalty = localGraph.calculateAdjacencyPenalty(levelVals);
		
		double ZScoreAdj = (adjMean - localAdjPenalty) / adjStd;
		return new Scores(ZScoreAdj, localAdjPenalty);
		
		} else if (penaltyType == REACH_PENALTY) {
		
		double[] localReachPenaltyArr = makeDoubleArr(reachPenaltyArr);
		double reachMean = StatUtils.mean(localReachPenaltyArr);
		double reachVariance = StatUtils.variance(localReachPenaltyArr);
		double reachStd = Math.sqrt(reachVariance);
		
		double localReachPenalty = localGraph.calcualteReachabilityPenalty(levelVals);
		
		double ZScoreReach = (reachMean - localReachPenalty) / reachStd;
		
		return new Scores(ZScoreReach, localReachPenalty);	
		} else {
			String err = "Invalid penalty type";
			throw new RuntimeException(err);
		}
		
	}

	private double[] makeDoubleArr(List<Double> adjPenaltyArr2) {

		double[] localArr = new double[adjPenaltyArr2.size()];
		
		for (int i = 0; i < adjPenaltyArr2.size(); i++) {
			localArr[i] = adjPenaltyArr2.get(i);
		}
		return localArr;
	}	
	
	public static void main(String[] args) {
		
		String oriGraphFile = args[0];
		String levelFile = args[1];
		int totalGraphs = Integer.parseInt(args[2]);
		int penaltyType = Integer.parseInt(args[3]);
		
		ZScore zscore = new ZScore(oriGraphFile, levelFile, penaltyType);
		Scores scores = zscore.getZScore(totalGraphs, penaltyType);
		
		if (penaltyType == ZScore.ADJ_PENALTY) {
		System.out.println(
				"Adjacency ZScore: "
				+ scores.ZScore 
				+ "\n"
				+ "Adjacency penalty: "
				+ scores.penalty
				);		
		} else if (penaltyType == ZScore.REACH_PENALTY) {
			System.out.println(
				"Reachability ZScore: "
				+ scores.ZScore 
				+ "\n"
				+ "Reachability penalty: "
				+ scores.penalty
				);	
		}
	}
}

