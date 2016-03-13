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
import java.util.ArrayList;
import java.util.Collections;

public class Main {
	
	public static void main(String[] args) {
		
		String filename = "input.dat";
		if(args.length != 0) {
			filename = args[0];
		}
		String n_str = "0";
		String ids_str = "0";
		
		try {
			File file = new File(filename);		
			BufferedReader br = new BufferedReader(new FileReader(file));
			n_str = br.readLine();
			ids_str = br.readLine();
			br.close();
		} catch(FileNotFoundException f) {
			System.out.print(f.getMessage());
			return;
		} catch(IOException i) {
			System.out.print(i.getMessage());
			return;
		}
		
		//parse read strings to integers 
		Integer n = Integer.parseInt(n_str);
		String[] id_array_str = ids_str.split(" ");
		int[] ids = new int[n];
		for (int i=0; i< n; i++) {
			ids[i] = Integer.parseInt(id_array_str[i]);
		}		
		
		// hard coded test values
		n = 7;
		
		// random ids
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 1; i < 1000; i++) {
			list.add(i);
		}
		//Collections.shuffle(list);
		
		for (int i=0; i < n; i++) {
			ids[i] = list.get(i);
		}
		
		// random adjacency matrix
		int[][] matrix = new int[n][n];
		ArrayList<Integer> zeroOnes = new ArrayList<Integer>(n*n);
		for (int i =0; i < (n*n)/2; ++i)
			zeroOnes.add(0);
		for (int i =(n*n)/2; i < (n*n); ++i)
			zeroOnes.add(1);
		Collections.shuffle(zeroOnes);
		
		int index = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i; j < n; j++) {
				matrix[i][j] = matrix[j][i] = zeroOnes.get(index++); 
			}
			matrix[i][i] = 0;
		}

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				System.out.print(matrix[i][j]);
			}
			System.out.println();
		}

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
}
