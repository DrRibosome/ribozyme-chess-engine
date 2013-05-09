package state4;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uci.Position;
import util.FenParser;

public class State4Test {

	@Test
	public void testGetCastleMoves() {
		class TestCase{
			final long exp;
			final Position p;
			TestCase(String fen, long exp){
				p = FenParser.parse(fen);
				this.exp = exp;
			}
		}
		
		final List<TestCase> l = new ArrayList<>();
		l.add(new TestCase("r1bk3r/ppp1nppp/2np1q2/1N6/2B1P3/2P1P3/PP4PP/RN1QK2R w KQ - - -", 0));
		
		for(TestCase c: l){
			final long castleMoves = State4.getCastleMoves(c.p.sideToMove, c.p.s);
			assertEquals(castleMoves, c.exp);
		}
	}
	
	@Test
	public void testIsAttacked2() {
		class TestCase{
			final boolean exp;
			final int loc; //location being attacked
			final Position p;
			TestCase(String fen, final int loc, boolean exp){
				p = FenParser.parse(fen);
				this.exp = exp;
				this.loc = loc;
			}
		}
		
		final List<TestCase> l = new ArrayList<>();
		l.add(new TestCase("r1bk3r/ppp1nppp/2np1q2/1N6/2B1P3/2P1P3/PP4PP/RN1QK2R b KQ - - -", 38, true));
		l.add(new TestCase("4rrk1/2q5/p1Ppbb2/1p2n2R/4P3/P1Q2PB1/3P2P1/2R1KBN1 b - - 0 30", 8, true));
		
		for(TestCase c: l){
			final boolean attacked = State4.isAttacked2(c.loc, c.p.sideToMove, c.p.s);
			//System.out.println(c.p.s);
			assertEquals(attacked, c.exp);
		}
	}

	@Test
	public void testIsForcedDraw() {
		class TestCase{
			final boolean exp;
			final Position p;
			TestCase(String fen, boolean exp){
				p = FenParser.parse(fen);
				this.exp = exp;
			}
		}
		
		final List<TestCase> l = new ArrayList<>();
		l.add(new TestCase("8/8/8/8/8/8/8/k6K b - - - -", true));
		
		for(TestCase c: l){
			final boolean draw = c.p.s.isForcedDraw();
			//System.out.println(c.p.s);
			assertEquals(draw, c.exp);
		}
	}
}
