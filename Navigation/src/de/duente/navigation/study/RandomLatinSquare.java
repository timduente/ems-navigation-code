package de.duente.navigation.study;
public class RandomLatinSquare {
	int[] latinSquare;
	int n;

	public RandomLatinSquare(int n) {
		this.n = n;
		latinSquare = new int[n * n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				latinSquare[i * n + j] = (j+i)% n;
			}
		}
	}
	
	public int[] randomize(int iterationCount){
		int col1, col2, row1, row2;
		for(int i = 0; i < iterationCount; i++){
			
			col1 = (int) (Math.random() * n);
			do{
			col2 = (int)(Math.random() * n);
			}
			while(col2 == col1);
			swapColumns(col1, col2);
			
			row1 = (int) (Math.random() * n);
			do{
			row2 = (int) (Math.random() * n);
			}
			while(row2 == row1);
			
			swapRows(row1, row2);
		}
		return latinSquare;
	}

	private void swapRows(int row1, int row2) {
		for(int j = 0; j<n; j++){
			swap(row1 * n + j, row2 * n+ j);
		}
	}

	private void swapColumns(int column1, int column2) {
		for(int i = 0; i<n; i++){
			swap(i * n + column1,i * n + column2);
		}
	}
	
	private void swap(int index1, int index2){
		int temp = latinSquare[index1];
		latinSquare[index1] = latinSquare[index2];
		latinSquare[index2] = temp;
	}

	public static void main(String... args) {
		System.out.println("Hello World!");
		RandomLatinSquare rl = new RandomLatinSquare(8);
		System.out.println(rl.toString());
		System.out.println("Swap Row1 Row4");
		rl.swapRows(0, 3);
		System.out.println(rl);
		
		rl = new RandomLatinSquare(8);
		System.out.println(rl);
		System.out.println("Swap Col1 Col4");
		rl.swapColumns(0, 3);
		System.out.println(rl);
		
		rl = new RandomLatinSquare(8);
		System.out.println(rl);
		System.out.println("Randomize. 10 Iterations");
		rl.randomize(10);
		System.out.println(rl);
		
	}
	
	@Override
	public String toString(){
		String stringRepresentation = new String();
		for (int i = 0; i < n; i++) {
			stringRepresentation = stringRepresentation + "| ";
			for (int j = 0; j < n; j++) {
				stringRepresentation = stringRepresentation + " " + latinSquare[i * n + j];
			}
			stringRepresentation = stringRepresentation + " |\n";
		}
		return stringRepresentation;
	}

}
