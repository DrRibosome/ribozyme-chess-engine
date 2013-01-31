package eval.testEval2;

import javax.rmi.CORBA.Tie;

import util.board4.State4;
import eval.Evaluator2;
import eval.SuperEvalS4V8;

public class EvalNetworkTest {
	public static void main(String[] args){
		/*double x = -593;
		System.out.println(elliotActivation(x));
		System.out.println(elliotActivationD(x));*/

		testTrain3();
		//testTrain2();
		//testTrain();
		//timeEval();
		//timeOldEval();
		
	}
	
	private static double elliotActivation(double x){
		return x/(1+Math.abs(x));
		//return Math.tanh(x);
	}
	
	private static double elliotActivationD(double x){
		final double z = Math.abs(x)+1;
		return Math.signum(x)/(z*z);
	}
	
	private static void testTrain3(){
		double[] inputs = new double[4];
		inputs[inputs.length-1] = 1;
		EvalNetwork n = new EvalNetwork(inputs, 10);
		n.initializeWeights();
		
		
		System.out.println("initial eval = "+n.eval());

		double learningRate = .001;
		final int testCases = 10;
		for(int w = 0; w < 999; w++){
			for(int a = 1; a <= testCases; a++){
				//inputs[0] = a;
				genInput(a, inputs);
				
				double target = target(inputs);

				n.train(n.derrivative(target), learningRate);

				//System.out.println("eval("+a+") = "+n.eval());
				/*if(w % 10000 == 0)
					System.out.println("input="+inputs[0]+", "+before+" -> "+n.eval());*/
			}
			
		}
		/*for(int a = 1; a <= 300 && !done; a++){
			inputs[0] = a;
			
			double target = target(inputs);
			
			System.out.println(a+", "+n.eval());
		}*/
		
		System.out.println("avg sqrd error = "+validate(n, inputs, 100));
	}
	
	private static double validate(EvalNetwork n, double[] inputs, int testCases){
		double error = 0; //squared error
		/*for(int w = 0; w < testCases; w++){
			inputs[0] = (int)(Math.random()*300);
			double target = target(inputs);
			error += Math.pow(target-n.eval(), 2);
		}
		return error/2/testCases;*/
		
		for(int a = 1; a <= testCases; a++){
			//inputs[0] = a;
			genInput(a, inputs);
			
			double target = target(inputs);
			
			String ins = "";
			for(int q = 0; q < inputs.length; q++){
				ins += inputs[q];
				if(q != inputs.length-1)
					ins += ", ";
			}
			
			System.out.println("eval("+ins+") = "+n.eval());
			
			error += Math.pow(target-n.eval(), 2);
		}
		return error/testCases/2;
	}
	
	private static void genInput(int q, double[] input){
		for(int a = 0; a < input.length-1; a++){
			//input[a] = q+a;
			input[a] = (int)(Math.random()*20);
		}
		for(int a = 0; a < input.length-1; a++){
			int index = (int)(Math.random()*(input.length-1));
			double temp = input[index];
			input[index] = input[a];
			input[a] = temp;
		}
	}
	
	private static double target(double[] input){
		/*double max = 0;
		for(int a = 0; a < input.length-1; a++){
			if(a == 0 || input[a] > max){
				max = input[a];
			}
		}
		return max;*/
		/*double sum = 0;
		for(int a = 0; a < input.length-1; a++){
			sum += input[a]*2+4;
		}
		return sum;*/
		if(input[0] >= 0 && input[0] <= 5){
			return 0;
		}
		return input[2];
		
		
		//return input[0]*input[0]+1;
		//return input[0]*2;
		//return 5;
		//return input[0]*2+5+input[0]*input[0];
	}
}
