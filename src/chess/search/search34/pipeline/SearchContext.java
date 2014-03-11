package chess.search.search34.pipeline;

public final class SearchContext{

	public final static int NODE_TYPE_PV = 2;
	public final static int NODE_TYPE_CUT = 1;
	public final static int NODE_TYPE_ALL = 0;

	public static int nextNodeType(final int nodeType){
		return (nodeType+1) % 2;
	}
}
