package me.topchetoeu.bedwars.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PermissionWildcard {
    private List<PermissionSegmentWildcard> prefix;
    private List<PermissionSegmentWildcard> suffix;

    public boolean hasRecursiveWildcard() {
        return suffix != null;
    }

    public PermissionWildcard(boolean isPrefix, Collection<PermissionSegmentWildcard> segments) {
        this.prefix = new ArrayList<>(segments);
        if (isPrefix) suffix = Collections.emptyList();
    }
    public PermissionWildcard(boolean isPrefix, PermissionSegmentWildcard ...segments) {
        this.prefix = Arrays.asList(segments);
        if (isPrefix) suffix = Collections.emptyList();
    }
    public PermissionWildcard(Collection<PermissionSegmentWildcard> prefix, Collection<PermissionSegmentWildcard> suffix) {
        this.prefix = new ArrayList<>(prefix);
        this.suffix = new ArrayList<>(suffix);
    }

    public boolean check(String perm) {
        String[] segments = perm.split(".");
        if (segments.length < prefix.size()) return false;
        if (suffix != null && segments.length < suffix.size()) return false;
        int i = 0;
        for (; i < prefix.size(); i++) {
            if (!prefix.get(i).check(segments[i])) return false;
        }

        if (hasRecursiveWildcard()) {
            int j = 0;
            for (i = suffix.size(), j = segments.length; i >= 0; i--, j--) {
                if (!prefix.get(i).check(segments[j])) return false;
            }
            return true;
        }
        else return i == prefix.size();
    }

    public String toString() {
        String res = String.join(".", prefix.toArray(String[]::new));

        if (hasRecursiveWildcard()) {
            res += ".**";
            if (suffix != null) res += "." + String.join(".", suffix.toArray(String[]::new));
        }

        return res;
    }

    public static PermissionWildcard parse(String raw) {
        String[] segments = raw.split("\\.");

        ArrayList<PermissionSegmentWildcard> prefix = new ArrayList<>();
        ArrayList<PermissionSegmentWildcard> suffix = null;
        ArrayList<PermissionSegmentWildcard> currList = prefix;

        for (String segment : segments) {
            if (segment.isEmpty()) throw new RuntimeException("Empty permission segments are not allowed.");

            if (segment.equals("**")) {
                if (suffix == null) {
                    currList = suffix = new ArrayList<>();
                }
                else throw new RuntimeException("Multiple recursive wildcards are not allowed.");
            }
            else if (segment.contains("**")) throw new RuntimeException("Wildcards must be the only thing in a segment.");
            else currList.add(PermissionSegmentWildcard.parse(segment));
        }


        if (suffix == null) return new PermissionWildcard(false, prefix);
        else return new PermissionWildcard(prefix, suffix);
    }
}
