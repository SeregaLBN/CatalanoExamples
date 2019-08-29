package ksn.imgusage.type.dto.opencv.custom;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import ksn.imgusage.tabs.ITabParams;
import ksn.imgusage.tabs.opencv.custom.LeadToPerspectiveTab;

/** Init parameters for {@link LeadToPerspectiveTab} */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LeadToPerspectiveTabParams implements ITabParams {


    @Override
    public String toString() { return "{ ..none.. }"; }

}
