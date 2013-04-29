package util.genetic;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

import eval.EvalParameters;

public final class GEntity{
	private static int idIndex = 0;
	public EvalParameters p;
	final AtomicInteger wins = new AtomicInteger();
	final AtomicInteger losses = new AtomicInteger();
	final AtomicInteger draws = new AtomicInteger();
	public int id;
	public double variance;
	/** index of entity in population vector*/
	public int index;
	private final DecimalFormat df = new DecimalFormat("#.####");
	
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
	
	@Override
	public String toString(){
		return "(w,l,d)=("+wins.get()+","+losses.get()+","+draws.get()+"), id="+id;
	}
}