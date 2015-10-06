package ch.fhnw.dist.bayes.filter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Calculates the spam probability of a given Mail-String.
 * Learns using given Maps of Spam-and Ham-Words.
 * Help for implementation: http://www.math.kit.edu/ianm4/~ritterbusch/seite/spam/de
 * @author Lukas Schmid
 */
public class SpamProbabilityCalculator {	
	private final static int NEUT_ELEM_MULT = 1;
	private final static double LOW_COUNT_ALPHA = 0.00001; //Gleich wie P(S) und P(S)
	public final static double SCHWELLENWERT = 0.5; //P(S) und P(S)
	private final static MathContext MATHCONTEXT = new MathContext(100);
	
	//Counts
	private BigDecimal totalHamMails; //1151 anlern
	private Map<String, BigDecimal> hamWords;
	private BigDecimal totalSpamMails; //249 anlern
	private Map<String, BigDecimal> spamWords;
	
	//Probabilities
	private Map<String, BigDecimal> hamProbability; //P("word" | H)
	private Map<String, BigDecimal> spamProbability; //P("word" | S)
	
	public SpamProbabilityCalculator(Integer totalHamMails, Map<String, Integer> hamWords, Integer totalSpamMails,
			Map<String, Integer> spamWords) {
		super();
		this.totalHamMails = new BigDecimal(totalHamMails);
		this.totalSpamMails = new BigDecimal(totalSpamMails);
		
		this.hamWords = new HashMap<>();
		this.spamWords = new HashMap<>();
		
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
		
		for(Entry<String, Integer> e : intHamWords.entrySet()){
			if(!hamWords.containsKey(e.getKey())){
				hamWords.put(e.getKey(), new BigDecimal(e.getValue()));
			}else{ //Add count to existing
				hamWords.put(e.getKey(), hamWords.get(e.getKey()).add(new BigDecimal(e.getValue())));
			}
				
			if(!intSpamWords.containsKey(e.getKey())){
				spamWords.put(e.getKey(), new BigDecimal(LOW_COUNT_ALPHA));
			}
		}
		for(Entry<String, Integer> e : intSpamWords.entrySet()){
			if(!spamWords.containsKey(e.getKey())){
				spamWords.put(e.getKey(), new BigDecimal(e.getValue()));
			}else{ //Add count to existing
				spamWords.put(e.getKey(), spamWords.get(e.getKey()).add(new BigDecimal(e.getValue())));
			}
			if(!intHamWords.containsKey(e.getKey())){
				hamWords.put(e.getKey(), new BigDecimal(LOW_COUNT_ALPHA));
			}
		}
	}
	
	public void learnFromNewMail(Set<String> words, Boolean spam){
		HashMap<String, Integer> intHamWords = new HashMap<>();
		HashMap<String, Integer> intSpamWords = new HashMap<>();
		if(spam){ //Spam
			totalSpamMails.add(new BigDecimal(1));
			for(String w : words){
				intSpamWords.put(w, 1);
			}
		}else{ //Ham
			totalHamMails.add(new BigDecimal(1));
			for(String w : words){
				intHamWords.put(w, 1);
			}
		}
		addWordsInclViceVersa(intHamWords, intSpamWords);
		learnWords();
	}

	/**
	 * Converts the given word-counts to probabilities in maps
	 */
	private void learnWords(){
		hamProbability = new HashMap<>();
		spamProbability = new HashMap<>();
		
		//Initialize Probablities
		for(Entry<String, BigDecimal> e : hamWords.entrySet()){
			hamProbability.put(e.getKey(), e.getValue().divide(totalHamMails, MATHCONTEXT));
		}
		for(Entry<String, BigDecimal> e : spamWords.entrySet()){
			spamProbability.put(e.getKey(), e.getValue().divide(totalSpamMails, MATHCONTEXT));
		}
	}
	
//	public void learnNewWords()
	
	/**
	 * Calculates the spam-probability for the given mail-string
	 * @param mail
	 * @return spam-probability 0 <= probability <= 1 
	 */
	public double calculateSpamProbability(String[] mailWords){		
		return calculateSpamProbOfWords(mailWords);
	}
	
	public boolean isSpam(String[] mailWords){
		return calculateSpamProbability(mailWords) > SCHWELLENWERT;
	}
	
	/**
	 * Calculate Spam probability of all given words:
	 * P(S | words[0] ∩ ... ∩ words[n])
	 * @param words
	 * @return P(S | WORDS) 0 <= ret <= 1
	 */	
	private double calculateSpamProbOfWords(String[] words){
		BigDecimal spamProb = new BigDecimal(NEUT_ELEM_MULT); //P(words[0] | S) * ... * P(words[n] | S)
		BigDecimal hamProb = new BigDecimal(NEUT_ELEM_MULT); //P(words[0] | H) * ... * P(words[n] | H)
		for(String w : words){
			if(!spamProbability.containsKey(w)){ 
				continue; //Ignore this element, neither list contains it -> If spam doesn't contain it, neither does ham
			}
			spamProb = spamProb.multiply(spamProbability.get(w));
			hamProb = hamProb.multiply(hamProbability.get(w));
		}
		BigDecimal result = spamProb.divide(spamProb.add(hamProb), MATHCONTEXT); 
		return result.round(new MathContext(10)).doubleValue(); //Divided by total probability
	}

	/**
	 * @return P("word",H)
	 */
	public Map<String, BigDecimal> getHamProbability() {
		return hamProbability;
	}

	/**
	 * @return P("word",S)
	 */
	public Map<String, BigDecimal> getSpamProbability() {
		return spamProbability;
	}
	
//	private boolean isValidWord(String w){
//		if(w == null || w.trim().isEmpty()){
//			return false;
//		}else{
//			return true;
//		}
//	}
}

