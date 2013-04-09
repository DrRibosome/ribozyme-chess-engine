package util.genetic.mutatorV1;

import util.genetic.GEntity;
import eval.expEvalV3.EvalParameters;

public interface Mutator {
	public void mutate(EvalParameters p, int mutations, GEntity parent);
}
