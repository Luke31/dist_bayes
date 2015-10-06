package ch.fhnw.dist.bayes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

import ch.fhnw.dist.bayes.filter.SpamProbabilityCalculator;

public class Terminal {
	public static void main(String[] args) throws IOException {
	    new Terminal().startTerminal();
	}
	
	SpamProbabilityCalculator calc;
	
	private void startTerminal() throws IOException{
		Bayes.train();
	    
	    calc = new SpamProbabilityCalculator(Bayes.hamMailCount, Bayes.hamWords, Bayes.spamMailCount, Bayes.spamWords);
//	    boolean bla = Bayes.spamWords.containsKey("");
	    try(Scanner s = new Scanner(System.in)){
	    	while(true){
	    		System.out.println("checkmail PATH");
	    		handleCommand(s.nextLine().split("\\s+"));
	    	}
	    }
	}
	//checkmail C:/Users/lukas/Desktop/spam-single.zip
	//checkmail C:/Users/lukas/Desktop/ham-single.zip
	private void handleCommand(String[] in) throws IOException{
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
				System.out.println(calc.calculateSpamProbability(mailWordsArr));
				System.out.println(calc.isSpam(mailWordsArr));
				break;
			case "exit":
				System.exit(0); //Terminate
				break;
			default:
				System.out.println("Illegal command");
				break;
		}
	}
	
	
}
