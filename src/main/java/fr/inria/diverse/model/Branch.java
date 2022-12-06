package fr.inria.diverse.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Arrays;

public class Branch implements Comparable<Branch>, Serializable {
    static Logger logger = LogManager.getLogger(Snapshot.class);
    private BranchType type;

    public Branch() {
        super();
    }

    public Branch(String branchType) {
        this.type = BranchType.findBranchType(branchType);
    }

    @Override
    public int compareTo(@NotNull Branch branch) {
        return this.type.compareTo(branch.type);
    }

    public enum BranchType implements Comparable<BranchType> {
        MAIN("main", 1), MASTER("master", 1),
        DEVELOP("develop", 2), STAGING("staging", 2),
        DEV("dev", 2), TRUNK("trunk", 2),
        STABLE("stable", 2);

        private final String text;
        private final Integer priority;

        private BranchType(String text, int priority) {
            this.text = text;
            this.priority = priority;
        }

        public static BranchType findBranchType(String str) {
            return Arrays.stream(values())
                    .filter(branch -> str.equalsIgnoreCase(branch.getBranchType()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No constant found"));
        }

        public static boolean isABranchType(String str) {
            return Arrays.stream(values())
                    .filter(branch -> str.equalsIgnoreCase(branch.getBranchType()))
                    .count() > 0;
        }

        public String getBranchType() {
            return this.text;
        }

        public Integer getBranchPriority() {
            return this.priority;
        }

    }
}
