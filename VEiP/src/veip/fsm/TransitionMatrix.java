package veip.fsm;


public class TransitionMatrix{
	double[][] matrix;
	
	public TransitionMatrix(int m, int n){
		matrix = new double[m][n]; 
		for (int i = 0; i < m; i++){
			for (int j = 0; j < n; j++)
				matrix[i][j] = 0;
		}
	}
	public TransitionMatrix(double[][] matrix){
		this.matrix = matrix;
	}		
	public void addEntry(int i, int j, double value){
		matrix[i][j] = value;
	}
	public double getEntry(int i, int j){
		return matrix[i][j];
	}
}
