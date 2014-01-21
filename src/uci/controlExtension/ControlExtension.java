package uci.controlExtension;

import uci.Position;

/** defines a means of easily extending the uci console*/
public interface ControlExtension {
	public abstract void execute(String[] args, Position pos);
}
