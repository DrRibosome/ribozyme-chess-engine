package util.genetic.mutatorV2.getters;

import java.util.List;

import eval.Weight;
import eval.expEvalV3.EvalParameters;

import util.genetic.mutatorV2.Getter;

public final class TempoGetter {
	public static void add(List<Getter> l){
		l.add(new Getter() {
			@Override
			public int get(EvalParameters p) {
				return p.tempo.start;
			}
			@Override
			public void set(EvalParameters p, int i) {
				p.tempo = new Weight(i, p.tempo.end);
			}
		});
		l.add(new Getter() {
			@Override
			public int get(EvalParameters p) {
				return p.tempo.end;
			}
			@Override
			public void set(EvalParameters p, int i) {
				p.tempo = new Weight(p.tempo.start, i);
			}
		});
	}
}
