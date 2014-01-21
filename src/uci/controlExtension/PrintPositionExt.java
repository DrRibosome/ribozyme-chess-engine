package uci.controlExtension;

import uci.Position;

/** extension for printing a position*/
public class PrintPositionExt implements ControlExtension {

	@Override
	public void execute(String[] args, Position pos) {
		if(pos == null){
			System.out.println("no state information");
		} else{
			System.out.println("side to move: "+pos.sideToMove);
			System.out.println(pos.s);
		}
	}
}
