package tests.windowTrainer;

import java.io.File;
import java.io.IOException;

public class WindowTest3 {
	public static void main(String[] args) throws IOException{
		RecordLoader.Record[] r = RecordLoader.loadRecords(new File("search27.stats"));
		
		int error = 0;
		int prev = 2;
		double margin = 100; //100 for 95%
		int tests = 0;
		for(int a = 0; a < r.length; a++){

			for(int i = 2; i < r[a].scoreCount-1; i++){
				
				double dir = 0; //direction of travel
				/*for(int q = i-3>0? i-3: 1; q <= i; q++){
					dir += Math.pow(.3, i-q)*(r[a].scores[q]-r[a].scores[q-1]);
				}*/
				dir = r[a].scores[i]-r[a].scores[i-1];
				
				double avg = 0;
				for(int q = 0; q < prev; q++){
					avg += r[a].scores[i-q];
				}
				avg /= prev;
				avg += dir/2;
				
				double target = r[a].scores[i+1];
				error += Math.abs(avg-target) < margin? 0: 1;
				tests++;
			}
		}
		
		System.out.println("error = "+error+", tests = "+tests);
		System.out.println(1-error*1./tests);
	}
}
