package search.windowTrainer;

import java.io.File;
import java.io.IOException;

public class WindowTest2 {
	public static void main(String[] args) throws IOException{
		RecordLoader.Record[] r = RecordLoader.loadRecords(new File("search25.stats"));
		
		int error = 0;
		int prev = 3;
		double margin = 35;
		int tests = 0;
		for(int a = 0; a < r.length; a++){
			
			for(int i = 2; i < r[a].scoreCount-1; i++){
				double avg = 0;
				for(int q = 0; q < prev; q++){
					avg += r[a].scores[i-q];
				}
				avg /= prev;
				double target = r[a].scores[i+1];
				
				error += Math.abs(avg-target) < margin? 0: 1;
				tests++;
			}
		}
		
		System.out.println("error = "+error+", tests = "+tests);
		System.out.println(1-error*1./tests);
	}
}
