package eval;

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
			
			TestCase(int score, int margin, int flags){
				this.score = score;
				this.margin = margin;
				this.flags = flags;
			}
			
			public boolean test(){
				final int scoreEncoding = ScoreEncoder.encode(score, margin, flags);
				return ScoreEncoder.getScore(scoreEncoding) == score &&
						ScoreEncoder.getMargin(scoreEncoding) == margin &&
						ScoreEncoder.getFlags(scoreEncoding) == flags;
			}
		}
		
		List<TestCase> l = new ArrayList<>();
		l.add(new TestCase(500, 80, 7));
		l.add(new TestCase(500, 900, 3));
		l.add(new TestCase(500, 800, 2));
		l.add(new TestCase(500, 80, 1));
		l.add(new TestCase(500, 300, 5));
		l.add(new TestCase(-500, 0, 0));
		l.add(new TestCase(-382, 800, 2));
		l.add(new TestCase(4000, 80, 1));
		l.add(new TestCase(-5000, 300, 5));
		l.add(new TestCase(5832, 0, 0));
		
		for(TestCase testCase: l){
			//System.out.println(testCase.score+", "+testCase.margin+", "+testCase.flags);
			Assert.assertTrue(testCase.test());
		}
	}

}
