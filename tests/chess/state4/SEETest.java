package chess.state4;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import chess.uci.Position;
import chess.util.FenParser;

public class SEETest {

	@Test
	public void testSEESign() {
		class TestCase{
			final String fen;
			final int player;
			final int from;
			final int to;
			final int expected;
			TestCase(String fen, int player, int from, int to, int expected){
				this.fen = fen;
				this.player = player;
				this.from = from;
				this.to = to;
				this.expected = expected;
			}
			boolean test(){
				final Position p = new Position();
				FenParser.parse(fen, p);
				//System.out.println(p.s);
				//System.out.println(from+" -> "+to+" = "+SEE.see(player, 1L << from, 1L << to, p.s));
				//System.out.println("sign = "+SEE.seeSign(player, 1L << from, 1L << to, p.s)+"\n");
				return SEE.seeSign(player, 1L << from, 1L << to, p.s) == expected;
			}
		}
		
		final List<TestCase> l = new ArrayList<>();
		l.add(new TestCase("1k1r4/1pp4p/p7/4p3/8/P5P1/1PP4P/2K1R3 w - - - -", 0, 4, 36, 1));
		l.add(new TestCase("1k1r4/1pp4p/p7/4p3/8/P5P1/1PP4P/2K1R3 w - - - -", 0, 10, 18, 0));
		l.add(new TestCase("1k1r3q/1ppn3p/p4b2/4p3/8/P2N2P1/1PP1R1BP/2K1Q3 w - - - -", 0, 19, 36, -1));
		l.add(new TestCase("1k1r3q/1ppn3p/p4b2/4p3/8/P2N2P1/1PP1R1BP/2K1Q3 w - - - -", 0, 19, 29, -1));
		l.add(new TestCase("5rk1/p6p/1p4pQ/2pqbp2/4p3/P1P1P3/1PR2PPP/2RrB1K1 w - d6 0 28", 1, 3, 2, 0));
		l.add(new TestCase("1r2r2k/p1b2pp1/Q1p5/2P5/P2Pp2p/4BqP1/R4P1P/5RK1 w - - 0 24", 0, 40, 58, -1));
		l.add(new TestCase("3r2k1/1pb1qppp/2p1r3/p7/P1Q5/1PPbPB1P/3BKPP1/R2R4 w - - 0 21", 1, 19, 26, 1));
		l.add(new TestCase("3r2k1/1pb1qppp/2p1r3/p7/P1Q5/1PPbPB1P/3BKPP1/R2R4 w - - 0 21", 0, 26, 42, -1));
		
		for(TestCase t: l){
			assertTrue(t.test());
		}
	}
	
	@Test
	public void testSEE() {
		class TestCase{
			final String fen;
			final int player;
			final int from;
			final int to;
			final int expected;
			TestCase(String fen, int player, int from, int to, int expected){
				this.fen = fen;
				this.player = player;
				this.from = from;
				this.to = to;
				this.expected = expected;
			}
			boolean test(){
				final Position p = new Position();
				FenParser.parse(fen, p);
				return SEE.see(player, 1L << from, 1L << to, p.s) == expected;
			}
		}
		
		final List<TestCase> l = new ArrayList<>();
		l.add(new TestCase("1k1r4/1pp4p/p7/4p3/8/P5P1/1PP4P/2K1R3 w - - - -", 0, 4, 36, 1));
		l.add(new TestCase("1k1r4/1pp4p/p7/4p3/8/P5P1/1PP4P/2K1R3 w - - - -", 0, 10, 18, 0));
		l.add(new TestCase("1k1r3q/1ppn3p/p4b2/4p3/8/P2N2P1/1PP1R1BP/2K1Q3 w - - - -", 0, 19, 36, -2));
		l.add(new TestCase("1k1r3q/1ppn3p/p4b2/4p3/8/P2N2P1/1PP1R1BP/2K1Q3 w - - - -", 0, 19, 29, -2));
		l.add(new TestCase("5rk1/p6p/1p4pQ/2pqbp2/4p3/P1P1P3/1PR2PPP/2RrB1K1 w - d6 0 28", 1, 3, 2, 0));
		l.add(new TestCase("1r2r2k/p1b2pp1/Q1p5/2P5/P2Pp2p/4BqP1/R4P1P/5RK1 w - - 0 24", 0, 40, 58, -9));
		l.add(new TestCase("3r2k1/1pb1qppp/2p1r3/p7/P1Q5/1PPbPB1P/3BKPP1/R2R4 w - - 0 21", 1, 19, 26, 1));
		l.add(new TestCase("3r2k1/1pb1qppp/2p1r3/p7/P1Q5/1PPbPB1P/3BKPP1/R2R4 w - - 0 21", 0, 26, 42, -8));
		
		for(TestCase t: l){
			assertTrue(t.test());
		}
	}

}
