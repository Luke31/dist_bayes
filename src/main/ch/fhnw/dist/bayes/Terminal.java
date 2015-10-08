package ch.fhnw.dist.bayes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Start-class for the Bayes Spam-Probability calculator
 * @author Lukas Schmid, Andreas Gloor
 */
public class Terminal {
	private final static String LINE = "------------------------------------";
	
	public static void main(String[] args) throws IOException {
	    new Terminal().startTerminal();
	}
	
	SpamProbabilityCalculator calc;
	BayesFileHandler trainFiles = new BayesFileHandler();
	BayesFileHandler calibrateFiles = new BayesFileHandler();
	BayesFileHandler testFiles = new BayesFileHandler();
	
	private void startTerminal() throws IOException{
		trainFiles.loadTrainFiles(false);
	    
	    calc = new SpamProbabilityCalculator(trainFiles.hamMailCount, trainFiles.hamWords, 
	    		trainFiles.spamMailCount, trainFiles.spamWords, 0.9, 0.000001);
	    
	    try(Scanner s = new Scanner(System.in)){
	    	while(true){
	    		System.out.println();
	    		System.out.println(LINE);
	    		System.out.println("Welcome to your spam-filter!\n"
	    				+ "Default spam-threshold is 0.9 and alpha is 0.000001. You may change this using the calibrate command.\n"
	    				+ "Following commands are available:\n"
	    				+ "-calibrate SCHWELLENWERT ALPHA [PROGRESSIVELEARN=Y] (e.g. calibrate 0.9 0.000001 progressivelearn=y)  Aufgabe c)\n"
	    				+ "-test Aufgabe d)\n"
	    				+ "-checkmail PATH-TO-MAIL (e.g. checkmail C:/Users/test/Desktop/test.txt) Aufgabe e)\n"
	    				+ "-exit");
	    		handleCommand(s.nextLine().split("\\s+"), s);
	    	}
	    }
	}
	
	private void handleCommand(String[] in, Scanner s) throws IOException{
		String cmd = in[0];
		
		switch(cmd){
			case "calibrate": //c) Kalibrierung schwellenwert alpha
				calc = new SpamProbabilityCalculator(trainFiles.hamMailCount, trainFiles.hamWords, 
						trainFiles.spamMailCount, trainFiles.spamWords, Double.parseDouble(in[1]), Double.parseDouble(in[2]));
				boolean learn = false;
				if(in.length > 3 && in[3].toLowerCase().equals("progressivelearn=y")){
					learn = true;
				}
				calibrateFiles.loadCalibrationFiles();
				test(calibrateFiles, learn);
				break;
			case "test": //d) Test
				testFiles.loadTestFiles();
				test(testFiles, false);
				break;
			case "checkmail": //e) Einzelnes mail
				checkSingleMail(in[1], s);
				break;
			case "exit":
				System.exit(0); //Terminate
				break;
			default:
				System.out.println("Illegal command");
				break;
		}
	}

	
	
	private void test(BayesFileHandler fileHandler, boolean learn) throws IOException{
		int spamMailsOfHam = countSpamProbOfMails(fileHandler, fileHandler.hamZipPath, learn, false);
		int hamMailsOfHam = fileHandler.hamMailCount - spamMailsOfHam;
		int spamMailsOfSpam = countSpamProbOfMails(fileHandler, fileHandler.spamZipPath, learn, true);
		double percentCorrH = (double)hamMailsOfHam / fileHandler.hamMailCount * 100;
		double percentCorrS = (double)spamMailsOfSpam / fileHandler.spamMailCount * 100;
		
		DecimalFormat df = new DecimalFormat("#0.0000");
		
		System.out.println(hamMailsOfHam + " of " + fileHandler.hamMailCount + " HAM mails have been CORRECTLY classified as ham. Success-rate: " + df.format(percentCorrH)+"%");
		System.out.println(spamMailsOfSpam + " of " + fileHandler.spamMailCount + " SPAM mails have been CORRECTLY classified as spam. Success-rate: " + df.format(percentCorrS)+"%");
	}

	private int countSpamProbOfMails(BayesFileHandler fileHandler, String zipFilePath, boolean learn, boolean spam) throws IOException{
		int spamCount = 0;
		try(ZipFile file = new ZipFile(zipFilePath)){
			Enumeration<? extends ZipEntry> zipEntries = file.entries();
			
			while(zipEntries.hasMoreElements()){
				ZipEntry entry = zipEntries.nextElement();
				InputStream fis = file.getInputStream(entry);
				String[] mailWords = fileHandler.countWords(fis);
				if (calc.isSpam(mailWords)){
					spamCount++;
					if(learn){
						calc.learnFromNewMail(mailWords, spam);
					}
				}
			}
		}
		return spamCount;
	}
	
	private void checkSingleMail(String path, Scanner s)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {
		File f = new File(path);
		FileInputStream fis = new FileInputStream(f);
		String[] mailWords = trainFiles.countWords(fis);
		double spamProb = calc.calculateSpamProbability(mailWords);
		System.out.println("The provided E-Mail has been classified as "+
				(calc.isSpam(mailWords)?"SPAM.":"HAM.") + " (Probability: "+ 
				spamProb +", SPAM-threshold: "+calc.getSchwellenwert() +
				" - higher values than threshold are classified as spam) alpha: "+calc.getAlpha());
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
	}
}

