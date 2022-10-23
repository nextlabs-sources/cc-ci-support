package com.nextlabs.common.cc.cisupport.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for deployment environment group.
 *
 * @author Sachindra Dasun
 */
public class EnvironmentGroup {

    private String name;
    private List<Environment> environments;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Environment> getEnvironments() {
        if (environments == null) {
            environments = new ArrayList<>();
        }
        return environments;
    }

    public void setEnvironments(List<Environment> environments) {
        this.environments = environments;
    }
}
