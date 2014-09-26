package mainPackage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.LMClassifier;

/*
 Βοηθητική κλάση για de/serialization δεδομένων από και προς το δίσκο.
 */
public class SerializationUtil implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("rawtypes")
	public static Object deserialize(String fileName) throws IOException, ClassNotFoundException
	{
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis);
		if (fileName.contains("classifier.dat"))
		{
			LMClassifier readClassifier;
			readClassifier = (LMClassifier)(ois.readObject());
			ois.close();
			fis.close();
			return readClassifier;
		}
		else
		{
			Object obj = ois.readObject();
			ois.close();
			fis.close();
			return obj;
		}
	}

	@SuppressWarnings("rawtypes")
	public static void serialize(Object obj, String fileName) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(fileName);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		if (fileName.contains("classifier.dat"))
		{
			DynamicLMClassifier obj2 = (DynamicLMClassifier) obj;
			obj2.compileTo(oos);
		}
		else
			oos.writeObject(obj);
		oos.close();
		fos.close();
	}

}
