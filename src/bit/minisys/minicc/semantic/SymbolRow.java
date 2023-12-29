package bit.minisys.minicc.semantic;

import java.util.ArrayList;
import java.util.List;

public class SymbolRow {
    public String name;
    public String type;
    public String spec; // int, float, ...

    // array
    public int arrayDim;
    public ArrayList<Integer> arraySize;

    // function
    public int paramNum;
    public ArrayList<String> paramType;

    public SymbolRow() {
        this.name = null;
        this.type = null;
        this.spec = null;
        // array
        this.arrayDim = 0;
        this.arraySize = null;

        // function
        this.paramNum = 0;
        this.paramType = null;

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
