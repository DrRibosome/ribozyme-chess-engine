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

public final class BookGenerator {
	private static final class Stat{
		int count;
		final int[] wins = new int[2];
	}
	public static void main(String[] args) throws IOException{
		
		//NOTE: THIS MUST BE RUN WITH ASSERTIONS TO WORK IF PGN FILES CONTAIN BAD GAMES!
		
		final Map<Long, Stat> m = new HashMap<>();
		
		final long seed = 43388L;
		PgnProcessor proc = new PgnProcessor() {
			int count = 0;
			public void process(PgnGame g) {
				
				final State4 s = new State4(seed);
				s.initialize();
				int[][] moves = g.moves;
				//System.out.println("processing "+moves.length+" moves");
				for(int a = 0; a < moves.length; a++){
					s.executeMove(a%2, 1L<<moves[a][0], 1L<<moves[a][1]);
					s.resetHistory();
					
					inc(m, s.zkey(), a%2, g.outcome == a%2);
				}
				
				if(++count % 5000 == 0){
					System.out.println("processed "+count+", positions = "+m.size());
				}
			}
		};
		//final File pgn = new File("sample3.pgn");
		final File pgn = new File("millionbase-2.22.pgn");
		
		System.out.println("loading...");
		int badEntries = PgnLoader.load(pgn, proc);
		System.out.println("bad entries = "+badEntries);
		System.out.println("loading complete!");
		

		final File out = new File("temp.bk");
		final DataOutputStream dos = new DataOutputStream(new FileOutputStream(out));
		dos.writeLong(seed);
		
		int maxAppearances = 0;
		for(long key: m.keySet()){
			final Stat s = m.get(key);
			if(s.count > maxAppearances){
				maxAppearances = s.count;
			}
			
			if(s.count >= 10){
				dos.writeLong(key);
				dos.writeShort(s.count);
				dos.writeShort(s.wins[0]);
				dos.writeShort(s.wins[1]);
			}
		}
		dos.close();
		System.out.println(m.size()+" positions");
		System.out.println("max appearances = "+maxAppearances);
	}
	
	/** increment key entry*/
	private static void inc(Map<Long, Stat> m, long key, int player, boolean win){
		if(!m.containsKey(key)){
			m.put(key, new Stat());
		}
		final Stat temp = m.get(key);
		temp.count++;
		if(win) temp.wins[player]++;
	}
}
