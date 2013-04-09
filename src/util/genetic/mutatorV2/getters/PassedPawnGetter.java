package util.genetic.mutatorV2.getters;

import java.util.List;

import util.genetic.mutatorV2.Getter;
import eval.Weight;
import eval.expEvalV3.EvalParameters;

public final class PassedPawnGetter{
	public static void build(List<Getter> l){
		for(int a = 0; a < 8; a++){
			final int index = a;
			l.add(new Getter(){
				public int get(EvalParameters p){
					return p.passedPawnRowWeight[0][index].start;
				}
				public void set(EvalParameters p, int i){
					final int end = p.passedPawnRowWeight[0][index].end;
					p.passedPawnRowWeight[0][index] = new Weight(i, end);
					p.passedPawnRowWeight[1][7-index] = new Weight(i, end);
				}
				public String toString(){
					return "passed pawn (r="+index+",start) weight";
				}
			});
			l.add(new Getter(){
				public int get(EvalParameters p){
					return p.passedPawnRowWeight[0][index].end;
				}
				public void set(EvalParameters p, int i){
					final int start = p.passedPawnRowWeight[0][index].start;
					p.passedPawnRowWeight[0][index] = new Weight(start, i);
					p.passedPawnRowWeight[1][7-index] = new Weight(start, i);
				}
				public String toString(){
					return "passed pawn (r="+index+",end) weight";
				}
			});
		}
	}
}
