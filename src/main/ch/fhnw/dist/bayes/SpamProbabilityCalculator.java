package ch.fhnw.dist.bayes;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Calculates the spam probability of a given Mail-String.
 * Learns using given Maps of Spam-and Ham-Words.
 * Help for implementation: http://www.math.kit.edu/ianm4/~ritterbusch/seite/spam/de
 * @author Lukas Schmid, Andreas Gloor
 */
public class SpamProbabilityCalculator {	
	private final static int NEUT_ELEM_MULT = 1;
	private double schwellenwert = 0.5; //P(S) und P(S)
	private double alpha = 0.001; //Gleich wie P(S) und P(S)
	private final static MathContext MATHCONTEXT = new MathContext(100);
	
	//Counts
	private BigDecimal totalHamMails;
	private Map<String, BigDecimal> hamWords;
	private BigDecimal totalSpamMails;
	private Map<String, BigDecimal> spamWords;
	
	//Probabilities
	private Map<String, BigDecimal> hamProbability; //P("word" | H)
	private Map<String, BigDecimal> spamProbability; //P("word" | S)
	
	/**
	 * @param totalHamMails total ham-words to learn
	 * @param hamWords ham-words to learn
	 * @param totalSpamMails total spam-words to learn
	 * @param spamWords spam-words to learn
	 * @param schwellenwert Probabilities higher than this threshold are considered spam
	 * @param alpha Value used for vice-versa adding of unknown words
	 */
	public SpamProbabilityCalculator(Integer totalHamMails, Map<String, Integer> hamWords, Integer totalSpamMails,
			Map<String, Integer> spamWords, double schwellenwert, double alpha) {
		super();
		this.totalHamMails = new BigDecimal(totalHamMails);
		this.totalSpamMails = new BigDecimal(totalSpamMails);
		
		this.hamWords = new HashMap<>();
		this.spamWords = new HashMap<>();
		
		this.schwellenwert = schwellenwert;
		this.alpha = alpha;
		
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
				
			if(!intSpamWords.containsKey(e.getKey()) && !spamWords.containsKey(e.getKey())){
				spamWords.put(e.getKey(), new BigDecimal(alpha));
			}
		}
		for(Entry<String, Integer> e : intSpamWords.entrySet()){
			if(!spamWords.containsKey(e.getKey())){
				spamWords.put(e.getKey(), new BigDecimal(e.getValue()));
			}else{ //Add count to existing
				spamWords.put(e.getKey(), spamWords.get(e.getKey()).add(new BigDecimal(e.getValue())));
			}
			if(!intHamWords.containsKey(e.getKey()) && !hamWords.containsKey(e.getKey())){
				hamWords.put(e.getKey(), new BigDecimal(alpha));
			}
		}
	}
	
	/**
	 * Improve Filter-accuracy by providing words, which stem from a spam or ham mail.
	 * @param words Words occuring in provided mail
	 * @param spam Are provided words spam-words? (false is treated as ham)
	 */
	public void learnFromNewMail(String[] words, Boolean spam){
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
	
	/**
	 * Calculates the spam-probability for the given mail-string
	 * @param mail
	 * @return spam-probability 0 <= probability <= 1 
	 */
	public double calculateSpamProbability(String[] mailWords){		
		return calculateSpamProbOfWords(mailWords);
	}
	
	/**
	 * Determine if provided mail is spam depending on given threshold
	 * @param mailWords
	 * @return True if mail is considered as spam
	 */
	public boolean isSpam(String[] mailWords){
		return calculateSpamProbability(mailWords) > schwellenwert;
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

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public double getSchwellenwert() {
		return schwellenwert;
	}

	public void setSchwellenwert(double schwellenwert) {
		this.schwellenwert = schwellenwert;
	}
	
	
}

