package club.thom.tem.export.search;

import java.util.Comparator;

public class SortFilter {
    String name;
    Comparator<ClickableItem> comparator;
    public SortFilter(String name, Comparator<ClickableItem> comparator) {
        this.name = name;
        this.comparator = comparator;
    }

    public String getName() {
        return name;
    }

    public Comparator<ClickableItem> getComparator() {
        return comparator;
    }
}
