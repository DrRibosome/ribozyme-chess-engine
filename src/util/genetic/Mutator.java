package util.genetic;

import eval.expEvalV3.EvalParameters;

public interface Mutator {
	public void mutate(EvalParameters p, int mutations, GEntity parent);
}
