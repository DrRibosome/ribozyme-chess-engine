package tests;

import java.util.ArrayList;
import java.util.List;

import util.OldPositions;
import util.board4.Debug;
import util.board4.State4;
import ai.modularAI2.Evaluator2;
import customAI.evaluators.board4.EvalS4;
import customAI.evaluators.board4.SuperEvalS4V7;
import customAI.searchers.board4.SearchS4V21qzit;
import eval.TestEval;

/** tests searchs on several sample boards and aggregates results*/
public class Test21v2 {
	public static void main(String[] args){
		
		List<char[][]> boards = new ArrayList<char[][]>();
		boards.add(OldPositions.puzzleSetup1);
		boards.add(OldPositions.puzzleSetup2);
		boards.add(OldPositions.puzzleSetup3);
		boards.add(OldPositions.enPassanteMistake);
		boards.add(OldPositions.queenSac1);
		boards.add(OldPositions.queenSac2);
		
		final int maxPly = 6;
		SearchS4V21qzit.SearchStat agg = new SearchS4V21qzit.SearchStat();
		
		for(char[][] c: boards){
			State4 s = Debug.loadConfig(c);
			
			Evaluator2<State4> e1 = 
					//new SuperEvalS4V6();
					//new SuperEvalS4V7();
					//new EvalS4();
					new TestEval();
			
			SearchS4V21qzit search = new SearchS4V21qzit(8, s, e1, 20);
			
			SearchS4V21qzit.SearchStat stats = search.search(State4.WHITE, maxPly, new int[4]);
			agg(stats, agg);
			print(stats);
			
			/*stats = search.search(State3.BLACK, maxPly, new int[4]);
			agg(stats, agg);
			print(stats);*/
		}
		
		System.out.println("\n=======================\n");
		print(agg);

	}
	
	/** aggregate search stats together*/
	private static void agg(SearchS4V21qzit.SearchStat s, SearchS4V21qzit.SearchStat agg){
		agg.forcedQuietCutoffs += s.forcedQuietCutoffs;
		agg.hashHits += s.hashHits;
		agg.nodesSearched += s.nodesSearched;
		agg.searchTime += s.searchTime;
	}
	
	private static void print(SearchS4V21qzit.SearchStat s){
		String t = ""+s.nodesSearched+" in "+s.searchTime+"ms (hash="+s.hashHits+", q-cuts="+s.forcedQuietCutoffs+")";
		System.out.println(t);
	}
}
