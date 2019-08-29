package ksn.imgusage.type.dto.another;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.another.LeadToPerspectiveTab;

/** Init parameters for {@link LeadToPerspectiveTab} */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LeadToPerspectiveTabParams implements ITabParams {


    @Override
    public String toString() { return "{ ..none.. }"; }

}
