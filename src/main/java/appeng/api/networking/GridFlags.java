package appeng.api.networking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GridFlags {
    private static final List<GridFlags> FLAGS = new ArrayList<>();

    public static final GridFlags REQUIRE_CHANNEL = createFlag("REQUIRE_CHANNEL");
    public static final GridFlags COMPRESSED_CHANNEL = createFlag("COMPRESSED_CHANNEL");
    public static final GridFlags CANNOT_CARRY = createFlag("CANNOT_CARRY");
    public static final GridFlags CANNOT_CARRY_COMPRESSED = createFlag("CANNOT_CARRY_COMPRESSED");
    public static final GridFlags DENSE_CAPACITY = createFlag("DENSE_CAPACITY");
    public static final GridFlags MULTIBLOCK = createFlag("MULTIBLOCK");
    public static final GridFlags PREFERRED = createFlag("PREFERRED");

    public static final GridFlags CABLE = createFlag("CABLE");

    private final String flag;

    private GridFlags(String flag) {
        this.flag = flag;
    }

    public static GridFlags createFlag(String flag) {
        GridFlags newFlag = new GridFlags(flag);
        FLAGS.add(newFlag);
        return newFlag;
    }

    public static List<GridFlags> values() {
        return Collections.unmodifiableList(FLAGS);
    }

    public String getProperty() {
        return flag;
    }
}
