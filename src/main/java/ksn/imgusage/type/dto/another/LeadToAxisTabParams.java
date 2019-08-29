package ksn.imgusage.type.dto.another;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.another.LeadToHorizontalTab;

/** Init parameters for {@link LeadToHorizontalTab} */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LeadToHorizontalTabParams implements ITabParams {


    @Override
    public String toString() { return "{ ..none.. }"; }

}
