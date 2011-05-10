package sncatalog.shared;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

public class Serializer {
	
	static final String HEXES = "0123456789ABCDEF";
	
	public static byte[] byteSerialize(Object o) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
		ObjectOutputStream os = new ObjectOutputStream(stream);
		
		os.writeObject(o);
		os.close();
		return stream.toByteArray();
	}
	
	public static String serialize(Object o) throws IOException {
		return toHex(byteSerialize(o));
	}
	
	public static Object deserialize(String hexString)  throws IOException, ClassNotFoundException {
		byte[] serializedBytes = toByteArray(hexString);
		return deserialize(serializedBytes);
	}
	
	public static Object deserialize(File file) throws StreamCorruptedException, IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(file);
		byte fileContent[] = new byte[(int)file.length()];
		fis.read(fileContent);
		return deserialize(fileContent);
	}
	
	public static Object deserialize(byte[] bytes) throws StreamCorruptedException, IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bis); 
		
		Object o = null;
		o = ois.readObject();
		ois.close(); 
		return o;
	}
	
	public static String toHex( byte [] raw ) {
		if ( raw == null ) {
			return null;
		}
		final StringBuilder hex = new StringBuilder( 2 * raw.length );
		for ( final byte b : raw ) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4))
			.append(HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}
	
	private static byte[] toByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}

}
