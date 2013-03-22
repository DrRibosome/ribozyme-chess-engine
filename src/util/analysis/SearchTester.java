package util.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import search.Search4;
import search.SearchStat;
import search.search33.SearchS4V33;
import state4.State4;
import uci.Position;
import util.FenParser;
import eval.Evaluator2;
import eval.evalV9.SuperEvalS4V9;

/**
 * tests search implementations by running through random test positions
 *
 */
public final class SearchTester {
	/*
---------------------------------------------------
depth = 5, hash = 20

search33
positions searched = 600
avg nodes searched = 28049.911666666667
avg time (ms) = 27.805
avg nodes/ms = 1008.8081879757838
avg braching factor = 7.462642582655846
avg hash hit rate = 0.1072277886555436

search32k
positions searched = 600
avg nodes searched = 58172.73
avg time (ms) = 52.038333333333334
avg nodes/ms = 1117.8822662780642
avg braching factor = 8.616624555337996
avg hash hit rate = 0.10688461758628141


---------------------------------------------------
depth = 6, hash = 20

search33
positions searched = 600
avg nodes searched = 59775.13
avg time (ms) = 54.405
avg nodes/ms = 1098.7065527065527
avg braching factor = 6.051671057108092
avg hash hit rate = 0.10523348088076094

search32k
positions searched = 600
avg nodes searched = 221919.385
avg time (ms) = 202.21833333333333
avg nodes/ms = 1097.42465651812
avg braching factor = 7.431774228704282
avg hash hit rate = 0.09875843728868781

---------------------------------------------------
depth = 9, hash = 20

search33
positions searched = 500
avg nodes searched = 631405.434
avg time (ms) = 562.978
avg nodes/ms = 1121.5454849034954
avg braching factor = 4.300426388513804
avg hash hit rate = 0.09721173226393234

search32k
positions searched = 200
avg nodes searched = 5985269.17
avg time (ms) = 5123.31
avg nodes/ms = 1168.242634156434
avg braching factor = 5.390726187869697
avg hash hit rate = 0.09519992815962193

	 */
	public static void main(String[] args) throws IOException{
		
		//load positions
		final File posf = new File("sample-positions.fen");
		final Scanner scanner = new Scanner(posf);
		final List<Position> pos = new ArrayList<>();
		while(scanner.hasNextLine()){
			final String fen = scanner.nextLine();
			final Position p = FenParser.parse(fen);
			pos.add(p);
		}
		scanner.close();
		
		
		//initialize search
		final int searchDepth = 9;
		final int hashSize = 20;
		final Evaluator2<State4> e = new SuperEvalS4V9();
		final Search4 searcher =
				//new SearchS4V32k(e, hashSize, false);
				new SearchS4V33(e, hashSize, false);
		
		//search positions
		final int len = pos.size();
		final SearchStat agg = new SearchStat();
		for(int a = 0; a < len; a++){
			final Position p = pos.get(a);
			searcher.resetSearch();
			
			searcher.search(p.sideToMove, p.s, null, searchDepth);
			SearchStat.agg(searcher.getStats(), agg);
			
			if(a % 100 == 0 && a != 0){
				printAggStats(agg, a);
				System.out.println("-------------------------------------");
			}
		}

		System.out.println("position file = \""+posf+"\"");
		System.out.println("search = "+searcher.getClass().getName());
		System.out.println("eval = "+e.getClass().getName());
		System.out.println("search depth = "+searchDepth);
		System.out.println("hash size = "+hashSize);
		System.out.println();
		printAggStats(agg, len);
	}
	
	private static void printAggStats(final SearchStat agg, final int len){
		System.out.println("positions searched = "+len);
		System.out.println("avg nodes searched = "+(agg.nodesSearched*1./len));
		System.out.println("avg time (ms) = "+(agg.searchTime*1./len));
		System.out.println("avg nodes/ms = "+(agg.nodesSearched*1./agg.searchTime));
		System.out.println("avg braching factor = "+(agg.empBranchingFactor*1./len));
		System.out.println("avg hash hit rate = "+(agg.hashHits*1./agg.nodesSearched));
	}
}
