package util.genetic.mutatorV2.getters;

import java.util.List;

import util.genetic.mutatorV2.Getter;
import eval.EvalParameters;
import eval.Weight;

public final class TempoGetter {
	public static void add(List<Getter> l){
		final String name = "tempo";
		l.add(new Getter() {
			@Override
			public int get(EvalParameters p) {
				return p.tempo.start;
			}
			@Override
			public void set(EvalParameters p, int i) {
				p.tempo = new Weight(i, p.tempo.end);
			}
			@Override
			public String toString(){
				return name+", start";
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
			@Override
			public String toString(){
				return name+", end";
			}
		});
	}
}
