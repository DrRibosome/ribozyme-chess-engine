package util.genetic.mutatorV2.getters;

import java.util.List;

import util.genetic.mutatorV2.Getter;
import eval.EvalParameters;

public final class PawnStormGetter {
	public static void add(List<Getter> l){
		final String[] names = new String[]{
				"no allied pawn",
				"allied pawn",
				"blocked enemy pawn",
		};
		
		for(int q = 0; q < 3; q++){
			final int type = q;
			for(int a = 0; a < 6; a++){
				final int index = a;
				l.add(new Getter() {
					@Override
					public int get(EvalParameters p) {
						return p.pawnStorm[type][index];
					}
					@Override
					public void set(EvalParameters p, int i) {
						p.pawnStorm[type][index] = i;
					}
					@Override
					public String toString(){
						return "pawn storm, "+names[type];
					}
				});
			}
		}
	}
}
