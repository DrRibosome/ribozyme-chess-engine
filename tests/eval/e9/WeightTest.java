package eval.e9;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

public class WeightTest {

	@Test
	public void testEncode() {
		class TestCase{
			int start;
			int end;
			public TestCase(int start, int end) {
				this.start = start;
				this.end = end;
			}
		}
		
		List<TestCase> l = new ArrayList<>();
		Random r = new Random(3827L);
		for(int a = 0; a < 9999; a++){
			l.add(new TestCase((int)(r.nextDouble()*3000-1500), (int)(r.nextDouble()*3000-1500)));
		}
		
		for(TestCase t: l){
			int w = Weight.encode(t.start, t.end);
			assertEquals(Weight.mgScore(w), t.start);
			assertEquals(Weight.egScore(w), t.end);
		}
	}
}
