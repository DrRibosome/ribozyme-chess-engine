package uci;

public class GoParams {
	boolean ponder = false;
	final int[] time = new int[]{-1,-1};
	/** time increment per move*/
	final int[] increment = new int[2];
	/** max time per move*/
	int moveTime = -1;
	/** max search depth*/
	int depth = -1;
	/** search until stop command received*/
	boolean infinite = false;
}
