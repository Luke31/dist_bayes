package ch.fhnw.dist.bayes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Bayes {


	public static void main(String[] args) throws IOException {
	    train();
	}
	
	private static HashMap<String, Integer> hamWords = null;
	private static HashMap<String, Integer> spamWords = null;
	
	/**
	 * Train --> create Wordlist
	 * @throws IOException
	 */
	private static void train() throws IOException {
		
		File f = new File("res/ham-anlern.zip");
		if(!f.exists()) { 
			hamWords = createWordList("res/ham-anlern.zip");
			saveHashMap(hamWords, "hamwords.ser");
		} else {
			hamWords = loadHashMap("hamwords.ser");
		}
		
		f = new File("res/ham-anlern.zip");
		if(!f.exists()) { 
			spamWords = createWordList("res/spam-anlern.zip");
			saveHashMap(spamWords, "spamwords.ser");
		} else {
			spamWords = loadHashMap("spamwords.ser");
		}
		
		/*
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
	    */
	}
	
	/**
	 * Serialize Hashmap to File
	 * @param map
	 * @param filename
	 */
	private static void saveHashMap(HashMap<String, Integer> map, String name) {
		try {
			FileOutputStream fos = new FileOutputStream(name);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(map);
			oos.close();
			fos.close();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Deserialize Hashmap from file
	 */
	private static HashMap<String, Integer> loadHashMap(String filename) {
		HashMap<String, Integer> map = null;
		try
		{
		   FileInputStream fis = new FileInputStream(filename);
		   ObjectInputStream ois = new ObjectInputStream(fis);
		   map = (HashMap) ois.readObject();
		   ois.close();
		   fis.close();
		}catch(IOException ioe) {
		   ioe.printStackTrace();
		}catch(ClassNotFoundException c) {
		   System.out.println("Class not found");
		   c.printStackTrace();
		}
		return map;
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
