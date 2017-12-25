package com.lovver.atoms.springboot.autoconfigue;

import com.lovver.atoms.config.AtomsBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "atoms")
public class AtomsProperties extends AtomsBean{

}