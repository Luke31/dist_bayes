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
	    HashMap<String, Integer> hamWords = createWordList("res/ham-anlern.zip");
	    HashMap<String, Integer> spamWords = createWordList("res/spam-anlern.zip");
	    
	    System.out.println("---------- Ham Words --------------");
	    for (Map.Entry entry : hamWords.entrySet()){
	    	  System.out.println(entry.getKey() + " " + entry.getValue());
	    }
	    System.out.println("----------- Word Count: " + spamWords.size());
	    System.out.println("---------- Spam Words --------------");
	    for (Map.Entry entry : spamWords.entrySet()){
	    	  System.out.println(entry.getKey() + " " + entry.getValue());
	    }
	    System.out.println("----------- Word Count: " + spamWords.size());
	}
	
	/**
	 * Creates word list of a zip file
	 * @param zipfile path 
	 * @return HashMap<String, Integer> -- Word, Count
	 * @throws IOException
	 */
	private static HashMap<String, Integer> createWordList(String filename) throws IOException {
		HashMap<String, Integer> wordCount = new HashMap<>();
		ZipFile file = new ZipFile(filename);
		Enumeration<? extends ZipEntry> zipEntries = file.entries();

		while(zipEntries.hasMoreElements()){
			ZipEntry entry = zipEntries.nextElement();
			BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(entry), "UTF-8"));

			String line = "";
			while ((line = reader.readLine()) != null) {
				// String[] words = line.toLowerCase().split("\\b");
				String[] words = line.toLowerCase().split("[^a-zA-Z]+");
			
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
		return wordCount;
	}

}
