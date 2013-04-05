package util.genetic;

import java.nio.ByteBuffer;

import eval.expEvalV3.DefaultEvalWeights;
import eval.expEvalV3.EvalParameters;

public class GeneticTrainer {
	public static void main(String[] args) throws Exception{
		final ByteBuffer holder = ByteBuffer.allocate(1<<20);
		final int population = 1;
		
		
		for(int a = 0; a < population; a++){
			DefaultEvalWeights.defaultEval().write(holder);
			System.out.println("pos 1 = "+holder.position());
			holder.rewind();
			new EvalParameters().read(holder);
			System.out.println("pos 2 = "+holder.position());
		}
		holder.limit(holder.position());
		
	}
}
