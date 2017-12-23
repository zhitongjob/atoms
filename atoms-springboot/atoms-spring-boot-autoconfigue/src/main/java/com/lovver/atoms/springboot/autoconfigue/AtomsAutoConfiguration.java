package com.lovver.atoms.springboot.autoconfigue;

import com.lovver.atoms.config.AtomsBean;
import com.lovver.atoms.config.AtomsSpringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AtomsProperties.class)
@ConditionalOnClass(AtomsBean.class)
@ConditionalOnProperty(prefix = "atoms", value = "enabled", matchIfMissing = true)
public class AtomsAutoConfiguration {
    @Autowired
    private AtomsProperties atomsProperties;

    @Bean
    @ConditionalOnMissingBean(AtomsBean.class)
    public AtomsBean initAtoms() {
        AtomsBean atomsBean = atomsProperties.getAtomsbean();
        AtomsSpringConfig atomsSpringConfig = new AtomsSpringConfig();
        atomsSpringConfig.setAtomsBean(atomsBean);
        return atomsBean;
    }
}