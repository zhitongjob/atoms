package com.lovver.atoms.springboot.autoconfigue;

import com.lovver.atoms.config.AtomsBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "atoms")
public class AtomsProperties {
    private AtomsBean atomsbean;

    public AtomsBean getAtomsbean() {
        return atomsbean;
    }

    public void setAtomsbean(AtomsBean atomsbean) {
        this.atomsbean = atomsbean;
    }
}