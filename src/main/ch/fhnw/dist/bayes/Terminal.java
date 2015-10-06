package ch.fhnw.dist.bayes;

import java.io.IOException;
import java.util.Scanner;

public class Terminal {
	public static void main(String[] args) throws IOException {
	    Bayes.train();
//	    int totalHamMails = 0;
//	    int totalSpamMails = 0;
//	    SpamProbabilityCalculator calc = new SpamProbabilityCalculator(totalHamMails, Bayes.hamWords, totalSpamMails, Bayes.spamWords);
	    
	    
	    while(true){
	    	System.out.println("train, checkmail PATH");
	    	Scanner s = new Scanner(System.in);
	    	handleCommand(s.nextLine().split("\\s+"));
	    	System.out.println(s.nextInt());
	    }
	}
	
	private static void handleCommand(String[] in){
		String cmd = in[0];
		switch(cmd){
			case "train":
//				Bayes.train();
				break;
			case "checkmail":
				String path = in[1];
				System.out.println("Path: "+ path);
				break;
		}
	}
}
