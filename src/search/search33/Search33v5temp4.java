package search.search33;

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

/** same as v4 but with much more conservative futility pruning*/
public final class Search33v5temp4 implements Search4{
	public final static class SearchStat32k extends SearchStat{
		/** scores returned from quiet search without bottoming out*/
		public long forcedQuietCutoffs;
		public long nullMoveVerifications;
		public long nullMoveCutoffs;
		public long nullMoveFailLow;
		/** scores seen pes ply*/
		public int[] scores;
		public int maxPlySearched;
		
		public String toString(){
			return "n="+nodesSearched+", t="+searchTime+", hh="+hashHits+", qc="+forcedQuietCutoffs;
		}
	}
	
	private final MoveList[] stack;
	
	private final static class MoveList{
		private final static int defSize = 128;
		private final MoveGen.MoveList list = new MoveGen.MoveList(defSize);
		/** used to aggregate tte move and killer moves*/
		private final MoveGen.MoveSet[] tempMset = new MoveGen.MoveSet[5];
		public final boolean[] kingAttacked = new boolean[2];
		public boolean skipNullMove = false;
		/** holds killer moves as first 12 bits (ie, masked 0xFFF) of move encoding*/
		public final long[] killer = new long[2];
		
		MoveList(){
			for(int a = 0; a < tempMset.length; a++) tempMset[a] = new MoveGen.MoveSet();
		}
	}
	
	private final static int[] pawnOffset = new int[]{9,7,8,16};
	/** rough material gain by piece type*/
	private final static int[] materialGain = new int[7];
	/** stores lmr reductions by [isPv][depth][move-count]*/
	private final static int[][][] reductions;
	/** stores futility margins by [depth]*/
	private final static int[][] futilityMargins;
	
	static{
		materialGain[State4.PIECE_TYPE_BISHOP] = 300;
		materialGain[State4.PIECE_TYPE_KNIGHT] = 300;
		materialGain[State4.PIECE_TYPE_PAWN] = 100;
		materialGain[State4.PIECE_TYPE_QUEEN] = 900;
		materialGain[State4.PIECE_TYPE_ROOK] = 500;
		
		reductions = new int[2][64][64];
		for (int d = 1; d < 64; d++){ //depth
			//System.out.print("d="+d+":\t");
			for (int mc = 1; mc < 64; mc++) //move count
			{
				final double pvRed = Math.log(d) * Math.log(mc) / 3.0;
				final double nonPVRed = 0.33 + Math.log(d) * Math.log(mc) / 2.25;
				reductions[1][d][mc] = (int)(pvRed > 1? pvRed: 0);
				reductions[0][d][mc] = (int)(nonPVRed > 1? nonPVRed: 0);
				//System.out.print(reductions[1][d][mc]+", ");
			}
			//System.out.println();
		}
		
		futilityMargins = new int[5][64];
		int[][] startEnd = {
				/*{150, 150},
				{250, 250},
				{350, 280},
				{450, 350},
				{600, 450},*/
				{250, 250},
				{300, 250},
				{425, 300},
				{500, 375},
				{600, 450},
		};
		for(int d = 0; d < futilityMargins.length; d++){
			for(int mc = 0; mc < futilityMargins[0].length; mc++){
				futilityMargins[d][mc] = (int)(startEnd[d][0] + (mc+1)*1./futilityMargins[0].length*(startEnd[d][1]-startEnd[d][0]));
			}
		}
	}
	
	private final SearchStat32k stats = new SearchStat32k();
	private final Evaluator2 e;
	private final int qply = 12;
	private final Hash m;
	private FileWriter f;
	private SearchListener2 l;
	private final static int stackSize = 256;
	/** sequence number for hash entries*/
	private int seq;
	
	/** controls printing pv to console for debugging*/
	private final boolean printPV;
	/** controls whether the printed pv should be in uci style*/
	private final static boolean uciPV = true;
	
	private final TTEntry fillEntry = new TTEntry();
	
	private final static int tteMoveRank = -99;
	/** rank set to the first of the non takes*/
	private final static int l1KillerMoveRank = -98;
	private final static int l2KillerMoveRank = -97;
	
	private final AtomicBoolean cutoffSearch = new AtomicBoolean(false);
	
	public Search33v5temp4(Evaluator2 e, int hashSize){
		this(e, hashSize, false);
	}
	
	public Search33v5temp4(Evaluator2 e, int hashSize, boolean printPV){
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
		e.reset();
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
		
		int alpha = min;
		int beta = max;
		
		final int failOffset = 100;
		long nodesSearched = 0;
		boolean skipAdjust = false;
		int minRestartDepth = 7;
		final int backDist = 5; //amount to back up the search
		//assert minRestartDepth-backDist >= 3;
		for(int i = 1; (maxPly == -1 || i <= maxPly) && !cutoffSearch.get() && i <= stackSize; i++){
			s.resetHistory();
			
			if(i <= 3){
				alpha = min;
				beta = max;
			} else if(i > 3 && !skipAdjust){
				/*alpha = score-35;
				beta = score+35;*/
				final int index = i-1-1; //index of most recent score observation
				int est = stats.scores[index];
				est += stats.scores[index-1];
				est /= 2;
				final double dir = stats.scores[index]-stats.scores[index-1];
				est += dir/2;
				alpha = est-40;
				beta = est+40;
				//System.out.println("adjusting");
			}
			skipAdjust = false;
			
			//System.out.println(alpha+", "+beta);
			
			//System.out.println("starting depth "+i);
			score = recurse(player, alpha, beta, i, true, true, 0, s);
			
			if((score <= alpha || score >= beta) && !cutoffSearch.get()){
				//final boolean failLow = score <= alpha;
				//if(score <= alpha || score > beta) System.out.println("fail, score = "+score+", fail low = "+failLow);
				alpha = min;
				beta = max;
				if(i < minRestartDepth){
					score = recurse(player, alpha, beta, i, true, true, 0, s);
				} else{
					/*final TTEntry tte;
					if((tte = m.get(s.zkey())) != null && tte.move != 0 && tte.move != bestMove && !cutoffSearch.get()){
						i = 3;
						skipAdjust = true;
						continue;
					}
					score = recurse(player, alpha, beta, i, true, true, 0, s);*/
					minRestartDepth += 1;
					i -= i/3+.5;//backDist;
					skipAdjust = true;
					continue;
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
		final TTEntry e = m.get(s.zkey());
		if(depth < maxDepth && e != null && e.move != 0){
			final long pmask = 1L<<MoveEncoder.getPos1(e.move);
			final long mmask = 1L<<MoveEncoder.getPos2(e.move);
			s.executeMove(player, pmask, mmask);
			getPredictedLeafState(1-player, s, depth+1, maxDepth, result);
			s.undoMove();
			return;
		}
		State4.copy(s, result);
	}
	
	public void cutoffSearch(){
		cutoffSearch.set(true);
	}
	
	/** tests to see if the passed player is in check*/
	private static boolean isChecked(final int player, final State4 s){
		return State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s);
	}
	
	private static int razorMargin(final double depth){
		return 512+16*(int)depth;
	}
	
	private static int lmrReduction(final boolean pv, final double depth, final int moveCount){
		return reductions[pv? 1: 0][depth < 64? (int)depth: 63][moveCount < 64? moveCount: 63];
	}
	
	private int recurse(final int player, int alpha, final int beta, final double depth,
			final boolean pv, final boolean rootNode, final int stackIndex, final State4 s){
		stats.nodesSearched++;
		assert alpha < beta;
		
		if(s.isForcedDraw()){
			return 0;
		} else if(depth <= 0){
			/*if(!isChecked(player, s)){
				return qsearch(player, alpha, beta, 0, stackIndex, pv, s);
			}
			depth = 1;*/
			final int q = qsearch(player, alpha, beta, 0, stackIndex, pv, s);
			if(q > 70000 && pv){
				return recurse(player, alpha, beta, 1, true, false, stackIndex, s);
			} else{
				return q;
			}
		} else if(cutoffSearch.get()){
			return 0;
		}


		final MoveList ml = stack[stackIndex];
		ml.killer[0] = 0;
		ml.killer[1] = 0;
		ml.list.length = 0;
		
		int w = 0;

		final long zkey = s.zkey();
		final TTEntry e = m.get(zkey);
		boolean tteMove = false;
		long tteMoveEncoding = 0;
		
		if(e != null){
			stats.hashHits++;
			if(e.depth >= depth){
				if(pv ? e.cutoffType == TTEntry.CUTOFF_TYPE_EXACT: (e.score >= beta?
						e.cutoffType == TTEntry.CUTOFF_TYPE_LOWER: e.cutoffType == TTEntry.CUTOFF_TYPE_UPPER)){
					
					if(stackIndex-1 >= 0 && e.score >= beta){
						attemptKillerStore(e.move, ml.skipNullMove, stack[stackIndex-1]);
					}
					
					return e.score;
				}
			}
			if(e.move != 0){
				tteMoveEncoding = e.move;
				ml.tempMset[w++].fill(1L<<MoveEncoder.getPos1(tteMoveEncoding),
						1L<<MoveEncoder.getPos2(tteMoveEncoding), -99);
				tteMove = true;
			}
		}
		
		final int lazyEval = this.e.lazyEval(s, player);
		
		//razoring
		/*final boolean pawnPrePromotion = (s.pawns[player] & Masks.pawnPrePromote[player]) != 0;
		if(!pv &&
				Math.abs(beta) < 70000 &&
				depth < 4 &&
				!ml.kingAttacked[player] &&
				!tteMove &&
				!pawnPrePromotion &&
				lazyEval + razorMargin(depth) < beta){
			final int rbeta = beta-razorMargin(depth);
			final int v = qsearch(player, rbeta-1, rbeta, 0, stackIndex+1, false, s);
			if(v <= rbeta){
				return v+rbeta;
			}
		}*/
		
		//load killer moves
		if(stackIndex-1 >= 0 && !ml.skipNullMove){
			final MoveList prev = stack[stackIndex-1];
			if(prev.killer[0] != 0 && isPseudoLegal(player, prev.killer[0], s)){
				assert MoveEncoder.getPos1(prev.killer[0]) != MoveEncoder.getPos2(prev.killer[0]);
				ml.tempMset[w++].fill(1L<<MoveEncoder.getPos1(prev.killer[0]), 1L<<MoveEncoder.getPos2(prev.killer[0]), l1KillerMoveRank);
			}
			if(prev.killer[1] != 0 && isPseudoLegal(player, prev.killer[1], s)){
				assert MoveEncoder.getPos1(prev.killer[1]) != MoveEncoder.getPos2(prev.killer[1]);
				ml.tempMset[w++].fill(1L<<MoveEncoder.getPos1(prev.killer[1]), 1L<<MoveEncoder.getPos2(prev.killer[1]), l1KillerMoveRank);
			}
			
			if(stackIndex-3 >= 0){
				final MoveList prev2 = stack[stackIndex-3];
				if(prev2.killer[0] != 0 && isPseudoLegal(player, prev2.killer[0], s)){
					assert MoveEncoder.getPos1(prev2.killer[0]) != MoveEncoder.getPos2(prev2.killer[0]);
					ml.tempMset[w++].fill(1L<<MoveEncoder.getPos1(prev2.killer[0]), 1L<<MoveEncoder.getPos2(prev2.killer[0]), l2KillerMoveRank);
				}
				if(prev2.killer[1] != 0 && isPseudoLegal(player, prev2.killer[1], s)){
					assert MoveEncoder.getPos1(prev2.killer[1]) != MoveEncoder.getPos2(prev2.killer[1]);
					ml.tempMset[w++].fill(1L<<MoveEncoder.getPos1(prev2.killer[1]), 1L<<MoveEncoder.getPos2(prev2.killer[1]), l2KillerMoveRank);
				}
			}
		}
		
		ml.kingAttacked[player] = isChecked(player, s);
		ml.kingAttacked[1-player] = isChecked(1-player, s);
		
		//null move pruning
		final boolean threatMove; //true if opponent can make a move that causes null-move fail low
		final boolean hasNonPawnMaterial = s.pieceCounts[player][0]-s.pieceCounts[player][State4.PIECE_TYPE_PAWN] > 1;
		if(!pv && !ml.skipNullMove && depth > 0 &&  !cutoffSearch.get() &&
				!ml.kingAttacked[player] && hasNonPawnMaterial){
			
			final double r = 3 + depth/4;
			
			//note, non-pv nodes are null window searched - no need to do it here explicitly
			stack[stackIndex+1].skipNullMove = true;
			s.nullMove();
			final long nullzkey = s.zkey();
			int n = -recurse(1-player, -beta, -alpha, depth-r, pv, rootNode, stackIndex+1, s);
			s.undoNullMove();
			stack[stackIndex+1].skipNullMove = false;
			
			threatMove = n < alpha;
			
			if(n >= beta){
				if(n >= 70000){
					n = beta;
				}
				if(depth < 12){ //stockfish prunes at depth<12
					stats.nullMoveCutoffs++;
					return n;
				}
				
				stats.nullMoveVerifications++;
				//verification search
				stack[stackIndex+1].skipNullMove = true;
				double v = recurse(player, alpha, beta, depth-r, pv, rootNode, stackIndex+1, s);
				stack[stackIndex+1].skipNullMove = false;
				if(v >= beta){
					stats.nullMoveCutoffs++;
					return n;
				}
			} else if(n < alpha){
				stats.nullMoveFailLow++;
				final TTEntry nullTTEntry = m.get(nullzkey);
				if(nullTTEntry != null && nullTTEntry.move != 0){
					ml.killer[0] = nullTTEntry.move & 0xFFFL; //doesnt matter which we store to, killer is at node start
				}
			}
		} else{
			threatMove = false;
		}

		//internal iterative deepening
		if(!tteMove && depth >= (pv? 5: 8) && (pv || (!ml.kingAttacked[player] && lazyEval+256 >= beta))){
			final double d = pv? depth-2: depth/2;
			stack[stackIndex+1].skipNullMove = true;
			recurse(player, alpha, beta, d, pv, rootNode, stackIndex+1, s);
			stack[stackIndex+1].skipNullMove = false;
			final TTEntry temp;
			if((temp = m.get(zkey)) != null && temp.move != 0){
				tteMove = true;
				tteMoveEncoding = temp.move;
				ml.tempMset[w++].fill(1L<<MoveEncoder.getPos1(tteMoveEncoding),
						1L<<MoveEncoder.getPos2(tteMoveEncoding), tteMoveRank);
			}
		}

		//move generation
		ml.list.add(ml.tempMset, w);
		MoveGen.genMoves(player, s, ml.list, ml.kingAttacked[player], false);
		
		
		int g = alpha;
		long bestMove = 0;
		final int initialBestScore = -99999;
		int bestScore = initialBestScore;
		int cutoffFlag = TTEntry.CUTOFF_TYPE_UPPER;
		int moveCount = 0;
		int quietMoveCount = 0; //move count for non-take, non-check moves
		
		final int drawCount = s.drawCount; //stored for error checking
		final long pawnZkey = s.pawnZkey(); //stored for error checking
		
		boolean hasMove = ml.kingAttacked[player];
		final boolean inCheck = ml.kingAttacked[player];
		
		final MoveGen.MoveList list = ml.list;
		final int length = list.length;
		for(int i = 0; i < length; i++){
			final MoveGen.MoveSet mset = list.mset[i];
			for(long movesTemp = mset.moves; movesTemp != 0 ; movesTemp &= movesTemp-1){
				moveCount++;
				long encoding = s.executeMove(player, mset.piece, movesTemp&-movesTemp);
				this.e.processMove(encoding);
				boolean isDrawable = s.isDrawable(); //player can take a draw

				if(State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s)){
					//king in check after move
					g = -88888+stackIndex+1;
				} else{
					hasMove = true;
					
					final boolean pvMove = pv && i==0;
					final boolean isCapture = MoveEncoder.getTakenType(encoding) != State4.PIECE_TYPE_EMPTY;
					final boolean givesCheck = isChecked(1-player, s);
					final boolean isPawnPromotion = MoveEncoder.isPawnPromoted(encoding);
					final boolean isPassedPawnPush = MoveEncoder.getMovePieceType(encoding) == State4.PIECE_TYPE_PAWN &&
							(Masks.passedPawnMasks[player][MoveEncoder.getPos1(encoding)] & s.pawns[1-player]) == 0;
					final boolean isTTEMove = tteMove && encoding == tteMoveEncoding;
					final boolean isKillerMove = (encoding&0xFFF) == ml.killer[0] || (encoding&0xFFF) == ml.killer[1];
					
					final boolean isDangerous = givesCheck ||
							MoveEncoder.isCastle(encoding) != 0 ||
							isPassedPawnPush;
					
					
					final double ext = 0;//(isDangerous && pv? 1: 0) + (threatMove && pv? 0: 0);
					
					//futility pruning
					if(!pv && !isPawnPromotion &&
							!inCheck &&
							!isTTEMove &&
							!isCapture &&
							!isKillerMove &&
							!isDangerous){
						
						if(depth <= 4){
							final int mc = quietMoveCount < 64? quietMoveCount: 63;
							final int d = depth < 5? (int)depth: 4;
							final int futilityScore = lazyEval+futilityMargins[d][mc];
							if(futilityScore < beta){
								bestScore = bestScore > futilityScore? bestScore: futilityScore;
								alpha = bestScore > alpha? bestScore: alpha;
								s.undoMove();
								this.e.undoMove(encoding);
								continue;
							}
						}
					}
					//count incremented after futility so first more has move count index 0
					if(!isCapture && !isDangerous && !isTTEMove && !isKillerMove && !isPawnPromotion) quietMoveCount++;
					
					//LMR
					final boolean fullSearch;
					final int reduction;
					if(depth > 2 && !pvMove && !isCapture && !inCheck && !isPawnPromotion &&
							!isDangerous && 
							!isKillerMove &&
							!isTTEMove &&
							(reduction = lmrReduction(pv, depth, moveCount)+1) > 1){
						//int reducedDepth = pv? depth-2: depth-3;
						final double reducedDepth = depth-reduction;
						g = -recurse(1-player, -alpha-1, -alpha, reducedDepth, false, false, stackIndex+1, s);
						fullSearch = g > alpha;
					} else{
						fullSearch = true;
					}
					
					if(fullSearch){
						//descend negascout style
						final double nextDepth = depth-1+ext;
						if(!pvMove){
							g = -recurse(1-player, -(alpha+1), -alpha, nextDepth, false, false, stackIndex+1, s);
							if(alpha < g && g < beta && pv){
								g = -recurse(1-player, -beta, -alpha, nextDepth, pv, false, stackIndex+1, s);
							}
						} else{
							g = -recurse(1-player, -beta, -alpha, nextDepth, pv, false, stackIndex+1, s);
						}
					}
				}
				s.undoMove();
				this.e.undoMove(encoding);
				assert zkey == s.zkey(); //keys should be unchanged after undo
				assert drawCount == s.drawCount;
				assert pawnZkey == s.pawnZkey();
				
				if(isDrawable && 0 > g){ //can take a draw instead of making the move
					g = 0;
					encoding = 0;
				} 
				
				if(g > bestScore){
					bestScore = g;
					bestMove = encoding;
					if(g > alpha){
						alpha = g;
						cutoffFlag = TTEntry.CUTOFF_TYPE_EXACT;
						if(alpha >= beta){
							if(!cutoffSearch.get()){
								//m.put2(zkey, bestMove, alpha, depth, ZMap.CUTOFF_TYPE_LOWER);
								fillEntry.fill(zkey, encoding, alpha, (int)depth, TTEntry.CUTOFF_TYPE_LOWER, seq);
								m.put(zkey, fillEntry);
							}
							
							//check to see if killer move can be stored
							//if used on null moves, need to have a separate killer array
							if(stackIndex-1 >= 0){
								attemptKillerStore(bestMove, ml.skipNullMove, stack[stackIndex-1]);
							}
							
							return g;
						}
					}
				}
			}
		}
		
		if(!hasMove){
			//no moves except king into death - draw
			bestMove = 0;
			bestScore = 0;
			cutoffFlag = TTEntry.CUTOFF_TYPE_EXACT;
		}
		
		if(!cutoffSearch.get()){
			//m.put2(zkey, bestMove, bestScore, depth, cutoffFlag);
			fillEntry.fill(zkey, bestMove, bestScore, (int)depth, pv? cutoffFlag: TTEntry.CUTOFF_TYPE_UPPER, seq);
			m.put(zkey, fillEntry);
		}
		return bestScore;
	}
	
	private int qsearch(final int player, int alpha, int beta, final int depth,
			final int stackIndex, final boolean pv, final State4 s){
		stats.nodesSearched++;
		
		if(depth < -qply){
			stats.forcedQuietCutoffs++;
			return beta; //qply bottomed out, return bad value
		} else if(cutoffSearch.get()){
			return 0;
		}

		MoveList ml = stack[stackIndex];
		ml.list.length = 0;
		int w = 0;

		final long zkey = s.zkey();
		final TTEntry e = m.get(zkey);
		if(e != null){
			stats.hashHits++;
			if(e.depth >= depth){
				if(pv ? e.cutoffType == TTEntry.CUTOFF_TYPE_EXACT: (e.score >= beta?
						e.cutoffType == TTEntry.CUTOFF_TYPE_LOWER: e.cutoffType == TTEntry.CUTOFF_TYPE_UPPER)){
					return e.score;
				}
			}
			if(e.move != 0){
				long encoding = e.move;
				ml.tempMset[w++].fill(1L<<MoveEncoder.getPos1(encoding),
						1L<<MoveEncoder.getPos2(encoding), tteMoveRank);
			}
		}
		

		int bestScore;
		ml.kingAttacked[player] = State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s);
		if(ml.kingAttacked[player]){
			bestScore = -77777; //NOTE: THIS CONDITION WILL PROBABLY NEVER BE CHECKED
		} else{
			bestScore = this.e.eval(s, player);
			if(bestScore >= beta){ //standing pat
				return bestScore;
			} else if(bestScore > alpha && pv){
				alpha = bestScore;
			}
		}
		ml.kingAttacked[1-player] = State4.isAttacked2(BitUtil.lsbIndex(s.kings[1-player]), player, s);
		
		ml.list.add(ml.tempMset, w);
		MoveGen.genMoves(player, s, ml.list, ml.kingAttacked[player], true);

		
		int g = alpha;
		int cutoffFlag = TTEntry.CUTOFF_TYPE_UPPER;
		long bestMove = 0;
		final int drawCount = s.drawCount; //stored for error checking purposes
		final MoveGen.MoveList list = ml.list;
		final int length = list.length;
		for(int i = 0; i < length; i++){
			final MoveGen.MoveSet mset = list.mset[i];
			for(long movesTemp = mset.moves; movesTemp != 0 ; movesTemp &= movesTemp-1){
				long encoding = s.executeMove(player, mset.piece, movesTemp&-movesTemp);
				this.e.processMove(encoding);
				final boolean isDrawable = s.isDrawable();
				
				if(State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s)){
					//king in check after move
					g = -77777;
				} else{
					g = -qsearch(1-player, -beta, -alpha, depth-1, stackIndex+1, pv, s);
				}
				s.undoMove();
				this.e.undoMove(encoding);
				
				if(isDrawable && 0 > g){// && -10*depth > g){ //can draw instead of making the move
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
							if(!cutoffSearch.get()){
								fillEntry.fill(zkey, encoding, g, depth, TTEntry.CUTOFF_TYPE_LOWER, seq);
								m.put(zkey, fillEntry);
							}
							return g;
						}
					}
				}
			}
		}

		if(!cutoffSearch.get()){
			fillEntry.fill(zkey, bestMove, bestScore, depth, pv? cutoffFlag: TTEntry.CUTOFF_TYPE_UPPER, seq);
			m.put(zkey, fillEntry);
		}
		return bestScore;
	}
	

	/**
	 * checks too see if a move is legal, assumming we do not start in check,
	 * moving does not yield self check, we are not castling, and if moving a pawn
	 * we have chosen a non take move that could be legal if no piece is
	 * blocking the target square
	 * 
	 * <p> used to check that killer moves are legal
	 * @param player
	 * @param piece
	 * @param move
	 * @param s
	 * @return
	 */
	private static boolean isPseudoLegal(final int player, final long encoding, State4 s){
		final int pos1 = MoveEncoder.getPos1(encoding);
		final int pos2 = MoveEncoder.getPos2(encoding);
		final int takenType = MoveEncoder.getTakenType(encoding);
		final long p = 1L << pos1;
		final long m = 1L << pos2;
		final long[] pieces = s.pieces;
		final long agg = pieces[0] | pieces[1];
		final long allied = pieces[player];
		final long open = ~allied;
		
		if((allied & p) != 0 && takenType == s.mailbox[pos2]){
			final int type = s.mailbox[pos1];
			switch(type){
			case State4.PIECE_TYPE_BISHOP:
				final long tempBishopMoves = Masks.getRawBishopMoves(agg, p) & open;
				return (m & tempBishopMoves) != 0;
			case State4.PIECE_TYPE_KNIGHT:
				final long tempKnightMoves = Masks.getRawKnightMoves(p) & open;
				return (m & tempKnightMoves) != 0;
			case State4.PIECE_TYPE_QUEEN:
				final long tempQueenMoves = Masks.getRawQueenMoves(agg, p) & open;
				return (m & tempQueenMoves) != 0;
			case State4.PIECE_TYPE_ROOK:
				final long tempRookMoves = Masks.getRawRookMoves(agg, p) & open;
				return (m & tempRookMoves) != 0;
			case State4.PIECE_TYPE_KING:
				final long tempKingMoves = (Masks.getRawKingMoves(p) & open) | State4.getCastleMoves(player, s);
				return (m & tempKingMoves) != 0;
			case State4.PIECE_TYPE_PAWN:
				final long tempPawnMoves = Masks.getRawAggPawnMoves(player, agg, s.pawns[player]);
				return (m & tempPawnMoves) != 0;
			}
		}
		
		return false;
	}
	
	/**
	 * attempts to store a move as a killer move
	 * @param move move to be stored
	 * @param skipNullMove current skip null move status
	 * @param prev previous move list
	 */
	private static void attemptKillerStore(final long move, final boolean skipNullMove, final MoveList prev){
		assert prev != null;
		if(move != 0 && !skipNullMove &&
				(move&0xFFF) != prev.killer[0] &&
				(move&0xFFF) != prev.killer[1] &&
				MoveEncoder.getTakenType(move) == State4.PIECE_TYPE_EMPTY &&
				MoveEncoder.isEnPassanteTake(move) == 0 &&
				!MoveEncoder.isPawnPromoted(move)){
			if(prev.killer[0] != move){
				prev.killer[1] = prev.killer[0];
				prev.killer[0] = move & 0xFFF;
			} else{
				prev.killer[0] = prev.killer[1];
				prev.killer[1] = move & 0xFFF;
			}
		}
	}

	@Override
	public void setListener(SearchListener2 l) {
		this.l = l;
	}
}
