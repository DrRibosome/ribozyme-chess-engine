package uci.controlExtension;

import uci.Position;
import uci.UCIEngine;

/** defines a means of easily extending the uci console*/
public interface ControlExtension {
	public abstract void execute(String command, Position pos, UCIEngine engine);
}
