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

/**
 * File-Handler and word-counting class
 * @author Lukas Schmid, Andreas Gloor
 */
public class BayesFileHandler {

	protected HashMap<String, Integer> hamWords = null;
	protected HashMap<String, Integer> spamWords = null;
	protected int hamMailCount = 0;
	protected int spamMailCount = 0;
	protected String hamZipPath = null;
	protected String spamZipPath = null;
	
	/**
	 * Train --> create Wordlist
	 * @param cache True if trained data shall be cached
	 * @throws IOException
	 */
	protected void loadTrainFiles(boolean cache) throws IOException {
		hamZipPath = "res/ham-anlern.zip";
		spamZipPath = "res/spam-anlern.zip";
		
		File f = new File("hamwords.ser");
		hamMailCount = countMails(hamZipPath);
		if(!f.exists() || !cache) { 
			hamWords = createWordList(hamZipPath);
			saveHashMap(hamWords, "hamwords.ser");
		} else {
			hamWords = loadHashMap("hamwords.ser");
		}
		
		f = new File("spamwords.ser");
		spamMailCount = countMails(spamZipPath);
		if(!f.exists() || !cache) { 
			spamWords = createWordList(spamZipPath);
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
	 * Load calibration files
	 * @throws IOException
	 */
	protected void loadCalibrationFiles() throws IOException {
		hamZipPath = "res/ham-kallibrierung.zip";
		spamZipPath = "res/spam-kallibrierung.zip";
		
		hamMailCount = countMails(hamZipPath);
		spamMailCount = countMails(spamZipPath);
	}
	
	/**
	 * Load test files
	 * @throws IOException
	 */
	protected void loadTestFiles() throws IOException {
		hamZipPath = "res/ham-test.zip";
		spamZipPath = "res/spam-test.zip";
		
		hamMailCount = countMails(hamZipPath);
		spamMailCount = countMails(spamZipPath);
	}
	
	
	/**
	 * Serialize Hashmap to File
	 * @param map
	 * @param filename
	 */
	private void saveHashMap(HashMap<String, Integer> map, String name) {
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
	private HashMap<String, Integer> loadHashMap(String filename) {
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
	private int countMails(String filename) throws IOException {
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
	private HashMap<String, Integer> createWordList(String filename) throws IOException {
		HashMap<String, Integer> wordCount = new HashMap<>();
		try(ZipFile file = new ZipFile(filename)){
			Enumeration<? extends ZipEntry> zipEntries = file.entries();
			
			while(zipEntries.hasMoreElements()){
				ZipEntry entry = zipEntries.nextElement();
				InputStream fileInputStream = file.getInputStream(entry);
				String[] wordSetMail = countWords(fileInputStream);
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
	protected String[] countWords(InputStream fileInputStream)
			throws UnsupportedEncodingException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"));
		
		Set<String> wordSet = new HashSet<>();
		String line = "";
		int newLines = 0; //E-Mail Body starts with \n\n
		Boolean bodyReached = false;
		while ((line = reader.readLine()) != null) {
			if(bodyReached){
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
				if(newLines == 1){
					bodyReached = true;
				}
			}else{
				newLines = 0;
			}
			
			
		}
		return wordSet.toArray(new String[wordSet.size()]);
	}
	
	

}
