package bit.minisys.minicc.icgen;

public class LabelRow {
    public String name;
    public boolean define;
    public int address; // +1

    public LabelRow() {
        name = null;
        define = false;
        address = 0;
    }

    public LabelRow(String _name, boolean _define, int _address) {
        name = _name;
        define = _define;
        address = _address;

    }
}
