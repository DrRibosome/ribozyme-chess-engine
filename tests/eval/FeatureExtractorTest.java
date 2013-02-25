package eval;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import state4.State4;
import uci.FenParser;
import uci.Position;
import eval.expEvalV1.FeatureExtractor;

public class FeatureExtractorTest {

	@Test
	public void testMobility(){
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
				assertEquals(tests[a].mobility[q], fset.mobility[q]);
			}
		}
	}
	
	@Test
	public void testUnopposedPawns() {
		class TestCase{
			String fen;
			int numUnopposedPawns;
			public TestCase(String fen, int numUnopposedPawns) {
				this.fen = fen;
				this.numUnopposedPawns = numUnopposedPawns;
			}
		}
		final TestCase[] tests = new TestCase[]{
				new TestCase("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", 0),
				new TestCase("8/8/Q4p1p/3p1K2/5Q2/4k2P/6P1/8 b - - - -", 1),
		};
		
		FeatureExtractor.FeatureSet fset = new FeatureExtractor.FeatureSet();
		for(int a = 0; a < tests.length; a++){
			Position p = FenParser.parse(tests[a].fen);
			final int player = p.sideToMove;
			FeatureExtractor.processPawns(fset, player, p.s);
			
			int unopposedPawns = 0;
			for(int q = 0; q < p.s.pieceCounts[player][State4.PIECE_TYPE_PAWN]; q++){
				unopposedPawns += fset.pawnUnopposed[q];
			}
			assertEquals(unopposedPawns, tests[a].numUnopposedPawns);
		}
	}
	
	@Test
	public void testPassedPawns() {
		class TestCase{
			String fen;
			int numPassedPawns;
			public TestCase(String fen, int numPassedPawns) {
				this.fen = fen;
				this.numPassedPawns = numPassedPawns;
			}
		}
		final TestCase[] tests = new TestCase[]{
				new TestCase("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", 0),
				new TestCase("8/8/Q4p1p/3p1K2/5Q2/4k2P/6P1/8 b - - - -", 1),
				new TestCase("3r2k1/2RP1pbp/4pqp1/1B6/2P2P2/4Q3/3K2PP/7R w - - - -", 2),
		};
		
		FeatureExtractor.FeatureSet fset = new FeatureExtractor.FeatureSet();
		for(int a = 0; a < tests.length; a++){
			Position p = FenParser.parse(tests[a].fen);
			final int player = p.sideToMove;
			FeatureExtractor.processPawns(fset, player, p.s);
			
			int passedPawns = 0;
			for(int q = 0; q < p.s.pieceCounts[player][State4.PIECE_TYPE_PAWN]; q++){
				passedPawns += fset.pawnPassed[q];
			}
			assertEquals(passedPawns, tests[a].numPassedPawns);
		}
	}

}
