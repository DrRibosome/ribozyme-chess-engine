package chess.search.search34.pipeline;

public final class SearchContext{

	public final static int NODE_TYPE_PV = 2;
	public final static int NODE_TYPE_CUT = 1;
	public final static int NODE_TYPE_ALL = 0;

	public final int player, alpha, beta, depth, stackIndex;
	public final int nt;

	public SearchContext(int player, int alpha, int beta, int depth, int nt, int stackIndex){
		this.player = player;
		this.alpha = alpha;
		this.beta = beta;
		this.depth = depth;
		this.nt = nt;
		this.stackIndex = stackIndex;
	}

	public static int nextNodeType(final int nodeType){
		return (nodeType+1) % 2;
	}
}
