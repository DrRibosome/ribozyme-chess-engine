package util.analysis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import state4.State4;
import state4.StateUtil;
import util.pgnLoader.PgnGame;
import util.pgnLoader.PgnLoader;
import util.pgnLoader.PgnProcessor;

/** 
 * runs through a collection of pgn files and samples positions from the game.
 * Idea is to build a sample set of usual positions to see how things like search
 * perform
 * 
 * <p> NOTE: THIS MUST BE RUN WITH ASSERTIONS TO WORK IF PGN FILES CONTAIN BAD GAMES!
 */
public final class GameSampler {
	public static void main(String[] args) throws IOException{
		
		//NOTE: THIS MUST BE RUN WITH ASSERTIONS TO WORK IF PGN FILES CONTAIN BAD GAMES!

		final File out = new File("temp-sample.fen");
		if(out.exists()){
			System.err.println("file \""+out+"\" exists already, aborting");
		}
		
		final PrintStream dos = new PrintStream(new FileOutputStream(out));
		
		final int skipMoves = 8; //skip initial X moves of game
		final double keepChance = .001; // record a sample from a game with probabilty specified
		
		
		final PgnProcessor proc = new PgnProcessor() {
			int count = 0;
			public void process(PgnGame g) {
				
				if(Math.random() < keepChance && g.moves.length > skipMoves){
					final State4 s = new State4();
					s.initialize();
					int[][] moves = g.moves;
					final int len = (int)(Math.random()*(moves.length-skipMoves)) + skipMoves;
					
					for(int a = 0; a < len; a++){
						s.executeMove(a%2, 1L<<moves[a][0], 1L<<moves[a][1]);
						s.resetHistory();
					}
					
					dos.println(StateUtil.fen((len+1)%2, s));
				}
				
				if(++count % 5000 == 0){
					System.out.println("processed "+count);
				}
			}
		};
		//final File pgn = new File("sample3.pgn");
		final File pgn = new File("millionbase-2.22.pgn");
		
		System.out.println("loading...");
		final int badEntries = PgnLoader.load(pgn, proc);
		dos.close();
		System.out.println("bad entries = "+badEntries);
		System.out.println("loading complete!");
	}
}
