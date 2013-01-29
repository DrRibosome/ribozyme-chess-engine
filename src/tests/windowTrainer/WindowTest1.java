package tests.windowTrainer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eval.testEval2.EvalNetwork;

public class WindowTest1 {
	static class Record{
		int turn;
		int[][] pieceCounts = new int[2][7];
		double[] scores = new double[128];
		int scoreCount;
		
		Record(String line){
			//System.out.println("line = "+line);
			Pattern pieceCountsSel = Pattern.compile("<piece-counts-(\\d)=(.*?)>");
			Matcher m = pieceCountsSel.matcher(line);
			while(m.find()){
				int player = Integer.parseInt(m.group(1));
				String[] counts = m.group(2).split(",");
				for(int a = 0; a < counts.length; a++){
					pieceCounts[player][a] = Integer.parseInt(counts[a]);
				}
			}
			

			Pattern turnSel = Pattern.compile("<turn=(\\d)>");
			m = turnSel.matcher(line);
			m.find();
			turn = Integer.parseInt(m.group(1));
			
			Pattern scoreSel = Pattern.compile("<scores=(.*?)>");
			m = scoreSel.matcher(line);
			m.find();
			String[] s = m.group(1).split(",");
			scoreCount = s.length;
			for(int a = 0; a < s.length; a++){
				scores[a] = Double.parseDouble(s[a]);
			}
		}
		
		public void train(double[] input, EvalNetwork n, int prev){
			/*for(int i = 0, turn = this.turn; i < 2; i++, turn = 1-turn){
				for(int a = 1; a < 7; a++){
					input[a+7*i-1] = pieceCounts[turn][a];
				}
			}
			int initialOffset = 12;*/
			int initialOffset = 0;
			
			for(int a = 0; a < scoreCount-prev; a++){
				for(int q = 0; q < prev; q++){
					input[initialOffset+q] = scores[a+q];
				}
				double target = scores[a+prev+1];
				
				n.train(n.derrivative(target), .01);
			}
		}
		
		public double error(double[] input, EvalNetwork n, int prev){
			/*for(int i = 0, turn = this.turn; i < 2; i++, turn = 1-turn){
				for(int a = 1; a < 7; a++){
					input[a+7*i-1] = pieceCounts[turn][a];
				}
			}
			int initialOffset = 12;*/
			int initialOffset = 0;
			
			double error = 0;
			for(int a = 0; a < scoreCount-prev; a++){
				for(int q = 0; q < prev; q++){
					input[initialOffset+q] = scores[a+q];
				}
				
				
				double target = scores[a+prev+1];
				
				System.out.println(n.eval()+", exp = "+target);
				error += Math.sqrt(Math.pow(n.eval()-target, 2)) < 150? 0 : 1;
			}
			return error;
		}
	}
	public static void main(String[] args) throws IOException{
		int prevRecordsIncluded = 3;
		double[] input = new double[prevRecordsIncluded+1];
		input[input.length-1] = 1;
		EvalNetwork n = new EvalNetwork(input, 100);
		n.initializeWeights();
		Record[] r = loadRecords();
		for(int a = 0; a < r.length; a++){
			r[a].train(input, n, prevRecordsIncluded);
		}
		
		double error = 0;
		int tests = 0;
		for(int a = 0; a < r.length; a++){
			error += r[a].error(input, n, prevRecordsIncluded);
			tests += r[a].scoreCount-prevRecordsIncluded;
		}
		System.out.println("error = "+error);
		/*error = error/r.length/2; //average squared error
		System.out.println("avg sqrd error = "+error);*/
		System.out.println("tests = "+tests);
		System.out.println((1-error/tests)+" % correct");
	}
	
	public static Record[] loadRecords() throws IOException{
		Scanner s = new Scanner(new File("search25.stats"));
		List<Record> l = new ArrayList<Record>();
		while(s.hasNextLine()){
			l.add(new Record(s.nextLine()));
		}
		return l.toArray(new Record[l.size()]);
	}
}
