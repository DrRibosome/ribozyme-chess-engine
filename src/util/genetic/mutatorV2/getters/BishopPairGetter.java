package util.genetic.mutatorV2.getters;

import java.util.List;

import eval.Weight;
import eval.expEvalV3.EvalParameters;

import util.genetic.mutatorV2.Getter;

public final class BishopPairGetter {
	public static void add(List<Getter> l){
		final String name = "bishop pair";
		l.add(new Getter() {
			@Override
			public int get(EvalParameters p) {
				return p.bishopPair.start;
			}
			@Override
			public void set(EvalParameters p, int i) {
				p.bishopPair = new Weight(i, p.bishopPair.end);
			}
			@Override
			public String toString(){
				return name+", start";
			}
		});
		l.add(new Getter() {
			@Override
			public int get(EvalParameters p) {
				return p.bishopPair.end;
			}
			@Override
			public void set(EvalParameters p, int i) {
				p.bishopPair = new Weight(p.bishopPair.start, i);
			}
			@Override
			public String toString(){
				return name+", end";
			}
		});
	}
}
