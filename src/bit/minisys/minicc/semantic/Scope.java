package bit.minisys.minicc.semantic;

import java.util.Map;
import java.util.HashMap;

import java.util.ArrayList;

import java.util.List;

public class Scope {
    public Map<String, SymbolRow> table;
    public Scope upScope;
    public List<Scope> childScopes;

    public Scope() {
        this.table = new HashMap<String, SymbolRow>();
        this.upScope = null;
        this.childScopes = new ArrayList<Scope>();
    }

    public Scope(Scope up) {
        this.table = new HashMap<String, SymbolRow>();
        this.upScope = up;
        this.childScopes = new ArrayList<Scope>();
    }

    public SymbolRow getSymbolRow(String name) {
        if (this.table.containsKey(name)) {
            return this.table.get(name);
        }
        if (upScope != null) {
            return upScope.getSymbolRow(name);
        } else {
            return null;
        }
    }

    public void print() {
        System.out.print("\n" + table + "\n");
    }
}
