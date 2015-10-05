package ch.fhnw.dist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Bayes {


	public static void main(String[] args) throws IOException {
	    train();
	}
	
	private static void train() throws IOException {
		
		ZipFile hamTrain = new ZipFile("res/ham-anlern.zip");
		ZipFile spamTrain = new ZipFile("res/spam-anlern.zip");

		HashMap<String, Integer> wordCount = new HashMap<>();

		
	    Enumeration<? extends ZipEntry> hamEntries = hamTrain.entries();

	    while(hamEntries.hasMoreElements()){
	        ZipEntry entry = hamEntries.nextElement();
	       // InputStream stream = hamTrain.getInputStream(entry);
	        
	        BufferedReader reader = new BufferedReader(new InputStreamReader(hamTrain.getInputStream(entry), "UTF-8"));
	        
	        
	        String line = "";
	        while ((line = reader.readLine()) != null) {
	        	String[] words = line.toLowerCase().split("\\b");
	        	for (int i = 0; i < words.length; i++) {
	        		if (wordCount.containsKey(words[i])) { 
        			  int n = wordCount.get(words[i]);    
        			  wordCount.put(words[i], ++n);
        			}
        			else {
        			  wordCount.put(words[i], 1);
        			}
	        	}
	        	
	            
	        }
	        
	    }
	    
	    
	    // Print Word Map
	    for (Map.Entry entry : wordCount.entrySet()){
	    	  System.out.println(entry.getKey() + " " + entry.getValue());
	    }
	    
	    
	    
	    
	    
	}
}
