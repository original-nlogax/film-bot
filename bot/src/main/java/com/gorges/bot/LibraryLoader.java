package com.gorges.bot;

import java.io.*;


public class LibraryLoader {

	public static void loadLibrary(String name) throws IOException {
		try {
			System.loadLibrary(name);
		} catch (UnsatisfiedLinkError e) {
			String filename = System.mapLibraryName(name);
			InputStream in = LibraryLoader.class.getClassLoader().getResourceAsStream(filename);
			int pos = filename.lastIndexOf('.');
			File file = File.createTempFile(filename.substring(0, pos), filename.substring(pos));
			file.deleteOnExit();
			try {
				byte[] buf = new byte[4096];
				OutputStream out = new FileOutputStream(file);
				try {
					while (in.available() > 0) {
						int len = in.read(buf);
						if (len >= 0) {
							out.write(buf, 0, len);
						}
					}
				} finally {
					out.close();
				}
			} finally {
				in.close();
			}
			System.load(file.getAbsolutePath());
			//System.out.println("Successfully loaded the library " + file.getAbsolutePath());
		}
	}
}
