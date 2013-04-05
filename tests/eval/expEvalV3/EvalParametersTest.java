package eval.expEvalV3;

import java.nio.ByteBuffer;

import junit.framework.Assert;

import org.junit.Test;

public class EvalParametersTest {

	@Test
	public void testParamSaveLoad() {
		final ByteBuffer holder = ByteBuffer.allocate(1<<15);
		DefaultEvalWeights.defaultEval().write(holder);
		int pos1 = holder.position();
		holder.rewind();
		new EvalParameters().read(holder);
		int pos2 = holder.position();
		Assert.assertEquals(pos1, pos2);
	}

}
