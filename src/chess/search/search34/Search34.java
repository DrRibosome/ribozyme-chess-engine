package chess.search.search34;

import chess.eval.Evaluator;
import chess.eval.ScoreEncoder;
import chess.eval.e9.pipeline.EvalResult;
import chess.search.MoveSet;
import chess.search.Search;
import chess.search.SearchListener2;
import chess.search.SearchStat;
import chess.search.search34.pipeline.*;
import chess.state4.BitUtil;
import chess.state4.MoveEncoder;
import chess.state4.SEE;
import chess.state4.State4;

/** heavy chess.search reductions for non-pv lines after depth 7*/
public final class Search34 implements Search{
	
	public final static class SearchStat32k extends SearchStat{
		/** scores returned from quiet chess.search without bottoming out*/
		public long forcedQuietCutoffs;
		public long nullMoveVerifications;
		public long nullMoveCutoffs;
		/** scores seen pes ply*/
		public int[] scores;
		public int maxPlySearched;
		
		public String toString(){
			return "n="+nodesSearched+", t="+searchTime+", hh="+hashHits+", qc="+forcedQuietCutoffs;
		}
	}
	
	public final static int ONE_PLY = 8;
	private final static int maxScore = 90000;
	private final static int minScore = -90000;

	private final StackFrame[] stack;
	private final SearchStat32k stats = new SearchStat32k();
	private final Evaluator e;
	private final int qply = 12;
	private final Hash m;
	private SearchListener2 l;
	private final static int stackSize = 256;
	/** sequence number for hash entries*/
	private int seq;
	private final MoveGen moveGen = new MoveGen();
	private final TTEntry fillEntry = new TTEntry();
	private volatile boolean cutoffSearch = false;
	private final boolean printPV;
	/** stores pv line as its encounted in PVS chess.search*/
	private final long[] pvStore = new long[64];
	private final EntryStage searchPipeline;
	
	public Search34(Evaluator e, int hashSize, boolean printPV){
		this.e = e;
		
		m = new ZMap4(hashSize);
		
		stack = new StackFrame[stackSize];
		for(int i = 0; i < stack.length; i++){
			stack[i] = new StackFrame();
		}
		stats.scores = new int[stackSize];
		
		this.printPV = printPV;

		//construct search pipeline
		FinalStage finalStage = new DescentStage(moveGen, stack, m, pvStore, this);
		MidStage loadKillers = new LoadKillerMoveStage(stack, finalStage);
		MidStage internalIterativeDeepening = new InternalIterativeDeepeningStage(m, this, loadKillers);
		MidStage nullMovePruning = new NullMoveStage(stack, m, this, internalIterativeDeepening);
		MidStage razoring = new RazoringStage(this, nullMovePruning);
		MidStage futilityPruning = new FutilityPruningStage(razoring);
		EntryStage entry = new HashLookupStage(stack, m, e, futilityPruning);
		this.searchPipeline = entry;

	}
	
	public SearchStat32k getStats(){
		return stats;
	}
	
	public void search(final int player, final State4 s, final MoveSet moveStore){
		search(player, s, moveStore, -1);
	}
	
	@Override
	public void resetSearch(){
		m.clear();
		seq = 0;
		e.reset();
		moveGen.reset();
	}
	
	public void search(final int player, final State4 s, final MoveSet moveStore, final int maxPly){
		stats.nodesSearched = 0;
		stats.hashHits = 0;
		stats.forcedQuietCutoffs = 0;
		stats.nullMoveVerifications = 0;
		stats.nullMoveCutoffs = 0;
		stats.maxPlySearched = 0;
		stats.searchTime = System.currentTimeMillis();
		
		//chess.search initialization
		seq++;
		moveGen.dampen();
		cutoffSearch = false;
		
		long bestMove = 0;
		int score;
		
		int alpha = minScore;
		int beta = maxScore;
		
		long nodesSearched = 0;
		boolean skipAdjust = false;
		int minRestartDepth = 7;
		for(int i = 1; (maxPly == -1 || i <= maxPly) && !cutoffSearch && i <= stackSize; i++){
			s.resetHistory();
			
			if(i <= 3){
				alpha = minScore;
				beta = maxScore;
			} else if(i > 3 && !skipAdjust){
				final int index = i-1-1; //index of most recent score observation
				int est = stats.scores[index];
				est += stats.scores[index-1];
				est /= 2;
				final double dir = stats.scores[index]-stats.scores[index-1];
				est += dir/2;
				alpha = est-25;
				beta = est+25;
			}
			skipAdjust = false;
			
			score = recurse(new SearchContext(player, alpha, beta, i*ONE_PLY, NodeType.pv, 0), s);
			
			if((score <= alpha || score >= beta) && !cutoffSearch){
				if(score <= alpha){
					alpha = score-50;
					beta = score+15;
				} else if(score >= beta){
					beta = score+50;
					alpha = score-15;
				}
				
				if(i < minRestartDepth){
					score = recurse(new SearchContext(player, alpha, beta, i*ONE_PLY, NodeType.pv, 0), s);
					if((score <= alpha || score >= beta) && !cutoffSearch){
						i--;
						if(score <= alpha) alpha = score-150;
						else if(score >= beta) beta = score+150;
						skipAdjust = true;
						continue;
					}
				} else{
					minRestartDepth = i+1;
					
					i -= i/4+.5;
					skipAdjust = true;
					continue;
				}
			}
			
			if(!cutoffSearch){
				nodesSearched = stats.nodesSearched;
				stats.maxPlySearched = i;
				
				bestMove = pvStore[0];
				
				if(printPV){
					String pvString = "";
					for(int a = 0; a < i; a++){
						long move = pvStore[a];
						int pos1 = MoveEncoder.getPos1(move);
						int pos2 = MoveEncoder.getPos2(move);
						pvString += moveString(pos1)+moveString(pos2)+" ";
					}
					
					long time = System.currentTimeMillis()-stats.searchTime;
					int nps = (int)(stats.nodesSearched*1000./time);
					
					String infoString = "info depth "+i+" score cp "+score +
							" time "+time +
							" nodes "+stats.nodesSearched +
							" nps "+nps +
							" pv "+pvString;
					
					System.out.println(infoString);
				}
				
				stats.predictedScore = score;
				if(l != null){
					l.plySearched(bestMove, i, score);
				}
				
				if(i-1 < stats.scores.length){
					stats.scores[i-1] = score;
				}
			}
		}
		
		stats.empBranchingFactor = Math.pow(nodesSearched, 1./stats.maxPlySearched);
		
		if(moveStore != null){
			final int pos1 = MoveEncoder.getPos1(bestMove);
			final int pos2 = MoveEncoder.getPos2(bestMove);
			moveStore.piece = 1L << pos1;
			moveStore.moves = 1L << pos2;
			moveStore.promotionType = MoveEncoder.getPawnPromotionType(bestMove);
		}
		
		stats.searchTime = System.currentTimeMillis()-stats.searchTime;
	}
	
	private static String moveString(int pos){
		return ""+(char)('a'+(pos%8))+(pos/8+1);
	}
	
	public void cutoffSearch(){
		cutoffSearch = true;
	}

	public boolean isCutoffSearch(){
		return cutoffSearch;
	}

	public int getSeq(){
		return seq;
	}
	
	/** tests to see if the passed player is in check*/
	public static boolean isChecked(final int player, final State4 s){
		return State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s);
	}
	
	public int recurse(SearchContext c, final State4 s){
		stats.nodesSearched++;

		assert c.alpha < c.beta;
		
		if(s.isForcedDraw()){
			return 0;
		} else if(c.depth <= 0){
			final int q = qsearch(c.player, c.alpha, c.beta, 0, c.stackIndex, c.nt, s);
			if(q > 70000 && c.nt == NodeType.pv){ //false mate for enemy king
				return recurse(new SearchContext(c.player, q, maxScore, ONE_PLY, c.nt, c.stackIndex), s);
			} else if(q < -70000 && c.nt == NodeType.pv){ //false mate for allied king
				return recurse(new SearchContext(c.player, minScore, q, ONE_PLY, c.nt, c.stackIndex), s);
			} else{
				return q;
			}
		} else if(cutoffSearch){
			return 0;
		}

		return searchPipeline.eval(c, s);
	}
	
	public int qsearch(final int player, int alpha, int beta, final int depth,
			final int stackIndex, final NodeType nt, final State4 s){
		stats.nodesSearched++;
		
		if(depth < -qply){
			stats.forcedQuietCutoffs++;
			return beta; //qply bottomed out, return bad value
		} else if(cutoffSearch){
			return 0;
		}

		
		int w = 0;
		final MoveSet[] mset = stack[stackIndex].mlist.list;

		final long zkey = s.zkey();
		final TTEntry e = m.get(zkey);
		final boolean hasTTMove;
		final long ttMove;
		final EvalResult staticEval;
		if(e != null){
			stats.hashHits++;
			if(e.depth >= depth){
				final int cutoffType = e.cutoffType;
				if(nt == NodeType.pv? cutoffType == TTEntry.CUTOFF_TYPE_EXACT: (e.score >= beta?
						cutoffType == TTEntry.CUTOFF_TYPE_LOWER: cutoffType == TTEntry.CUTOFF_TYPE_UPPER)){
					return e.score;
				}
			}
			if(e.move != 0){
				final long encoding = e.move;
				final MoveSet temp = mset[w++];
				temp.piece = 1L<<MoveEncoder.getPos1(encoding);
				temp.moves = 1L<<MoveEncoder.getPos2(encoding);
				temp.rank = MoveGen.tteMoveRank;
				hasTTMove = true;
				ttMove = encoding;
			} else{
				hasTTMove = false;
				ttMove = 0;
			}
			
			staticEval = nt == NodeType.pv? this.e.refine(player, s, minScore, maxScore, e.staticEval):
				this.e.refine(player, s, alpha, beta, e.staticEval);
		} else{
			hasTTMove = false;
			ttMove = 0;
			staticEval = nt == NodeType.pv? this.e.eval(player, s): this.e.eval(player, s, alpha, beta);
		}
		
		int bestScore;
		final boolean alliedKingAttacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s);
		if(alliedKingAttacked){
			bestScore = -77777;
		} else{
			bestScore = staticEval.score;
			if(bestScore >= beta){ //standing pat
				return bestScore;
			} else if(bestScore > alpha && nt == NodeType.pv){
				alpha = bestScore;
			}
		}
		
		final int length = moveGen.genMoves(player, s, alliedKingAttacked, mset, w, true, stackIndex);
		isort(mset, length);

		
		int g = alpha;
		int cutoffFlag = TTEntry.CUTOFF_TYPE_UPPER;
		long bestMove = 0;
		final int drawCount = s.drawCount; //stored for error checking purposes
		for(int i = 0; i < length; i++){
			final MoveSet set = mset[i];
			final long pieceMask = set.piece;
			final int promotionType = set.promotionType;
			final long move = set.moves;
			long encoding = s.executeMove(player, pieceMask, move, promotionType);
			final boolean isDrawable = s.isDrawable();

			if(State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s)){
				//king in check after move
				g = -77777;
			} else{
				if(nt != NodeType.pv && !alliedKingAttacked && !MoveEncoder.isPawnPromotion(encoding) &&
						(!hasTTMove || encoding != ttMove)){
					s.undoMove();
					if(SEE.seeSign(player, pieceMask, move, s) < 0){
						continue;
					} else{
						s.executeMove(player, pieceMask, move, promotionType);
					}
				}
				
				g = -qsearch(1-player, -beta, -alpha, depth-1, stackIndex+1, nt, s);
			}
			s.undoMove();

			if(isDrawable && 0 > g){ //can draw instead of making the move
				g = 0;
				encoding = 0;
			}

			assert zkey == s.zkey();
			assert drawCount == s.drawCount;

			if(g > bestScore){
				bestScore = g;
				bestMove = encoding;
				if(g > alpha){
					alpha = g;
					cutoffFlag = TTEntry.CUTOFF_TYPE_EXACT;
					if(g >= beta){
						if(!cutoffSearch){
							fillEntry.fill(zkey, encoding, g, staticEval.toScoreEncoding(), depth, TTEntry.CUTOFF_TYPE_LOWER, seq);
							m.put(zkey, fillEntry);
						}
						return g;
					}
				}
			}
		}

		if(!cutoffSearch){
			fillEntry.fill(zkey, bestMove, bestScore, staticEval.toScoreEncoding(),
					depth, nt == NodeType.pv? cutoffFlag: TTEntry.CUTOFF_TYPE_UPPER, seq);
			m.put(zkey, fillEntry);
		}
		return bestScore;
	}
	
	/**
	 * attempts to store a move as a killer move
	 * @param move move to be stored
	 * @param skipNullMove current skip null move status
	 * @param prev previous move list
	 */
	public static void attemptKillerStore(final long move, final boolean skipNullMove, final StackFrame prev){
		assert prev != null;
		if(move != 0 && !skipNullMove &&
				(move&0xFFF) != prev.killer[0] &&
				(move&0xFFF) != prev.killer[1] &&
				MoveEncoder.getTakenType(move) == State4.PIECE_TYPE_EMPTY &&
				MoveEncoder.isEnPassanteTake(move) == 0 &&
				!MoveEncoder.isPawnPromotion(move)){
			if(prev.killer[0] != move){
				prev.killer[1] = prev.killer[0];
				prev.killer[0] = move & 0xFFF;
			} else{
				prev.killer[0] = prev.killer[1];
				prev.killer[1] = move & 0xFFF;
			}
		}
	}
	
	/**
	 * insertion sort (lowest rank first)
	 * @param mset move set for sorting
	 * @param length length of move set
	 */
	public static void isort(final MoveSet[] mset, final int length){
		for(int i = 1; i < length; i++){
			for(int a = i; a > 0 && mset[a-1].rank > mset[a].rank; a--){
				final MoveSet temp = mset[a];
				mset[a] = mset[a-1];
				mset[a-1] = temp;
			}
		}
	}

	@Override
	public void setListener(SearchListener2 l) {
		this.l = l;
	}
}
