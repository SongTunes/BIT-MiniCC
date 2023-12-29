package bit.minisys.minicc.icgen;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import bit.minisys.minicc.parser.ast.*;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;

import bit.minisys.minicc.MiniCCCfg;
import bit.minisys.minicc.internal.util.MiniCCUtil;
import bit.minisys.minicc.parser.ast.ASTCompilationUnit;

public class MiniCCICGen implements IMiniCCICGen {

	@Override
	public String run(String iFile) throws Exception {

		System.out.println("5. MiniCCICGen IC Generating...");

		ObjectMapper mapper = new ObjectMapper();

		ASTCompilationUnit program = (ASTCompilationUnit) mapper.readValue(new File(iFile), ASTCompilationUnit.class);

		/*
		 * String[] dummyStrs = new String[16];
		 * TreeViewer viewr = new TreeViewer(Arrays.asList(dummyStrs), program);
		 */

		MiniCCICBuilder icBuilder = new MiniCCICBuilder();
		program.accept(icBuilder);
		// Scope scope = icBuilder.getScope();

		// icBuilder.printErrorInfo();

		// oFile is xx.ir.txt
		String oFile = MiniCCUtil.remove2Ext(iFile) + MiniCCCfg.MINICC_ICGEN_OUTPUT_EXT;
		MiniCCICPrinter icPrinter = new MiniCCICPrinter(icBuilder.getQuats());
		icPrinter.print(oFile);
		// System.out.println("5. ICGen finished!");
		return oFile;
	}

	public String run1(String iFile, MiniCCICBuilder icBuilder) throws Exception {
		System.out.println("5. MiniCCICGen IC Generating...");
		ObjectMapper mapper = new ObjectMapper();

		ASTCompilationUnit program = (ASTCompilationUnit) mapper.readValue(new File(iFile), ASTCompilationUnit.class);

		/*
		 * String[] dummyStrs = new String[16];
		 * TreeViewer viewr = new TreeViewer(Arrays.asList(dummyStrs), program);
		 */

		program.accept(icBuilder);

		// Scope scope = icBuilder.getScope();

		// icBuilder.printErrorInfo();

		// oFile is xx.ir.txt
		String oFile = MiniCCUtil.remove2Ext(iFile) + MiniCCCfg.MINICC_ICGEN_OUTPUT_EXT;
		MiniCCICPrinter icPrinter = new MiniCCICPrinter(icBuilder.getQuats());
		icPrinter.print(oFile);

		return oFile;
	}

}
