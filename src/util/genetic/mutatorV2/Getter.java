package util.genetic.mutatorV2;

import eval.expEvalV3.EvalParameters;

public interface Getter{
	int get(EvalParameters p);
	void set(EvalParameters p, int i);
}
