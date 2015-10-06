package ch.fhnw.dist.bayes.filter;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestSpamProbabilityCalculator {
	
	Map<String, Integer> hamWords = new HashMap<>();
	Map<String, Integer> spamWords = new HashMap<>();
	SpamProbabilityCalculator calc;
	
	@Before
    public void setUp() {
		hamWords.put("haben", 30);
		hamWords.put("online", 3);
		spamWords.put("haben", 7);
		spamWords.put("online", 8);
		calc = new SpamProbabilityCalculator(100, hamWords, 100, spamWords);
    }
	
	@After
    public void tearDown() {
		hamWords.clear();
		spamWords.clear();
    }
	
	@Test
	public void testLearnWords(){
		Map<String, BigDecimal> hamProb = calc.getHamProbability();
		Map<String, BigDecimal> spamProb = calc.getSpamProbability();
		assertEquals(0.3, hamProb.get("haben").doubleValue(), 0);
		assertEquals(0.03, hamProb.get("online").doubleValue(), 0);
		assertEquals(0.07, spamProb.get("haben").doubleValue(), 0);
		assertEquals(0.08, spamProb.get("online").doubleValue(), 0);
	}
	
	@Test
	public void testSpamCalculateProbability(){
		String[] mailWords = new String[]{"haben","online"};
		double prob = calc.calculateSpamProbability(mailWords);
		assertEquals(0.38, prob, 0.01);
	}
}

