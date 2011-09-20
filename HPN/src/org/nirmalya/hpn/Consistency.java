package org.nirmalya.hpn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * 
 * @author nirmalya
 * 
 */
public class Consistency {

	/* For this experiment, we assume the penalty to be always ADJ_PENALTY (=0). */
	private int penaltyType = ZScore.ADJ_PENALTY;

	private Set<String> subNetGenes;
	private Multimap<String, String> subNetwork;

	private String resultDir;
	private String subNetFile;
	private String globalLevelFile;
	private String calculateLocalLevelFile;
	private String inheritedLocalLevelFile;
	private String outFile;

	private String species;
	private String xmlFile;
	private String oriGraphFile;
	private String codePath;
	private int oriLevel;
	private int partitionSize;
	private int totalGraphs;

	public Consistency(String argFile) {
		readArgs(argFile);
		createPaths();

	}

	private void createPaths() {

		subNetFile = resultDir + "/subnet.txt";
		globalLevelFile = resultDir + "/globalLevel.txt";
		calculateLocalLevelFile = resultDir + "/calculatedLocalLevel.txt";
		inheritedLocalLevelFile = resultDir + "/inheritedLocalLevel.txt";
		outFile = resultDir + "/outFile.txt";

	}

	private void readArgs(String argFile) {
		try {
			BufferedReader inFile = new BufferedReader(new FileReader(argFile));

			String regex = "^(\\S+)\\s+(\\S+)";
			Pattern pat = Pattern.compile(regex);

			String line = null;
			while (null != (line = inFile.readLine())) {
				Matcher mat = pat.matcher(line);

				if (mat.find()) {
					String key = mat.group(1);
					String val = mat.group(2);

					if (key.equals("species")) {
						this.species = val;
					} else if (key.equals("xmlFile")) {
						this.xmlFile = val;
					} else if (key.equals("oriGraphFile")) {
						this.oriGraphFile = val;
					} else if (key.equals("codePath")) {
						this.codePath = val;
					} else if (key.equals("oriLevel")) {
						this.oriLevel = Integer.parseInt(val);
					} else if (key.equals("partitionSize")) {
						this.partitionSize = Integer.parseInt(val);
					} else if (key.equals("totalGraphs")) {
						this.totalGraphs = Integer.parseInt(val);
					} else if (key.equals("resultDir")) {
						this.resultDir = val;
					} else {
						String errStr = "Illegal argument: " + key + " " + val;
						throw new RuntimeException(errStr);
					}
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initialize() {

		this.subNetGenes = getSubNetGenes(species, xmlFile);
		this.subNetwork = getSubNetwork(subNetGenes, oriGraphFile);
		HPNUlilities.dumpLocalGraph(subNetwork, subNetFile);
		/* Create level file for the original graph */
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
	private Set<String> getSubNetGenes(String organism, String file) {

		Set<String> localSubNetGenes = Sets.newHashSet();
		Document doc = openDoc(file);

		// Now I got the doc. So I can traverse the file
		Element root = doc.getDocumentElement();

		NodeList nodes = root.getChildNodes();

		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			if (node.getNodeName().equals("entry")) {

				String localName = null;
				String objType = null;

				NamedNodeMap localAttrs = node.getAttributes();

				for (int k = 0; k < localAttrs.getLength(); k++) {

					Node localAt = localAttrs.item(k);

					if (localAt.getNodeName().equals("name")) {
						localName = localAt.getNodeValue();
					} else if (localAt.getNodeName().equals("type")) {
						objType = localAt.getNodeValue();
					}
				}

				processEntry(localSubNetGenes, localName, objType);
			}

		}
		return localSubNetGenes;
	}

	private Document openDoc(String file) {
		Document doc = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();
			doc = parser.parse(file);
			// System.out.println(fileName + " is well-formed.");
		} catch (SAXException e) {
			System.out.println(file + " is not well-formed.");
		} catch (IOException e) {
			System.out
					.println("Due to an IOException, the parser could not check "
							+ file);
		} catch (FactoryConfigurationError e) {
			System.out.println("Could not locate a factory class");
		} catch (ParserConfigurationException e) {
			System.out.println("Could not locate a JAXP parser");
		}
		return doc;
	}

	private void processEntry(Set<String> localSubNetGenes, String localName,
			String objType) {
		if (objType.equals("gene")) {

			String buffer = localName;
			String[] localStrs = buffer.split("\\s+");

			for (String localStr : localStrs) {
				if (localStr.startsWith(species)) {
					int index = localStr.indexOf(":");
					String subString = localStr.substring(index + 1);
					if (!localSubNetGenes.contains(subString)) {
						localSubNetGenes.add(subString);
					}
				}
			}
		}
	}

	private Multimap<String, String> getSubNetwork(Set<String> subNetGenes,
			String oriFile) {
		Multimap<String, String> localMap = HashMultimap.create();

		System.out.println(subNetGenes.toString());

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

					boolean valFirst = subNetGenes.contains(first);
					boolean valSec = subNetGenes.contains(sec);

					if (valFirst || valSec) {
						// System.out.println("Discovering a pair: " +
						// subNetGenes.contains(first) + " " +
						// subNetGenes.contains(sec));
					}

					if (subNetGenes.contains(first)
							&& subNetGenes.contains(sec)) {
						System.out.println("Adding a pair: " + first + " "
								+ sec);
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

	public ConScores getConScores() {

		/*
		 * This obtains the inherited local level and put that into
		 * inheritedLocalLevelFile.
		 */
		int inheritedLevel = getLevel(subNetGenes, globalLevelFile,
				inheritedLocalLevelFile);
		// Now we can calculate the zscores on that inheritedLocalLevelFile.

		ZScore zscoreInherited = new ZScore(subNetFile,
				inheritedLocalLevelFile, penaltyType);
		Scores scoreInherited = zscoreInherited.getZScore(totalGraphs,
				penaltyType);

		/* local the global level */
		HPNUlilities.createLevelFile(codePath, subNetFile, inheritedLevel,
				calculateLocalLevelFile, penaltyType, partitionSize);

		/* get the global ZScore and penalty */

		ZScore zscoreCalculated = new ZScore(subNetFile,
				calculateLocalLevelFile, penaltyType);
		Scores scoreCalculated = zscoreCalculated.getZScore(totalGraphs,
				penaltyType);

		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(outFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ConScores score = new ConScores(scoreInherited.getZScore(),
				scoreInherited.getPenalty(), scoreCalculated.getZScore(),
				scoreCalculated.getPenalty());

		out.println("zScoreInherited: " + score.zScoreInherited
				+ " penaltyInherited: " + score.penaltyInherited
				+ " zScorecalculated: " + score.zScorecalculated
				+ " penaltyCalculated: " + score.penaltyCalculated);

		out.close();

		return score;

	}

	private int getLevel(Set<String> subNetGenes2, String oriLevelFile2,
			String derivedLevelFile) {

		Set<Integer> localSet = Sets.newHashSet();

		try {

			PrintWriter outFile = new PrintWriter(new FileWriter(
					derivedLevelFile));
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

					if (subNetGenes2.contains(key)) {

						outFile.println(key + " " + val);

						if (-1 != val && !localSet.contains(val)) {
							localSet.add(val);
						}

					}
				}
			}

			outFile.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return localSet.size();
	}

	public static void main(String[] args) {
		Consistency consis = new Consistency(args[0]);
		consis.initialize();
		ConScores score = consis.getConScores();
		System.out.println("zScoreInherited: " + score.zScoreInherited
				+ " penaltyInherited: " + score.penaltyInherited
				+ " zScorecalculated: " + score.zScorecalculated
				+ " penaltyCalculated: " + score.penaltyCalculated);

	}

}

class ConScores {

	public ConScores(double zScoreInherited, int penaltyInherited,
			double zScorecalculated, int penaltyCalculated) {

		this.zScoreInherited = zScoreInherited;
		this.penaltyInherited = penaltyInherited;
		this.zScorecalculated = zScorecalculated;
		this.penaltyCalculated = penaltyCalculated;
	}

	double zScoreInherited;
	int penaltyInherited;
	double zScorecalculated;
	int penaltyCalculated;

}
