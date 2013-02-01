package uci;

public class GoParams {
	boolean ponder = false;
	int whiteTime = -1;
	int blackTime = -1;
	/** white time increment per move*/
	int whiteTimeInc = -1;
	/** black time increment per move*/
	int blackTimeInc = -1;
	/** max time per move*/
	int moveTime = -1;
	/** max search depth*/
	int depth = -1;
	/** search until stop command received*/
	boolean infinite = false;
}
