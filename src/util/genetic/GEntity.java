package util.genetic;

import java.util.concurrent.atomic.AtomicInteger;

import eval.expEvalV3.EvalParameters;

public final class GEntity{
	private static int idIndex = 0;
	EvalParameters p;
	final AtomicInteger wins = new AtomicInteger();
	final AtomicInteger losses = new AtomicInteger();
	final AtomicInteger draws = new AtomicInteger();
	public final int id;
	GEntity(){
		id = idIndex++;
	}
	public double score(){
		final double score = wins.get()+draws.get()/2.;
		return score/totalGames();
	}
	public int totalGames(){
		return wins.get() + losses.get() + draws.get();
	}
	public String toString(){
		return "(w,l,d)=("+wins.get()+","+losses.get()+","+draws.get()+"), id="+id;
	}
}