package org.nirmalya.hpn;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map.Entry;

import com.google.common.collect.Multimap;

public class HPNUlilities {

	public static void createLevelFile(String codePath, String graphFile,
			int level, String outFile, int penaltyType, int partitionSize) {
		try {

			Runtime rt = Runtime.getRuntime();

			String command = codePath + " " + graphFile + " " + level + " "
					+ outFile + " " + penaltyType + " " + partitionSize;

			Process pro = rt.exec(command);

			int status;
			if (0 == (status = pro.waitFor())) {
				String errStr = "The process " + command
						+ " exited abnormally, whith exist status: " + status;
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
	
	public static void dumpLocalGraph(Multimap<String, String> multimap,
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

}
