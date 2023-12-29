package bit.minisys.minicc;

public class BITMiniCC {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		if (args.length < 1) {
			usage();
			return;
		}

		String file = args[0];
		// String file = "F:/d31/complie/BIT-MiniCC/test/parse_test/test.c";

		if (!file.endsWith(".c")) {
			System.out.println("Incorrect input file:" + file);
			return;
		}

		MiniCCompiler cc = new MiniCCompiler();
		System.out.println("Start to compile ...");
		cc.run(file);
		System.out.println("Compiling completed!");
	}

	public static void usage() {
		System.out.println("USAGE: BITMiniCC FILE_NAME.c");

	}
}
