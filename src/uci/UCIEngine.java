package uci;

/** defines common interface for engines to work with UCI protocol*/
public interface UCIEngine {
	/** start calculating for given position*/
	public void go(GoParams params, Position p);
	/** stop calculating for given position*/
	public void stop();
	/** ready engine for new game (ie, clear hash, etc)*/
	public void resetEngine();
	public String getName();
}
