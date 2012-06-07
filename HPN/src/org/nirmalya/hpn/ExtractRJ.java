package org.nirmalya.hpn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/* This file is related to RajaJothi method */
public class ExtractRJ {
	HashMap<String, List<Integer>> randMap = Maps.newHashMap();
	
	long seed = 101010101;
	Random rand = null;
	int randIter = 1000;
	
	public ExtractRJ(String randomFile) {
		
		rand = new Random(seed);		
		try {
			BufferedReader inFile = new BufferedReader(new FileReader(randomFile));
			
			String line = null;
			
			while (null != (line = inFile.readLine())) {
				String[] splitted = line.split("\\s+");
				
				String key = splitted[0];
				if (!randMap.containsKey(key)) {
					ArrayList<Integer> localList = Lists.newArrayList();
					
					for (int i = 1; i < splitted.length; i++) {
						localList.add(Integer.parseInt(splitted[i]));
					}
					randMap.put(key, localList);
				}
			}
			inFile.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public void getRandomKeyVals(List<String> keys, List<Integer> vals) {
		
		for (Entry<String, List<Integer>> en : randMap.entrySet()) {
			
			String localKey = en.getKey();
			List<Integer> localVals = en.getValue();
			Integer localVal = null;
			
			if (localVals.size() == 1) {
				localVal = localVals.get(0);
			} else {
				int index = rand.nextInt(localVals.size());
				localVal = localVals.get(index);
			}
			keys.add(localKey);
			vals.add(localVal);
			
		}
	}
	
	public static void main(String[] args) {
		
		String oriGraph = args[0];
		ExtractRJ exrj = new ExtractRJ(args[1]);
		
		int penVal = Integer.MAX_VALUE;
		int penaltyType = Integer.parseInt(args[2]);
		int totalGraphs = 100;

		
		List<String> finalKeys = null;
		List<Integer> finalVals = null;
		
		for (int i = 0; i < exrj.randIter; i++) {
			
			List<String> keys = Lists.newArrayList();
			List<Integer> vals = Lists.newArrayList();
			
			exrj.getRandomKeyVals(keys, vals);
			
			ZScore zScore = new ZScore(oriGraph, keys, vals, penaltyType);
			
			int localPen = -1;
			
			if (penaltyType == ZScore.ADJ_PENALTY)
				localPen = zScore.getOriAdjPenalty();
			else
				localPen = zScore.getOriReachPenalty();
			
			System.out.println("Present penalty: " + localPen);
			
			if (localPen < penVal) {
				penVal = localPen;
				finalKeys = keys;
				finalVals = vals;
			}			
		}

		ZScore zScore = new ZScore(oriGraph, finalKeys, finalVals, penaltyType);
		Scores scores = zScore.getZScore(totalGraphs, penaltyType);

		System.out.println(
				"ZScore: "
				+ scores.zScore 
				+ "\n"
				+ "penalty: "
				+ scores.penalty
				);		
		
		
	}

}
