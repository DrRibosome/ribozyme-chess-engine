package uci;

/** defines common interface for engines to work with UCI protocol*/
public interface UCIEngine {
	/** start calculating for given position*/
	public void go(GoParams params);
	/** stop calculating for given position*/
	public void stop();
	public void setPos(Position p);
}
