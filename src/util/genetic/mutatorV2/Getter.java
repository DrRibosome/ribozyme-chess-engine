package util.genetic.mutatorV2;

import eval.EvalParameters;

public interface Getter{
	int get(EvalParameters p);
	void set(EvalParameters p, int i);
}
