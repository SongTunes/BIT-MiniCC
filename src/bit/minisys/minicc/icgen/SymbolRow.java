package bit.minisys.minicc.icgen;

import java.util.ArrayList;
import java.util.List;

public class SymbolRow {
    public String name;
    public String type;
    public String spec; // int, float, ...
    public int intValue;

    // array
    public int arrayDim;
    public ArrayList<Integer> arraySize;

    // function
    public int paramNum;
    public ArrayList<String> paramType;
    public int iId;
    public int oId;

    // constant
    public int constId;

    public SymbolRow() {
        this.name = null;
        this.type = null;
        this.spec = null;
        this.intValue = 0;
        // array
        this.arrayDim = 0;
        this.arraySize = null;

        // function
        this.paramNum = 0;
        this.paramType = null;
        this.iId = 0; // include
        this.oId = 0; // not include <
        // [iId, oId)

        // constant
        this.constId = 0;

    }

    public boolean addArrayDim(Integer a) {
        if (this.type == "Array") {
            if (this.arraySize == null) {
                this.arraySize = new ArrayList<Integer>();
            }
            this.arrayDim++;
            this.arraySize.add(a);
            return true;
        }
        return false;
    }

    public boolean addFuncParams(String spec) {
        if (this.type == "Function") {
            if (this.paramType == null) {
                this.paramType = new ArrayList<String>();
            }
            this.paramNum++;
            this.paramType.add(spec);
            return true;
        }
        return false;
    }
}
