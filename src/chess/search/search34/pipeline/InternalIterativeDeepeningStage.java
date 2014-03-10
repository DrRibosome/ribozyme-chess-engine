package chess.search.search34.pipeline;

import chess.search.search34.Hash;
import chess.search.search34.Search34;
import chess.search.search34.StackFrame;
import chess.search.search34.TTEntry;
import chess.state4.State4;

/** internal iterative deepening*/
public final class InternalIterativeDeepeningStage implements MidStage {

	private final Hash m;
	private final Search34 searcher;
	private final MidStage next;
	private final StackFrame[] stack;

	public InternalIterativeDeepeningStage(Hash m, StackFrame[] stack, Search34 searcher, MidStage next){
		this.m = m;
		this.searcher = searcher;
		this.next = next;
		this.stack = stack;
	}

	@Override
	public int eval(int player, int alpha, int beta, int depth, int nt, int stackIndex, State4 s) {

		StackFrame frame = stack[stackIndex];

		if(!frame.hasTTMove && frame.nonMateScore &&
				depth > 6*Search34.ONE_PLY &&
				nt == SearchContext.NODE_TYPE_PV){
			final int d = depth/2;
			stack[stackIndex+1].skipNullMove = true;
			searcher.recurse(player, alpha, beta, d, nt, stackIndex + 1, s);
			stack[stackIndex+1].skipNullMove = false;

			long move = stack[stackIndex+1].bestMove;
			if(move != 0){
				frame.hasTTMove = true;
				frame.tteMoveEncoding = move;
			}
		}

		return next.eval(player, alpha, beta, depth, nt, stackIndex, s);
	}
}
