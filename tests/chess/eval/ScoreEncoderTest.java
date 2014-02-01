package chess.eval;

import java.util.ArrayList;
import java.util.List;

import chess.state4.Masks;
import junit.framework.Assert;

import org.junit.Test;

public class ScoreEncoderTest {

	@Test
	public void testEncode() {
		class TestCase{
			final int score;
			final int lowerMargin;
			final int upperMargin;
			final int flag;

			TestCase(int score, int lowerMargin, int upperMargin, int flag) {
				this.score = score;
				this.lowerMargin = lowerMargin;
				this.upperMargin = upperMargin;
				this.flag = flag;
			}

			public boolean test(){
				final long scoreEncoding = ScoreEncoder.encode(score, lowerMargin, upperMargin, flag);
				/*System.out.println("testing "+score+", "+lowerMargin+", "+upperMargin+", "+flag);
				System.out.println("\tcalculated = "+
						ScoreEncoder.getScore(scoreEncoding)+", "+
						ScoreEncoder.getLowerMargin(scoreEncoding)+", "+
						ScoreEncoder.getUpperMargin(scoreEncoding)+", "+
						ScoreEncoder.getFlag(scoreEncoding));
				System.out.println(Masks.getString(scoreEncoding));*/

				return ScoreEncoder.getScore(scoreEncoding) == score &&
						ScoreEncoder.getLowerMargin(scoreEncoding) == lowerMargin &&
						ScoreEncoder.getUpperMargin(scoreEncoding) == upperMargin &&
						ScoreEncoder.getFlag(scoreEncoding) == flag;
			}
		}
		
		List<TestCase> l = new ArrayList<>();
		l.add(new TestCase(500, 1, 2, 7));
		l.add(new TestCase(500, 90, 200, 3));
		l.add(new TestCase(500, 500, -30, 2));
		l.add(new TestCase(500, 80, 0, 1));
		l.add(new TestCase(500, 300, -800, 5));
		l.add(new TestCase(-500, 0, 135, 0));
		l.add(new TestCase(-382, 200, 5, 2));
		l.add(new TestCase(4000, 80, -30, 1));
		l.add(new TestCase(-5000, -300, 500, 5));
		l.add(new TestCase(0, 0, 0, 0));
		l.add(new TestCase(500, -100, 19, 5));
		l.add(new TestCase(-500, 0, 1, 0));
		l.add(new TestCase(-382, -50, 35, 2));
		l.add(new TestCase(4000, -80, -888, 1));
		l.add(new TestCase(-5000, -30, 98, 5));
		l.add(new TestCase(5832, 0, 67, 0));
		
		for(TestCase testCase: l){
			//System.out.println("expected results = "+testCase.score+", "+testCase.margin+", "+testCase.flags);
			Assert.assertTrue(testCase.test());
		}
	}

}
