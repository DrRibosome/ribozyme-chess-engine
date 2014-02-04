package chess.eval.e9.pipeline;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Random;

public class EvalResultTest {
	@Test
	public void testEncodeDecode(){
		Random r = new Random(4821L);

		int scoreRange = 6000;
		int lowerMarginRange = 1600;
		int upperMarginRange = lowerMarginRange;

		for(int a = 0; a < 1000; a++){

			int score = (int)(r.nextDouble()*scoreRange - scoreRange/2);
			int lowerMargin = (int)(r.nextDouble()*lowerMarginRange - lowerMarginRange/2);
			int upperMargin = (int)(r.nextDouble()*upperMarginRange - upperMarginRange/2);
			int flag = (int)(r.nextDouble()*8);

			EvalResult tester = new EvalResult(score, lowerMargin, upperMargin, flag);
			EvalResult decoding = EvalResult.decode(tester.toScoreEncoding());

			Assert.assertEquals(tester.score, decoding.score);
			Assert.assertEquals(tester.upperMargin, decoding.upperMargin);
			Assert.assertEquals(tester.lowerMargin, decoding.lowerMargin);
		}
	}
}
