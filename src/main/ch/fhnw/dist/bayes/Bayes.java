package ch.fhnw.dist.bayes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Bayes {

	protected static HashMap<String, Integer> hamWords = null;
	protected static HashMap<String, Integer> spamWords = null;
	protected static int hamMailCount = 0;
	protected static int spamMailCount = 0;
	private final static boolean CACHE_TRAINING = false;
	
	/**
	 * Train --> create Wordlist
	 * @throws IOException
	 */
	protected static void train() throws IOException {
		
		File f = new File("hamwords.ser");
		hamMailCount = countWords("res/ham-anlern.zip"); //TODO: Nur einmal laden?
		if(!f.exists() || !CACHE_TRAINING) { 
			hamWords = createWordList("res/ham-anlern.zip");
			saveHashMap(hamWords, "hamwords.ser");
		} else {
			hamWords = loadHashMap("hamwords.ser");
		}
		
		f = new File("spamwords.ser");
		spamMailCount = countWords("res/spam-anlern.zip");//TODO: Nur einmal laden?
		if(!f.exists() || !CACHE_TRAINING) { 
			spamWords = createWordList("res/spam-anlern.zip");
			saveHashMap(spamWords, "spamwords.ser");
		} else {
			spamWords = loadHashMap("spamwords.ser");
		}
		
		
//	    System.out.println("---------- Ham Words --------------");
//	    for (Map.Entry entry : hamWords.entrySet()){
//	    	  System.out.println(entry.getKey() + " " + entry.getValue());
//	    }
//	    System.out.println("----------- Word Count: " + spamWords.size());
//	    System.out.println("---------- Spam Words --------------");
//	    for (Map.Entry entry : spamWords.entrySet()){
//	    	  System.out.println(entry.getKey() + " " + entry.getValue());
//	    }
//	    System.out.println("----------- Word Count: " + spamWords.size());

	    
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
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
	 * Count Mails
	 */
	private static int countWords(String filename) throws IOException {
		try(ZipFile file = new ZipFile(filename)){
			return file.size();
		}
	}
	
	/**
	 * Creates word list of a zip file
	 * @param zipfile path 
	 * @return HashMap<String, Integer> -- Word, Count
	 * @throws IOException
	 */
	private static HashMap<String, Integer> createWordList(String filename) throws IOException {
		HashMap<String, Integer> wordCount = new HashMap<>();
		try(ZipFile file = new ZipFile(filename)){
			Enumeration<? extends ZipEntry> zipEntries = file.entries();
			
			while(zipEntries.hasMoreElements()){
				ZipEntry entry = zipEntries.nextElement();
				InputStream fileInputStream = file.getInputStream(entry);
				Set<String> wordSetMail = countWords(fileInputStream);
				for(String word : wordSetMail){
					if (wordCount.containsKey(word)) { 
						int n = wordCount.get(word);    
						wordCount.put(word, ++n);
					}
					else {
						wordCount.put(word, 1);
					}
				}
			}
		}
		return wordCount;
	}

	/**
	 * Count words in given file stream. Same word is only counted once
	 * @param fileInputStream
	 * @return List of all words in given file. Only counted once
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	protected static Set<String> countWords(InputStream fileInputStream)
			throws UnsupportedEncodingException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"));
		
		Set<String> wordSet = new HashSet<>();
		String line = "";
		int newLines = 0; //E-Mail Body starts with \n\n\n
		Boolean bodyReached = false;
		while ((line = reader.readLine()) != null) {
			if(bodyReached){
				// String[] words = line.toLowerCase().split("\\b");
				String[] words = line.toLowerCase().split("[^a-zA-Z]+");
				
				for (int i = 0; i < words.length; i++) {
					String w = words[i];
					if(w != null && !w.trim().isEmpty()){
						wordSet.add(w);
					}
				}
			}
			
			if(!bodyReached && "".equals(line)){
				newLines++;
				if(newLines == 2){
					bodyReached = true;
				}
			}else{
				newLines = 0;
			}
			
			
		}
		return wordSet;
	}
	
	

}
