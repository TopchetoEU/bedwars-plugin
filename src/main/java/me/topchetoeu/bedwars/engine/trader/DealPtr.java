package me.topchetoeu.bedwars.engine.trader;

import java.util.List;

public class DealPtr {
    private int sectionN;
    private int dealN;
    
    public int getSectionN() {
        return sectionN;
    }
    public int getDealN() {
        return dealN;
    }
    
    public Deal getDeal(List<Section> sections) {
        if (sections.size() <= sectionN) return null;
        if (sections.get(sectionN).getDeals().size() <= dealN) return null;
        return sections.get(sectionN).getDeal(dealN);
    }
    
    public DealPtr(int sectionN, int dealN) {
        this.sectionN = sectionN;
        this.dealN = dealN;
    }
}
