package search.parallel;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import search.Search4;
import search.SearchListener2;
import search.SearchStat;
import state4.BitUtil;
import state4.Masks;
import state4.MoveEncoder;
import state4.State4;
import eval.Evaluator2;

public final class PSearch implements Search4{

	
	/** controls printing pv to console for debugging*/
	private final boolean printPV;
	/** controls whether the printed pv should be in uci style*/
	private final static boolean uciPV = true;
	
	
	
	private final AtomicBoolean cutoffSearch = new AtomicBoolean(false);
	
	public PSearch(Evaluator2 e, int hashSize){
		this(e, hashSize, false);
	}
	
	public PSearch(Evaluator2 e, int hashSize, boolean printPV){
		this.e = e;
		
		//m = new ZMap3(hashSize);
		m = new ZMap4(hashSize);
		
		stack = new MoveList[stackSize];
		for(int i = 0; i < stack.length; i++){
			stack[i] = new MoveList();
		}
		stats.scores = new int[stackSize];
		
		this.printPV = printPV;
	}
	
	public SearchStat32k getStats(){
		return stats;
	}
	
	public void search(final int player, final State4 s, final int[] moveStore){
		search(player, s, moveStore, -1);
	}
	
	@Override
	public void resetSearch(){
		m.clear();
		seq = 0;
	}
	
	public void search(final int player, final State4 s, final int[] moveStore, final int maxPly){
		stats.nodesSearched = 0;
		stats.hashHits = 0;
		stats.forcedQuietCutoffs = 0;
		stats.nullMoveVerifications = 0;
		stats.nullMoveCutoffs = 0;
		stats.maxPlySearched = 0;
		stats.searchTime = System.currentTimeMillis();
		
		//search initialization
		seq++;
		e.initialize(s);
		cutoffSearch.set(false);
		
		final boolean debugPrint = false;
		
		long bestMove = 0;
		int score = 0;
		
		final int max = 90000;
		final int min = -90000;
		
		final int failOffset = 100;
		long nodesSearched = 0;
		for(int i = 1; (maxPly == -1 || i <= maxPly) && !cutoffSearch.get() && i <= stackSize; i++){
			s.resetHistory();
			int alpha = min;
			int beta = max;
			
			if(i > 3){
				/*alpha = score-35;
				beta = score+35;*/
				final int index = i-1-1; //index of most recent score observation
				int est = stats.scores[index];
				est += stats.scores[index-1];
				est /= 2;
				final double dir = stats.scores[index]-stats.scores[index-1];
				est += dir/2;
				alpha = est-35;
				beta = est+35;
			}
			
			//System.out.println("starting depth "+i);
			score = recurse(player, alpha, beta, i, true, true, 0, s);
			
			if((score <= alpha || score >= beta) && !cutoffSearch.get()){
				final boolean failLow = score <= alpha;
				if(failLow) alpha = score-failOffset;
				else beta = score+failOffset;
				score = recurse(player, alpha, beta, i, true, true, 0, s);
				if((score <= alpha || score >= beta) && !cutoffSearch.get()){
					final boolean failLow2 = score <= alpha;
					if(failLow2) alpha = min;
					else beta = max;
					score = recurse(player, alpha, beta, i, true, true, 0, s);
				}
			}
			
			if(!cutoffSearch.get()){
				nodesSearched = stats.nodesSearched;
				stats.maxPlySearched = i;
			}
			final TTEntry tte;
			if((tte = m.get(s.zkey())) != null && tte.move != 0 && !cutoffSearch.get()){
				bestMove = tte.move;
				stats.predictedScore = tte.score;
				if(l != null){
					l.plySearched(bestMove, i, score);
				}
				if(printPV){
					final String pvString = getPVString(player, s, "", 0, i, uciPV);
					if(!uciPV) System.out.println("pv "+i+": ["+score+"] "+pvString);
					else System.out.println("info depth "+i+" score cp "+(int)score+" time "+
							((System.currentTimeMillis()-stats.searchTime)/1000.)+
							" nodes "+stats.nodesSearched+" nps "+(int)(stats.nodesSearched*1000./
							(System.currentTimeMillis()-stats.searchTime))+" pv "+pvString);
				}
			}
			if(i-1 < stats.scores.length){
				stats.scores[i-1] = score;
			}
		}
		
		stats.empBranchingFactor = Math.pow(nodesSearched, 1./stats.maxPlySearched);
		
		if(f != null){
			//record turn, piece counts, and scores at each level of search
			String pieceCounts = "";
			for(int q = 0; q < 2; q++){
				pieceCounts += "<piece-counts-"+q+"=";
				for(int a = 0; a < s.pieceCounts[0].length; a++){
					pieceCounts += s.pieceCounts[q][a];
					if(a != s.pieceCounts[0].length-1)
						pieceCounts+=",";
				}
				pieceCounts += ">";
			}
			String scorestr = "<scores=";
			for(int a = 0; a < maxPly && a < stats.scores.length; a++){
				scorestr += stats.scores[a];
				if(a != maxPly-1)
					scorestr += ",";
			}
			scorestr += ">";
			String record = "<turn="+player+">"+pieceCounts+scorestr;
			//ps.println(record);
			try{
				f.write(record+"\n");
				f.flush();
			} catch(IOException a){
				a.printStackTrace();
			}
			//ps.append(record+"\n");
		}

		stats.endScore = score;
		
		if(moveStore != null){
			int pos1 = MoveEncoder.getPos1(bestMove);
			int pos2 = MoveEncoder.getPos2(bestMove);
			moveStore[0] = pos1;
			moveStore[1] = pos2;
		}
		
		stats.searchTime = System.currentTimeMillis()-stats.searchTime;
	}
	
	private String getPVString(int player, State4 s, String pv, int depth, int maxDepth, boolean uci){
		final TTEntry e = m.get(s.zkey());
		if(depth < maxDepth && e != null && e.move != 0){
			int pos1 = MoveEncoder.getPos1(e.move);
			int pos2 = MoveEncoder.getPos2(e.move);

			long pmask = 1L<<pos1;
			long mmask = 1L<<pos2;
			s.executeMove(player, pmask, mmask);

			if(!uci){
				this.e.initialize(s);
				final double eval = this.e.eval(s, player);
				pv += moveString(pos1)+"->"+moveString(pos2)+" ("+eval+"), ";
			} else{
				pv += moveString(pos1)+moveString(pos2)+" ";
			}
			
			String r = getPVString(1-player, s, pv, depth+1, maxDepth, uci);
			s.undoMove();
			return r;
		}
		return pv;
	}
	
	private static String moveString(int pos){
		return ""+(char)('a'+(pos%8))+(pos/8+1);
	}
	
	/**
	 * Traverses TT entries until the leaf state from which the evaluation was performed
	 * is found. This can potentially fail if the TT path has been broken
	 * <p> note, this should be called directly after a search has been performed
	 * @param player
	 * @param s
	 * @param depth
	 * @param maxDepth
	 * @param result store for the result
	 */
	public void getPredictedLeafState(int player, State4 s, int depth, int maxDepth, State4 result){
		/*final TTEntry e = m.get(s.zkey());
		if(depth < maxDepth && e != null && e.move != 0){
			final long pmask = 1L<<MoveEncoder.getPos1(e.move);
			final long mmask = 1L<<MoveEncoder.getPos2(e.move);
			s.executeMove(player, pmask, mmask);
			getPredictedLeafState(1-player, s, depth+1, maxDepth, result);
			s.undoMove();
			return;
		}
		State4.copy(s, result);*/
	}
	
	@Override
	public void cutoffSearch(){
		cutoffSearch.set(true);
	}
	
	@Override
	public void setListener(SearchListener2 l) {
		//this.l = l;
	}
}
