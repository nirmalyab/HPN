package org.nirmalya.consistency;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nirmalya.hpn.HPNUlilities;
import org.nirmalya.hpn.Scores;
import org.nirmalya.hpn.ZScore;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class Consistency {

	int penaltyType;
	String codePath;
	int partitionSize;

	Set<String> subNetGenes;

	Multimap<String, String> subNetwork;
	/* Contains the subnetwork */
	String subNetFile;

	String globalLevelFile;
	String calculateLocalLevelFile;
	String inheritedLocalLevelFile;

	String oriLevelFile;

	int totalGraphs;

	public Consistency(String organism, String xmlFile, String oriGraphFile,
			String codePath, int partitionSize, int oriLevel, int totalGraphs) {

		this.penaltyType = ZScore.ADJ_PENALTY;
		this.codePath = codePath;
		this.partitionSize = partitionSize;
		this.totalGraphs = totalGraphs;

		this.subNetGenes = getSubNetGenes(organism, xmlFile);
		this.subNetwork = getSubNetwork(subNetGenes, oriGraphFile);
		HPNUlilities.dumpLocalGraph(subNetwork, subNetFile);
		HPNUlilities.createLevelFile(codePath, oriGraphFile, oriLevel,
				globalLevelFile, penaltyType, partitionSize);

	}

	/**
	 * This function accepts an XML file and the name of the organism and
	 * returns the collection of transcription factors as subnetworks.
	 * 
	 * @param organism
	 * @param file
	 * @return
	 */
	Set<String> getSubNetGenes(String organism, String file) {

		Set<String> subNetGenes = Sets.newHashSet();

		/* Process the XML file */

		return subNetGenes;
	}

	Multimap<String, String> getSubNetwork(Set<String> subNetGenes,
			String oriFile) {
		Multimap<String, String> localMap = HashMultimap.create();

		try {

			BufferedReader inFile = new BufferedReader(new FileReader(oriFile));

			String regex = "^(\\S+)\\s+(\\S+)";
			Pattern pat = Pattern.compile(regex);

			String line = null;

			while (null != (line = inFile.readLine())) {
				Matcher mat = pat.matcher(line);

				if (mat.find()) {
					String first = mat.group(1);
					String sec = mat.group(2);

					if (subNetGenes.contains(first)
							&& subNetGenes.contains(sec)) {
						localMap.put(first, sec);
					}
				}
			}

			inFile.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return localMap;

	}

	public ConScores getConScores(String globalLevelFile, int level) {

		/* Get the localLevel */

		/* get the local ZScore and penalty */

		int inheritedLevel = getLevel(subNetGenes, oriLevelFile);

		/* local the global level */
		HPNUlilities.createLevelFile(codePath, subNetFile, inheritedLevel,
				calculateLocalLevelFile, penaltyType, partitionSize);

		/* get the global ZScore and penalty */

		ZScore zscoreGlobal = new ZScore(subNetFile, calculateLocalLevelFile,
				penaltyType);
		Scores globalSores = zscoreGlobal.getZScore(totalGraphs, penaltyType);
		return null;

	}

	private int getLevel(Set<String> subNetGenes2, String oriLevelFile2) {

		Set<Integer> localSet = Sets.newHashSet();

		try {
			BufferedReader inFile = new BufferedReader(new FileReader(
					oriLevelFile2));
			String regex = "^(\\S+)\\s+(\\S+)";
			Pattern pat = Pattern.compile(regex);

			String line = null;

			while (null != (line = inFile.readLine())) {
				Matcher mat = pat.matcher(line);

				if (mat.find()) {
					String key = mat.group(1);
					int val = Integer.parseInt(mat.group(2));

					if (subNetGenes2.contains(key) && val != -1
							&& !localSet.contains(val)) {

						localSet.add(val);

					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return localSet.size();
	}
}

class ConScores {

	double globalZScore;
	int globalPenalty;
	double localZScore;
	int localPenalty;
}
