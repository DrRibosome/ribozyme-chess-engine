package chess.search.search34.pipeline;

import chess.eval.Evaluator;
import chess.eval.e9.pipeline.EvalResult;
import chess.search.search34.Hash;
import chess.search.search34.Search34;
import chess.search.search34.StackFrame;
import chess.search.search34.TTEntry;
import chess.search.search34.moveGen.MoveList;
import chess.state4.Masks;
import chess.state4.State4;

public final class HashLookupStage implements EntryStage {

	private final StackFrame[] stack;
	private final Hash hash;
	private final Evaluator evaluator;
	private final MidStage next;

	public HashLookupStage(StackFrame[] stack, Hash hash, Evaluator evaluator, MidStage next){
		this.stack = stack;
		this.hash = hash;
		this.evaluator = evaluator;
		this.next = next;
	}

	@Override
	public int eval(SearchContext c, State4 s) {
		final StackFrame frame = stack[c.stackIndex];
		final MoveList list = frame.mlist;
		list.clear();

		frame.killer[0] = 0;
		frame.killer[1] = 0;

		final long zkey = s.zkey();
		final TTEntry hashEntry = hash.get(zkey);
		boolean tteMove = false;
		long tteMoveEncoding = 0;

		//query hash for stored node results
		final EvalResult staticScore;
		if(hashEntry != null){
			if(hashEntry.depth >= c.depth){
				final int cutoffType = hashEntry.cutoffType;
				if(c.nt == SearchContext.NODE_TYPE_PV? cutoffType == TTEntry.CUTOFF_TYPE_EXACT: (hashEntry.score >= c.beta?
						cutoffType == TTEntry.CUTOFF_TYPE_LOWER: cutoffType == TTEntry.CUTOFF_TYPE_UPPER)){

					if(c.stackIndex-1 >= 0 && hashEntry.score >= c.beta){
						Search34.attemptKillerStore(hashEntry.move, c.skipNullMove, stack[c.stackIndex-1]);
					}

					if(c.nt != SearchContext.NODE_TYPE_PV){
						return hashEntry.score;
					}
				}
			}

			if(hashEntry.move != 0){
				tteMoveEncoding = hashEntry.move;
				//tte move added to move list in descent stage
				tteMove = true;
			}

			staticScore = evaluator.refine(c.player, s, c.alpha, c.beta, hashEntry.staticEval);
		} else{
			staticScore = evaluator.eval(c.player, s, c.alpha, c.beta);
		}

		//construct node static eval score
		final int eval;
		if(hashEntry != null && ((hashEntry.cutoffType == TTEntry.CUTOFF_TYPE_LOWER &&
				hashEntry.score > staticScore.score) ||
				(hashEntry.cutoffType == TTEntry.CUTOFF_TYPE_UPPER &&
				hashEntry.score < staticScore.score))){
			eval = hashEntry.score;
		} else{
			eval = staticScore.score;
		}

		//calculate node properties for reuse in later sections
		final boolean alliedKingAttacked = Search34.isChecked(c.player, s);
		final boolean pawnPrePromotion = (s.pawns[c.player] & Masks.pawnPrePromote[c.player]) != 0;
		final boolean hasNonPawnMaterial = s.pieceCounts[c.player][0]-s.pieceCounts[c.player][State4.PIECE_TYPE_PAWN] > 1;
		final boolean nonMateScore = Math.abs(c.beta) < 70000 && Math.abs(c.alpha) < 70000;

		return next.eval(c,
				new NodeProps(zkey, eval, staticScore, alliedKingAttacked,
						pawnPrePromotion, hasNonPawnMaterial, nonMateScore,
						tteMove, tteMoveEncoding),
				s);
	}
}
