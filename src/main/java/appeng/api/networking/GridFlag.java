package appeng.api.networking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GridFlag {
    private static final List<GridFlag> FLAGS = new ArrayList<>();

    public static final GridFlag REQUIRE_CHANNEL = createFlag("REQUIRE_CHANNEL");
    public static final GridFlag COMPRESSED_CHANNEL = createFlag("COMPRESSED_CHANNEL");
    public static final GridFlag CANNOT_CARRY = createFlag("CANNOT_CARRY");
    public static final GridFlag CANNOT_CARRY_COMPRESSED = createFlag("CANNOT_CARRY_COMPRESSED");
    public static final GridFlag DENSE_CAPACITY = createFlag("DENSE_CAPACITY");
    public static final GridFlag MULTIBLOCK = createFlag("MULTIBLOCK");
    public static final GridFlag PREFERRED = createFlag("PREFERRED");

    public static final GridFlag CABLE = createFlag("CABLE");

    private final String flag;

    private GridFlag(String flag) {
        this.flag = flag;
    }

    public static GridFlag createFlag(String flag) {
        GridFlag newFlag = new GridFlag(flag);
        FLAGS.add(newFlag);
        return newFlag;
    }

    public static List<GridFlag> values() {
        return Collections.unmodifiableList(FLAGS);
    }

    public String getProperty() {
        return flag;
    }
}
