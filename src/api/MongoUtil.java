package api;

import java.net.UnknownHostException;

import com.mongodb.MongoClient;

public class MongoUtil {
	private static MongoClient mongo = null;
	
	public static synchronized MongoClient getMongo() {
		if (mongo == null) {
			try {
				System.out.println("Initializing a new MongoClient.");
				mongo = new MongoClient("localhost");
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		return mongo;
	}
	
	public static synchronized void closeMongo() {
		if (mongo != null) mongo.close();
		mongo = null;
		System.out.println("Closed the MongoClient.");
	}
}
