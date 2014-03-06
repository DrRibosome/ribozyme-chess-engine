package chess.uci.controlExtension;

import chess.eval.e9.E9;
import chess.uci.Position;
import chess.uci.UCIEngine;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * extension for printing the eval weights
 */
public final class EvalWeightControllerExt implements ControlExtension {
	private final E9.EvalWeights weights;
	private final Map<String, Object> m = new HashMap<>();

	public EvalWeightControllerExt(E9.EvalWeights weights){
		this.weights = weights;
		recurse(weights, m);
	}

	@Override
	public void execute(String[] args, Position pos, UCIEngine engine) {
		if(args.length > 0){
			if(args[0].equals("list")){
				listWeights(m);
			} else if(args[0].equals("set")){
				String weightClass = args[1];
				String weightName = args[2];
				String weightValue = args[3];
				set(weightClass, weightName, weightValue, m);
			}
		}
	}

	/** look up corresponding weight class, set passed weight to target value*/
	private static void set(String weightClass, String weightName, String weightValue, Map<String, Object> m){
		try{
			Object t = m.get(weightClass);
			Field[] f = t.getClass().getFields();
			for(int a = 0; a < f.length; a++){
				if(f[a].getName().equals(weightName)){
					if(f[a].getType().isPrimitive()){
						f[a].set(t, Integer.parseInt(weightValue));
					} else if(f[a].getType().isArray()){
						//expects weight value of form 'index=value'
						String[] s = weightValue.split("=");
						int index = Integer.parseInt(s[0]);
						int value = Integer.parseInt(s[1]);
						Array.set(t, index, value);
					}
					return;
				}
			}
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}

	/** traverse list of weight classes and print class names and weight names*/
	private static void listWeights(Map<String, Object> m){
		try{
			for(Map.Entry<String, Object> e: m.entrySet()){
				Object t = e.getValue();

				System.out.println(t.getClass().getName());
				Field[] f = t.getClass().getFields();
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
								System.out.print(",");
							}
						}
						System.out.println(")");
					}
				}
			}
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}

	/** recursively build weight class lookup mapping*/
	private static <T> void recurse(T t, Map<String, Object> m){
		try{
			m.put(t.getClass().getName(), t);
			Field[] f = t.getClass().getFields();
			for(int a = 0; a < f.length; a++){
				if(!f[a].getType().isPrimitive() && !f[a].getType().isArray()){
					recurse(f[a].get(t), m);
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
