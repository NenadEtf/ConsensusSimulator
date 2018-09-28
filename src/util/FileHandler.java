package util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.Semaphore;

public class FileHandler {

	public static Semaphore mutex = new Semaphore(0);

	public static void putTextToFile(String fileName, String text) {
		try {
			PrintWriter printWriter = new PrintWriter(fileName);
			printWriter.write(text);
			printWriter.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public static void appendTextToFile(String fileName, String text) {
		try {
			mutex.acquire();
			PrintWriter printWriter = new PrintWriter(fileName);
			printWriter.append(text);
			printWriter.flush();
			mutex.release();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {

		}

	}

}
