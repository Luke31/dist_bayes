package ch.fhnw.dist.bayes.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Calculates the spam probability of a given Mail-String.
 * Learns using given Maps of Spam-and Ham-Words.
 * Help for implementation: http://www.math.kit.edu/ianm4/~ritterbusch/seite/spam/de
 * @author Lukas Schmid
 */
public class SpamProbabilityCalculator {	
	private final static double NEUT_ELEM_MULT = 1;
	private final static double LOW_COUNT_ALPHA = 0.5; //Gleich wie P(S) und P(S)
	
	//Counts
	private Integer totalHamMails;
	private Map<String, Double> hamWords;
	private Integer totalSpamMails;
	private Map<String, Double> spamWords;
	
	//Probabilities
	private Map<String, Double> hamProbability; //P("word" | H)
	private Map<String, Double> spamProbability; //P("word" | S)
	
	public SpamProbabilityCalculator(Integer totalHamMails, Map<String, Integer> hamWords, Integer totalSpamMails,
			Map<String, Integer> spamWords) {
		super();
		this.totalHamMails = totalHamMails;
		this.totalSpamMails = totalSpamMails;
		
		addWordsInclViceVersa(hamWords, spamWords);
		learnWords();
	}
	
	/**
	 * Adds words with count.
	 * Add missing words to spam from ham and vice versa with very low count.
	 * @param intHamWords
	 * @param intSpamWords
	 */
	private void addWordsInclViceVersa(Map<String, Integer> intHamWords, Map<String, Integer> intSpamWords){
		this.hamWords = new HashMap<>();
		this.spamWords = new HashMap<>();
		for(Entry<String, Integer> e : intHamWords.entrySet()){
			hamWords.put(e.getKey(), (double)e.getValue());
			if(!intSpamWords.containsKey(e.getKey())){
				spamWords.put(e.getKey(), LOW_COUNT_ALPHA);
			}
		}
		for(Entry<String, Integer> e : intSpamWords.entrySet()){
			spamWords.put(e.getKey(), (double)e.getValue());
			if(!intHamWords.containsKey(e.getKey())){
				hamWords.put(e.getKey(), LOW_COUNT_ALPHA);
			}
		}
	}

	/**
	 * Converts the given word-counts to probabilities in maps
	 */
	private void learnWords(){
		hamProbability = new HashMap<>();
		spamProbability = new HashMap<>();
		
		//Initialize Probablities
		for(Entry<String, Double> e : hamWords.entrySet()){
			hamProbability.put(e.getKey(), (double)e.getValue() / totalHamMails);
		}
		for(Entry<String, Double> e : spamWords.entrySet()){
			spamProbability.put(e.getKey(), (double)e.getValue() / totalSpamMails);
		}
	}
	
	/**
	 * Calculates the spam-probability for the given mail-string
	 * @param mail
	 * @return spam-probability 0 <= probability <= 1 
	 */
	public double calculateSpamProbability(String mail){		
		String[] words = mail.split("\\s+");
		
		return calculateSpamProbOfWords(words);
		//TODO: learn as new word
	}
	
	/**
	 * Calculate Spam probability of all given words:
	 * P(S | words[0] ∩ ... ∩ words[n])
	 * @param words
	 * @return P(S | WORDS) 0 <= ret <= 1
	 */	
	private double calculateSpamProbOfWords(String[] words){
		double spamProb = NEUT_ELEM_MULT; //P(words[0] | S) * ... * P(words[n] | S)
		double hamProb = NEUT_ELEM_MULT; //P(words[0] | H) * ... * P(words[n] | H)
		for(String w : words){
			if(!spamProbability.containsKey(w)){ 
				continue; //Ignore this element, neither list contains it -> If spam doesn't contain it, neither does ham
			}
			spamProb *= spamProbability.get(w);
			hamProb *= hamProbability.get(w);
		}
		return spamProb / (spamProb + hamProb); //Divided by total probability
	}

	/**
	 * @return P("word",H)
	 */
	public Map<String, Double> getHamProbability() {
		return hamProbability;
	}

	/**
	 * @return P("word",S)
	 */
	public Map<String, Double> getSpamProbability() {
		return spamProbability;
	}
	
}

