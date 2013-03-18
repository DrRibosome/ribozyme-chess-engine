package util.opening2;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import state4.State4;
import util.pgnLoader.PgnGame;
import util.pgnLoader.PgnLoader;
import util.pgnLoader.PgnProcessor;

public class BookGenerator {
	public static void main(String[] args) throws IOException{
		
		
		final Map<Long, Integer> m = new HashMap<>();
		final long seed = 43388L;
		PgnProcessor proc = new PgnProcessor() {
			public void process(PgnGame g) {
				
				final State4 s = new State4(seed);
				s.initialize();
				int[][] moves = g.moves;
				for(int a = 0; a < moves.length; a++){
					s.executeMove(a%2, 1L<<moves[a][0], 1L<<moves[a][1]);
					s.resetHistory();
					inc(m, s.zkey());
				}
				
			}
		};
		final File pgn = new File("sample.pgn");
		
		System.out.println("loading...");
		PgnLoader.load(pgn, proc);
		System.out.println("loading complete!");
		

		final File out = new File("megabook.bk");
		final DataOutputStream dos = new DataOutputStream(new FileOutputStream(out));
		dos.writeLong(seed);
		
		int totalPos = 0;
		int maxAppearances = 0;
		for(long key: m.keySet()){
			final int appearances = m.get(key);
			totalPos++;
			if(appearances > maxAppearances){
				maxAppearances = appearances;
			}
			
			dos.writeLong(key);
			dos.writeShort(totalPos);
		}
		dos.close();
		System.out.println(totalPos+" positions");
		System.out.println("max appearances = "+maxAppearances);
	}
	
	/** increment key entry*/
	private static void inc(Map<Long, Integer> m, long key){
		if(!m.containsKey(key)){
			m.put(key, 0);
		}
		m.put(key, m.get(key)+1);
	}
}
