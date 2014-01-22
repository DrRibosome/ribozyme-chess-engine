package chess.search.search34.pipeline;

public class SearchContext{
	public final int player, alpha, beta, depth, stackIndex;
	public final NodeType nt;
	public final boolean skipNullMove;

	public SearchContext(int player, int alpha, int beta, int depth, NodeType nt, int stackIndex, boolean skipNullMove){
		this.player = player;
		this.alpha = alpha;
		this.beta = beta;
		this.depth = depth;
		this.nt = nt;
		this.stackIndex = stackIndex;
		this.skipNullMove = skipNullMove;
	}

	public SearchContext(int player, int alpha, int beta, int depth, NodeType nt, int stackIndex){
		this(player, alpha, beta, depth, nt, stackIndex, false);
	}
}
