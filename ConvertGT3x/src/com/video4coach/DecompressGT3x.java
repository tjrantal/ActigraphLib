package com.video4coach;

import java.io.*;
import java.util.zip.*;

public class DecompressGT3x {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("usage: java DecompressGT3x .gt3xFilePathAndName targetPathIncludingTrailingSeparator");
			return;
		}
		// Extract the activity file
		unzipActivityFile(args);
	}



	private static void log(String text) {
		System.out.println(text);
	}

	private static boolean unzipActivityFile(String[] args) {

		try {

			// Take the filename from the input arguments
			FileInputStream fis = new FileInputStream(args[0]);

			//
			// Creating input stream that also maintains the checksum of the
			// data which later can be used to validate data integrity.
			//
			CheckedInputStream checksum = new CheckedInputStream(fis,
					new Adler32());
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(
					checksum));
			ZipEntry entry;

			//
			// Read each entry from the ZipInputStream until no more entry found
			// indicated by a null return value of the getNextEntry() method.
			//
			while ((entry = zis.getNextEntry()) != null) {
				System.out.println("Unzipping: " + entry.getName());

				if (! (entry.getName().equalsIgnoreCase("activity.bin") == true ||
						entry.getName().equalsIgnoreCase("log.bin") == true ||
						entry.getName().equals("info.txt") == true) )
					continue;

				int size;
				byte[] buffer = new byte[2048];

				FileOutputStream fos = new FileOutputStream(args[1]+entry.getName());
				BufferedOutputStream bos = new BufferedOutputStream(fos,
						buffer.length);

				while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
					bos.write(buffer, 0, size);
				}
				bos.flush();
				bos.close();
			}

			zis.close();
			fis.close();

			//
			// Print out the checksum value
			//
			System.out.println("Checksum = "
					+ checksum.getChecksum().getValue());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

}
