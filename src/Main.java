/* Group Members
 * 
 * Upendra Govindagowda (uxg140230)
 * Ankur Gupta (axg156130)
 * Sarvotam Pal Singh (sxs155032) 
 * 
 * */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Main {
	
	public static void main(String[] args) {
		
		String filename = "input_10.dat";
		if(args.length != 0) {
			
			filename = args[0];
		}
		
		int n;
		int[] ids;
		int[][] matrix;
		
		try {
			File file = new File(filename);		
			BufferedReader br = new BufferedReader(new FileReader(file));
			String n_str = br.readLine();
			String ids_str = br.readLine();

			//parse read strings to integers 
			n = Integer.parseInt(n_str);
			String[] id_array_str = ids_str.split(" ");
			ids = new int[n];
			matrix = new int[n][n];
			
			for (int i=0; i< n; i++) {
				ids[i] = Integer.parseInt(id_array_str[i]);
			} 
			
			for (int i=0; i< n; i++) {
				String row_str = br.readLine();
				String[] row = row_str.split(" ");
				for (int j=0; j< n; j++) {
					matrix[i][j] = Integer.parseInt(row[j]);
				}
			}
			
			br.close();
		} catch(FileNotFoundException f) {
			System.out.print(f.getMessage());
			return;
		} catch(IOException i) {
			System.out.print(i.getMessage());
			return;
		}
		
		
		// random adjacency matrix
		
		// Initialize n processes		
		Runnable[] processes = new Process[n];
		for (int i = 0; i < processes.length; i++) {
			processes[i] = new Process(ids[i]);
		}
		for (int i = 0; i < processes.length; i++) {
			Process p = (Process) processes[i];
			for (int j = 0; j < processes.length; j++) {
				if (matrix[i][j] == 1)
					p.addNeighbor((Process)processes[j]);
			}
		}

		// Start n threads
		Thread[] threads = new Thread[n];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(processes[i]);
			threads[i].start();
		}

		while (true) {
			// Perform termination checks
			boolean isAnyThreadRunning = false;
			for (Thread thread : threads) {
				if (thread.isAlive()) {
					isAnyThreadRunning = true;
					break;
				}
			}
			// finish computation if all threads have terminated
			if (!isAnyThreadRunning) {
				System.out.println("[Main]: Leader election completed!");
				break;
			}
			
			// Ask processes to start their rounds
			for (Runnable p : processes) {
				Process proc = (Process)p;
				proc.setCanStartRound(true);
			}			

			// Wait for all processes to read their current buffer values
			boolean isAnyProcessStillReading;
			while (true) {
				isAnyProcessStillReading = false;
				for (Runnable p : processes) {
					Process proc = (Process)p;
					if (proc.isCanStartRound() && !proc.isTerminated()) {
						isAnyProcessStillReading = true;
						break;
					}
				}
				// marks the end of corresponding round in all processes
				if (!isAnyProcessStillReading)
					break;
			}
			
			for (Runnable p : processes) {
				Process proc = (Process)p;
				proc.setCanStartRound(true);
			}
			
			// Wait for all processes to complete the round
			boolean isAnyRoundActive;
			while (true) {
				isAnyRoundActive = false;
				for (Runnable p : processes) {
					Process proc = (Process)p;
					if (proc.isCanStartRound() && !proc.isTerminated()) {
						isAnyRoundActive = true;
						break;
					}
				}
				// marks the end of corresponding round in all processes
				if (!isAnyRoundActive)
					break;
			}
		}
		
	}
	
	public static int[][] convert(  int[] array,  int rows,  int cols ) {
	    if (array.length != (rows*cols))
	        throw new IllegalArgumentException("Invalid array length");

	    int[][] new_arr = new int[rows][cols];
	    for ( int i = 0; i < rows; i++ ) {
	        System.arraycopy(array, (i*cols), new_arr[i], 0, cols);
	    	//display(new_arr,rows); (uncomment to check output)
		}
    	return new_arr;
    	
	}


	//extra function to display output
	public static void display(int[][] array, int num){
		
		for(int i=0;i<num;i++){
			for(int j=0;j<num;j++){
				System.out.print(array[i][j]);
			}
		}
	}
}
