package eval.testEval2;


/** simple 2 layer neural network*/
public class EvalNetwork {
	private final double[] input;
	public final double[][] hiddenWeights;
	public final double[] outputWeights;
	private WeightSystem prev;
	
	public static class WeightSystem{
		private final double[][] hiddenWeights;
		private final double[] outputWeights;
		private WeightSystem(int inputNodes, int hiddenNodes){
			this.hiddenWeights = new double[hiddenNodes][inputNodes];
			this.outputWeights = new double[hiddenNodes+1];
		}
	}
	
	public EvalNetwork(double[] input, int hiddenNodes){
		this.input = input;
		this.hiddenWeights = new double[hiddenNodes][input.length];
		this.outputWeights = new double[hiddenNodes+1];
	}
	
	public void initializeWeights(){
		for(int a = 0; a < hiddenWeights.length; a++){
			for(int q = 0; q < input.length; q++){
				hiddenWeights[a][q] = Math.random()-.5;
			}
			outputWeights[a] = Math.random()-.5;
		}
		outputWeights[outputWeights.length-1] = Math.random()-.5;
	}
	
	public double eval(){
		double sum = 0;
		for(int a = 0; a < hiddenWeights.length; a++){
			double temp = 0;
			for(int i = 0; i < input.length; i++){
				temp += hiddenWeights[a][i]*input[i];
			}
			//sum += Math.tanh(temp)*outputWeights[a];
			//sum += tanhApprox(temp)*outputWeights[a];
			//sum += logistic(temp)*outputWeights[a];
			sum += elliotActivation(temp)*outputWeights[a];
		}
		sum += outputWeights[outputWeights.length-1]; //bias unit
		return sum;
	}
	
	/** http://www.heatonresearch.com/wiki/Elliott_Activation_Function*/
	private static double elliotActivation(double x){
		//return x/(1+Math.abs(x));
		return Math.tanh(x);
		//return x;
	}
	
	/** derivative of elliot activation function*/
	private static double elliotActivationD(double x){
		/*final double z = Math.abs(x)+1;
		return Math.signum(x)/(z*z);*/
		return 1-Math.pow(Math.tanh(x), 2);
		//return 1;
	}
	
	public WeightSystem derrivative(double expected){
		double[] activations = new double[hiddenWeights.length];
		double[] preActivations = new double[hiddenWeights.length];
		double eval = 0;
		for(int a = 0; a < hiddenWeights.length; a++){
			double sum = 0;
			for(int i = 0; i < input.length; i++){
				sum += hiddenWeights[a][i]*input[i];
			}
			preActivations[a] = sum;
			activations[a] = elliotActivation(sum);
			eval += activations[a]*outputWeights[a];
		}
		eval += outputWeights[outputWeights.length-1]; //bias unit
		
		double error = eval-expected;
		//System.out.println("error = "+error);
		
		//compute deltas
		WeightSystem w = new WeightSystem(input.length, hiddenWeights.length);
		for(int a = 0; a < hiddenWeights.length; a++){
			for(int i = 0; i < input.length; i++){
				w.hiddenWeights[a][i] = elliotActivationD(preActivations[a])*
						outputWeights[a]*error*input[i];
			}
			w.outputWeights[a] = activations[a]*error;
			//System.out.print(w.outputWeights[a]+", ");
		}
		//System.out.println();
		w.outputWeights[outputWeights.length-1] = error; //bias unit
		
		/*if(prev != null){ //gradient momentum
			accumulate(w, prev, .1);
		}
		prev = w;*/
		return w;
	}
	
	private static void accumulate(WeightSystem target, WeightSystem src, double scale){
		for(int a = 0; a < target.hiddenWeights.length; a++){
			for(int i = 0; i < target.hiddenWeights[0].length; i++){
				target.hiddenWeights[a][i] += src.hiddenWeights[a][i]*scale;
			}
			target.outputWeights[a] += src.outputWeights[a]*scale;
		}
	}
	
	public void train(WeightSystem w, double learningRate){
		for(int a = 0; a < hiddenWeights.length; a++){
			for(int i = 0; i < input.length; i++){
				hiddenWeights[a][i] -= w.hiddenWeights[a][i]*learningRate;
			}
			outputWeights[a] -= w.outputWeights[a]*learningRate;
		}
	}
}
