package util.genetic.mutatorV2.getters;

import java.util.List;

import util.genetic.mutatorV2.Getter;
import eval.Weight;
import eval.expEvalV3.EvalParameters;

public final class WeightArrayGetter{
	public static void build(final Weight[] w, List<Getter> l){
		for(int a = 0; a < w.length; a++){
			final int index = a;
			l.add(new Getter(){
				public int get(EvalParameters p) {
					return w[index].start;
				}
				public void set(EvalParameters p, int i) {
					w[index] = new Weight(i, w[index].end);
				}
			});
			l.add(new Getter(){
				public int get(EvalParameters p) {
					return w[index].end;
				}
				public void set(EvalParameters p, int i) {
					w[index] = new Weight(w[index].start, i);
				}
			});
		}
	}
}
