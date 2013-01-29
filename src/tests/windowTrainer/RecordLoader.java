package tests.windowTrainer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eval.testEval2.EvalNetwork;

public final class RecordLoader {
	public static class Record{
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
	}
	
	public static Record[] loadRecords(File log) throws IOException{
		Scanner s = new Scanner(log);
		List<Record> l = new ArrayList<Record>();
		while(s.hasNextLine()){
			l.add(new Record(s.nextLine()));
		}
		s.close();
		return l.toArray(new Record[l.size()]);
	}
}
