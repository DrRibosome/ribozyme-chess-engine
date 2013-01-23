package tests.pvRecover;

import search.SearchS4V25qzitRecord;
import util.board4.MoveEncoder;
import util.board4.State4;
import ai.modularAI2.Evaluator2;
import customAI.evaluators.board4.SuperEvalS4V7;

/** test for recovering the pv*/
public class Test25pv {
	public static void main(String[] args){
		
		State4 s = new State4();
		s.initialize();
		
		final int ply = 15;
		final int hashSize = 20;
		
		Evaluator2<State4> e = new SuperEvalS4V7();
		
		SearchS4V25qzitRecord search = new SearchS4V25qzitRecord(ply, s, e, hashSize);
		
		int player = State4.WHITE;
		search.search(State4.WHITE, null, ply);
		SearchS4V25qzitRecord.SearchStat25 stat = search.getStats();
		double endScore = stat.endScore;
		long[] pv = search.recoverPV(player, ply, endScore);
		
		for(long encoding: pv){
			s.executeMove(player, encoding);
			System.out.println(MoveEncoder.getString(encoding));
			player = 1-player;
		}
		

		e.initialize(s);
		double eval = e.eval(s, player);
		System.out.println("eval = "+eval);
	}
}
