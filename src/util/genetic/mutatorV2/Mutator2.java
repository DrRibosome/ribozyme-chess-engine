package util.genetic.mutatorV2;

import util.genetic.GEntity;
import eval.expEvalV3.EvalParameters;

public interface Mutator2 {
	/** mutates based off population*/
	public void mutate(EvalParameters p, GEntity[] population, int excludeIndex, double multiplier);
	/** initially mutates the values to generate a starting population*/
	public void initialMutate(EvalParameters p, double stdDev);
	public void printStdDev(GEntity[] population);
}
