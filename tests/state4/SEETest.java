package state4;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uci.Position;
import util.FenParser;

public class SEETest {

	@Test
	public void testSeeSign() {
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
				final Position p = FenParser.parse(fen);
				return SEE.seeSign(player, 1L << from, 1L << to, p.s) == expected;
			}
		}
		
		final List<TestCase> l = new ArrayList<>();
		l.add(new TestCase("1k1r4/1pp4p/p7/4p3/8/P5P1/1PP4P/2K1R3 w - - - -", 0, 4, 36, 1));
		l.add(new TestCase("1k1r4/1pp4p/p7/4p3/8/P5P1/1PP4P/2K1R3 w - - - -", 0, 10, 18, 0));
		l.add(new TestCase("1k1r3q/1ppn3p/p4b2/4p3/8/P2N2P1/1PP1R1BP/2K1Q3 w - - - -", 0, 19, 36, -1));
		l.add(new TestCase("1k1r3q/1ppn3p/p4b2/4p3/8/P2N2P1/1PP1R1BP/2K1Q3 w - - - -", 0, 19, 29, -1));
		l.add(new TestCase("1k1r3q/1ppn3p/p4b2/4p3/5n2/P2N4/1PP1R1BP/2K1Q3 w - - - -", 0, 19, 29, 0));
		
		for(TestCase t: l){
			assertTrue(t.test());
		}
	}

}
