package bit.minisys.minicc.semantic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import bit.minisys.minicc.internal.util.MiniCCUtil;
import bit.minisys.minicc.MiniCCCfg;

import com.fasterxml.jackson.databind.ObjectMapper;

import bit.minisys.minicc.parser.ast.*;

public class MiniCCSemanticAnalyzer implements IMiniCCSemantic {

	@Override
	public String run(String iFile) throws Exception {
		System.out.println("4. MiniCCSemanticAnalyzer Semanticing...");
		String oFile = iFile;
		ObjectMapper mapper = new ObjectMapper();

		ASTCompilationUnit program = (ASTCompilationUnit) mapper.readValue(new File(iFile), ASTCompilationUnit.class);

		/*
		 * String[] dummyStrs = new String[16];
		 * TreeViewer viewr = new TreeViewer(Arrays.asList(dummyStrs), program);
		 */

		MiniCCSemanticBuilder stBuilder = new MiniCCSemanticBuilder();
		program.accept(stBuilder);
		// Scope scope = stBuilder.getScope();

		stBuilder.printErrorInfo();

		return oFile;
	}

}
