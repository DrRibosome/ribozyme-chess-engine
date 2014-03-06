package chess.uci.controlExtension;

import chess.eval.e9.E9;
import chess.uci.Position;
import chess.uci.UCIEngine;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/** extension for printing the eval weights*/
public final class PrintEvalWeightsExt implements ControlExtension {
	private final E9.EvalWeights weights;

	public PrintEvalWeightsExt(E9.EvalWeights weights){
		this.weights = weights;
	}

	@Override
	public void execute(String[] args, Position pos, UCIEngine engine) {
		if(args.length > 0){
			if(args[0].equals("list")){
				recurse(weights);
			}
		}
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
