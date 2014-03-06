package chess.uci.controlExtension;

import chess.eval.e9.E9;
import chess.uci.Position;
import chess.uci.UCIEngine;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/** extension for printing the eval weights*/
public final class PrintEvalWeightsExt implements ControlExtension {
	@Override
	public void execute(String command, Position pos, UCIEngine engine) {
		E9.EvalWeights weights = new E9.EvalWeights();

		recurse(weights);
	}

	private static <T> void recurse(T t){
		try{
			Field[] f = t.getClass().getFields();
			System.out.println(t.getClass().getName()+":");
			for(int a = 0; a < f.length; a++){
				if(f[a].getType().isPrimitive()){
					System.out.println(f[a].getName()+"="+f[a].getInt(t));
				} else if(f[a].getType().isArray()){
					System.out.print(f[a].getName()+"=(");
					Object array = f[a].get(t);
					int length = Array.getLength(array);
					for(int q = 0; q < length; q++){
						int value = (int)Array.get(array, q);
						System.out.print(value);
						if(q+1 != length){
							System.out.print(", ");
						}
					}
					System.out.println(")");
				} else{
					recurse(f[a].get(t));
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
