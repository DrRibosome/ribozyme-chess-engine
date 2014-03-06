package chess.uci.controlExtension;

import chess.uci.Position;
import chess.uci.UCIEngine;

/** defines a means of easily extending the chess.uci console*/
public interface ControlExtension {
	public abstract void execute(String[] args, Position pos, UCIEngine engine);
}
