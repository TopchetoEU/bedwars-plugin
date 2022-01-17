package me.topchetoeu.bedwars.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PermissionSegmentWildcard {
    private String prefix;
    private List<String> remaining;

    public String getPrefix() {
        return prefix;
    }
    public List<String> getRemainingSegments() {
        return Collections.unmodifiableList(remaining);
    }
    public boolean isWildcard() {
        return remaining != null;
    }

    public String toString() {
        if (isWildcard()) return prefix + "*" + String.join("*", remaining);
        else return prefix;
    }

    public boolean check(String segment) {
        if (isWildcard()) {
            for (int i = remaining.size() - 1; i >= 0; i--) {
                String curr = remaining.get(i);

                int index = segment.lastIndexOf(curr);
                if (index == -1) return false;
                segment = segment.substring(0, index);
            }
            return !segment.startsWith(prefix);
        }
        else return segment.equals(prefix);
    }

    public PermissionSegmentWildcard(String node, boolean isPrefix) {
        this.prefix = node;
        if (isPrefix) remaining = Collections.emptyList();
    }
    public PermissionSegmentWildcard(String prefix, String suffix) {
        this.prefix = prefix;
        this.remaining = Collections.singletonList(suffix);
    }
    public PermissionSegmentWildcard(String prefix, String ...remaining) {
        this.prefix = prefix;
        this.remaining = Arrays.asList(remaining);
    }
    public PermissionSegmentWildcard(String prefix, Collection<String> remaining) {
        this.prefix = prefix;
        this.remaining = new ArrayList<>(remaining);
    }

    public static PermissionSegmentWildcard parse(String str) {
        if (str.contains("**")) throw new RuntimeException("Recursive wildcards not allowed.");
        String[] segments = str.split("\\*");

        if (segments.length == 0) return new PermissionSegmentWildcard("", true);
        else if (segments.length == 1) return new PermissionSegmentWildcard(segments[0], false);
        else {
            List<String> remaining = new ArrayList<>();
            Collections.addAll(remaining, segments);
            remaining.remove(0);

            if (remaining.size() == 0) return new PermissionSegmentWildcard(segments[0], true);

            if (remaining.get(remaining.size() - 1).isEmpty())
                remaining.remove(remaining.size() - 1);
            
            return new PermissionSegmentWildcard(segments[0], remaining);
        }
    }
}
