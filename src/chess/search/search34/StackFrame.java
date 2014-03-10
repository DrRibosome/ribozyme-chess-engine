package chess.search.search34;

import chess.search.search34.moveGen.MoveList;

public class StackFrame {
	private final static int defSize = 128;

	public final MoveList mlist = new MoveList(defSize);
	/** holds killer moves as first 12 bits (ie, masked 0xFFF) of move encoding*/
	public final long[] killer = new long[2];
	public boolean skipNullMove;
}
