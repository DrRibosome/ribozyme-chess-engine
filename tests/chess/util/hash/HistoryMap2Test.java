package chess.util.hash;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import chess.state4.HistoryMap2;

public class HistoryMap2Test {

	@Test
	public void testPut() {
		final int size = 7;
		final long seed = 123L;
		
		//needs multiple attemps because the hash offsets (for rehashing) are chosen randomly
		for(int a = 0; a < 100; a++){
			final HistoryMap2 m = new HistoryMap2(size);
			Random r1 = new Random(seed);
			final int tests = 70;
			for(int q = 0; q < tests; q++){
				final long l = r1.nextLong();
				m.put(l);
			}
			Random r2 = new Random(seed);
			int count = 0;
			for(int q = 0; q < tests; q++){
				final long l = r2.nextLong();
				count += m.get(l);
			}
			
			assertEquals(count, tests);
		}
	}
	
	@Test
	public void testRemove() {
		final int size = 7;
		final long seed = 123L;

		//needs multiple attemps because the hash offsets (for rehashing) are chosen randomly
		for(int a = 0; a < 100; a++){
			final HistoryMap2 m = new HistoryMap2(size);
			Random r1 = new Random(seed);
			final int tests = 70;
			for(int q = 0; q < tests; q++){
				final long l = r1.nextLong();
				m.put(l);
			}
			Random r2 = new Random(seed);
			for(int q = 0; q < tests; q++){
				final long l = r2.nextLong();
				m.remove(l);
			}
			Random r3 = new Random(seed);
			int count = 0;
			for(int q = 0; q < tests; q++){
				final long l = r3.nextLong();
				count += m.get(l);
			}
			
			assertEquals(count, 0);
		}
	}

}
