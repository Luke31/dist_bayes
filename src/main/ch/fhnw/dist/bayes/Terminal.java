package ch.fhnw.dist.bayes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

import ch.fhnw.dist.bayes.filter.SpamProbabilityCalculator;

public class Terminal {
	private final static String LINE = "------------------------------------";
	
	public static void main(String[] args) throws IOException {
	    new Terminal().startTerminal();
	}
	
	SpamProbabilityCalculator calc;
	
	private void startTerminal() throws IOException{
		Bayes.train();
	    
	    calc = new SpamProbabilityCalculator(Bayes.hamMailCount, Bayes.hamWords, Bayes.spamMailCount, Bayes.spamWords);
	    
	    try(Scanner s = new Scanner(System.in)){
	    	while(true){
	    		System.out.println();
	    		System.out.println(LINE);
	    		System.out.println("Welcome to your spam-filter!\n"
	    				+ "Following commands are available:\n"
	    				+ "-checkmail PATH-TO-MAIL (e.g. checkmail C:/Users/lukas/Desktop/ham.txt)");
	    		handleCommand(s.nextLine().split("\\s+"), s);
	    	}
	    }
	}
	//checkmail C:/Users/lukas/Desktop/spam.txt
	//checkmail C:/Users/lukas/Desktop/ham.txt
	private void handleCommand(String[] in, Scanner s) throws IOException{
		String cmd = in[0];
		switch(cmd){
			case "calibrate": //c) Kalibrierung
				break;
			case "test": //d) Test
				
				break;
			case "checkmail": //e) Einzelnes mail
				File f = new File(in[1]);
				FileInputStream fis = new FileInputStream(f);
				Set<String> mailWords = Bayes.countWords(fis);
				String[] mailWordsArr = mailWords.toArray(new String[mailWords.size()]);
				double spamProb = calc.calculateSpamProbability(mailWordsArr);
				System.out.println("The provided E-Mail has been classified as "+
						(calc.isSpam(mailWordsArr)?"SPAM.":"HAM.") + " (Probability: "+ 
						spamProb +", SPAM-threshold: "+SpamProbabilityCalculator.SCHWELLENWERT +
						" - higher values than threshold are classified as spam)");
				System.out.println("Please decide if this E-Mail was SPAM(S) or HAM(H):");
				while(true){
					String chr = s.nextLine().trim().toLowerCase();
					if("s".equals(chr)){
						//SPAM
						calc.learnFromNewMail(mailWords, true);
						return;
					}else if("h".equals(chr)){
						//HAM
						calc.learnFromNewMail(mailWords, false);
						return;
					}else{
						System.out.println("Illegal command, allowed commands: S or H");
					}
				}
			case "exit":
				System.exit(0); //Terminate
				break;
			default:
				System.out.println("Illegal command");
				break;
		}
	}
	
	
}
