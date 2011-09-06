package org.nirmalya.hpn;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;

public class TestClass {
	
	public static void main(String [] args) throws Exception {
		String oriFile = args[0];
		Set<String> localSet = Sets.newHashSet();
		
		BufferedReader inFile = new BufferedReader(new FileReader(oriFile));
		
		String regex = "(\\S+)\\s+(\\S+)";
		Pattern pat = Pattern.compile(regex);
		
		String line = null;
		
		while (null != (line = inFile.readLine())) {
			Matcher mat = pat.matcher(line);
			
			if (mat.find()) {
				String key = mat.group(1);
				String val = mat.group(2);
				
				localSet.add(key);
				localSet.add(val);
			}
		}
		System.out.println("Size: " + localSet.size());
	}

}
