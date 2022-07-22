import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

	public final static int PORT = 8080;
	public final static int ThreadSize = 7;

	public static void main(String[] args) throws IOException {
		// create server
		ServerSocket server = new ServerSocket(PORT);
		// create a threadpool to handle all the connections later
		ExecutorService pool = Executors.newFixedThreadPool(ThreadSize);

		System.out.println("Server started on port " + PORT + " at " + server.getInetAddress().getHostAddress());

		// listens for requests from clients
		while (true) {
			try {
				// creates connection
				Socket newSocket = server.accept();
				// creates a new thread for the client
				ServerThread thread = new ServerThread(newSocket);
				// starts the thread
				pool.execute(thread);
				// prints out the number of active threads
				int threads = 0;
				for (Thread t : Thread.getAllStackTraces().keySet()) {
					if (t.getState() == Thread.State.RUNNABLE)
						threads++;
				}
				System.out.println("Pool size: " + ThreadSize);
				System.out.println("Using " + threads);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
