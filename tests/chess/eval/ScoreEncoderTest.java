package chess.eval;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class ScoreEncoderTest {

	@Test
	public void testEncode() {
		class TestCase{
			final int score;
			final int margin;
			final int flags;
			final boolean cutoffType;
			
			TestCase(int score, int margin, int flags, boolean cutoffType){
				this.score = score;
				this.margin = margin;
				this.flags = flags;
				this.cutoffType = cutoffType;
			}
			
			public boolean test(){
				final int scoreEncoding = ScoreEncoder.encode(score, margin, flags, cutoffType);
				/*System.out.println("calculated = "+ScoreEncoder.getScore(scoreEncoding)+", "+
						ScoreEncoder.getMargin(scoreEncoding)+", "+ScoreEncoder.getFlags(scoreEncoding));*/
				return ScoreEncoder.getScore(scoreEncoding) == score &&
						ScoreEncoder.getMargin(scoreEncoding) == margin &&
						ScoreEncoder.isLowerBound(scoreEncoding) == cutoffType &&
						ScoreEncoder.getFlags(scoreEncoding) == flags;
			}
		}
		
		List<TestCase> l = new ArrayList<>();
		l.add(new TestCase(500, 80, 7, true));
		l.add(new TestCase(500, 90, 3, false));
		l.add(new TestCase(500, 500, 2, true));
		l.add(new TestCase(500, 80, 1, false));
		l.add(new TestCase(500, 300, 5, true));
		l.add(new TestCase(-500, 0, 0, false));
		l.add(new TestCase(-382, 200, 2, true));
		l.add(new TestCase(4000, 80, 1, true));
		l.add(new TestCase(-5000, -300, 5, true));
		l.add(new TestCase(5832, 0, 0, false));
		l.add(new TestCase(500, -100, 5, false));
		l.add(new TestCase(-500, 0, 0, true));
		l.add(new TestCase(-382, -50, 2, false));
		l.add(new TestCase(4000, -80, 1, true));
		l.add(new TestCase(-5000, -30, 5, false));
		l.add(new TestCase(5832, 0, 0, true));
		
		for(TestCase testCase: l){
			//System.out.println("expected results = "+testCase.score+", "+testCase.margin+", "+testCase.flags);
			Assert.assertTrue(testCase.test());
		}
	}

}
