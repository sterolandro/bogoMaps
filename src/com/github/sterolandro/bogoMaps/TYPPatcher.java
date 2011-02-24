package com.github.sterolandro.bogoMaps;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class TYPPatcher {

	// FID -> Family ID
	// PID -> Product ID
	private static final int FID_START = Integer.parseInt("2F", 16);
	private static final int FID_LEN = 2;
	private static int fid_arr[] = { 0, 0 };

	private static final int PID_START = Integer.parseInt("31", 16);
	private static final int PID_LEN = 2;
	private static int pid_arr[] = { 0, 0 };

	private static File i_file;
	private static File o_file;

	private static String lpad(String expr, int length, char fillChar) {
		int qtyToAdd = length - expr.length();
		if (qtyToAdd <= 0) {
			return expr;
		}
		char filler[] = new char[qtyToAdd];
		Arrays.fill(filler, fillChar);
		StringBuffer sb = new StringBuffer(length);
		sb.append(filler);
		sb.append(expr);
		return sb.toString();
	}

	private static void writePatch(File i_file, File o_file, int fid_patch,
			int pid_patch) {
		int i_data = 0;
		String fid_found = "";
		String pid_found = "";
		int iter = 0;

		try {
			FileInputStream file_input = new FileInputStream(i_file);
			DataInputStream data_in = new DataInputStream(file_input);
			FileOutputStream file_output = new FileOutputStream(o_file);
			DataOutputStream data_out = new DataOutputStream(file_output);

			while (true) {
				try {
					i_data = data_in.read();
					if (i_data == -1) {
						data_out.close();
						break;
					}
				} catch (EOFException eof) {
					data_out.close();
					break;
				}

				if ((iter >= FID_START) && (iter < FID_START + FID_LEN)) {
					fid_found = fid_found
							+ lpad(String.format("%1$X", i_data), 2, '0');
					if (iter == FID_START) {
						data_out.write(fid_arr[0]);
					} else {
						data_out.write(fid_arr[1]);
					}
				} else if ((iter >= PID_START) && (iter < PID_START + PID_LEN)) {
					pid_found = pid_found
							+ lpad(String.format("%1$X", i_data), 2, '0');
					if (iter == PID_START) {
						data_out.write(pid_arr[0]);
					} else {
						data_out.write(pid_arr[1]);
					}
				} else {
					data_out.write(i_data);
				}
				iter++;
			}
			data_in.close();

			System.out.println("Found:   "
					+ lpad(Integer.toString(Integer.valueOf(fid_found, 16)
							.intValue()), 5, ' ')
					+ " - "
					+ lpad(Integer.toString(Integer.valueOf(pid_found, 16)
							.intValue()), 5, ' ') + " (0x" + fid_found
					+ " - 0x" + pid_found + ")");
			System.out.println("Written: "
					+ lpad(Integer.toString(fid_patch), 5, ' ') + " - "
					+ lpad(Integer.toString(pid_patch), 5, ' ') + " (0x"
					+ lpad(String.format("%1$X", fid_arr[0]), 2, '0')
					+ lpad(String.format("%1$X", fid_arr[1]), 2, '0') + " - 0x"
					+ lpad(String.format("%1$X", pid_arr[0]), 2, '0')
					+ lpad(String.format("%1$X", pid_arr[1]), 2, '0') + " )");

		} catch (IOException e) {
			System.out.println("IO Exception =: " + e);
		}
	}

	private static void setParams(String i_sfile, String o_sfile,
			int fid_patch, int pid_patch) {
		String sfid_patch = lpad(
				Integer.toHexString(Integer.valueOf(fid_patch)), 4, '0');
		String spid_patch = lpad(
				Integer.toHexString(Integer.valueOf(pid_patch)), 4, '0');
		i_file = new File(i_sfile);
		o_file = new File(o_sfile);

		fid_arr[0] = Integer.parseInt(sfid_patch.substring(0, 2).trim(), 16);
		fid_arr[1] = Integer.parseInt(sfid_patch.substring(2).trim(), 16);
		pid_arr[0] = Integer.parseInt(spid_patch.substring(0, 2).trim(), 16);
		pid_arr[1] = Integer.parseInt(spid_patch.substring(2).trim(), 16);
	}

	public static void patchFile(String i_sfile, String o_sfile, int fid_patch,
			int pid_patch) throws InvalidParameterException {
		setParams(i_sfile, o_sfile, fid_patch, pid_patch);
		writePatch(i_file, o_file, fid_patch, pid_patch);
	}

}
