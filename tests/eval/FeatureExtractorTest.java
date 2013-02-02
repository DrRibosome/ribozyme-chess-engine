package eval;

import static org.junit.Assert.*;

import org.junit.Test;

import uci.FenParser;
import uci.Position;
import util.board4.State4;

public class FeatureExtractorTest {

	@Test
	public void testProcessMobility(){
		class TestCase{
			String fen;
			int[] mobility;
			
			public TestCase(String fen, int[] mobility) {
				this.fen = fen;
				this.mobility = mobility;
			}
		}
		final TestCase[] tests = new TestCase[]{
				new TestCase("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", 
						new int[]{4, 0, 0, 0, 0, 4, 0}),
				new TestCase("8/8/Q4p1p/3p1K2/5Q2/4k2P/6P1/8 w - - - -", 
						new int[]{38, 0, 38, 0, 0, 0, 0})
		};
		
		FeatureExtractor.FeatureSet fset = new FeatureExtractor.FeatureSet();
		for(int a = 0; a < tests.length; a++){
			Position p = FenParser.parse(tests[a].fen);
			final int player = p.sideToMove;
			FeatureExtractor.processMobility(fset, player, p.s);

			for(int q = 0; q < 7; q++){
				//System.out.println(tests[a].mobility[q]+", "+fset.mobility[q]+", q="+q);
				assertEquals(tests[a].mobility[q], fset.mobility[q]);
			}
		}
	}
	
	@Test
	public void testProcessPawns() {
		class TestCase{
			String fen;
			int numPassedPawns;
			int numUnopposedPawns;
			
			public TestCase(String fen, int numPassedPawns, int numUnopposedPawns) {
				this.fen = fen;
				this.numPassedPawns = numPassedPawns;
				this.numUnopposedPawns = numUnopposedPawns;
			}
		}
		final TestCase[] tests = new TestCase[]{
				new TestCase("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", 0, 0),
				new TestCase("8/8/Q4p1p/3p1K2/5Q2/4k2P/6P1/8 b - - - -", 1, 1),
		};
		
		FeatureExtractor.FeatureSet fset = new FeatureExtractor.FeatureSet();
		for(int a = 0; a < tests.length; a++){
			Position p = FenParser.parse(tests[a].fen);
			final int player = p.sideToMove;
			System.out.println("player = "+player);
			FeatureExtractor.processPawns(fset, player, p.s);
			
			int passedPawns = 0;
			int unopposedPawns = 0;
			for(int q = 0; q < p.s.pieceCounts[player][State4.PIECE_TYPE_PAWN]; q++){
				passedPawns += fset.pawnPassed[q];
				unopposedPawns += fset.pawnUnopposed[q];
			}
			//System.out.println("passedPawns = "+passedPawns+", exp = "+tests[a].numPassedPawns);
			assertEquals(passedPawns, tests[a].numPassedPawns);
			assertEquals(unopposedPawns, tests[a].numUnopposedPawns);
		}
	}

}
