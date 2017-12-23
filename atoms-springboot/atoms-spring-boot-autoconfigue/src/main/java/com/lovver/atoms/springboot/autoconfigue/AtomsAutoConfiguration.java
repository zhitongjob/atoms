package com.lovver.atoms.springboot.autoconfigue;

import com.lovver.atoms.config.AtomsBean;
import com.lovver.atoms.config.AtomsConfig;
import com.lovver.atoms.config.AtomsSpringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration//开启配置
@EnableConfigurationProperties(AtomsProperties.class)//开启使用映射实体对象
@ConditionalOnClass(AtomsBean.class)//存在HelloService时初始化该配置类
@ConditionalOnProperty//存在对应配置信息时初始化该配置类
        (
                prefix = "atoms",//存在配置前缀hello
                value = "enabled",//开启
                matchIfMissing = true//缺失检查
        )
public class AtomsAutoConfiguration {

    //application.properties配置文件映射前缀实体对象
    @Autowired
    private AtomsProperties atomsProperties;

    /**
     * 根据条件判断不存在HelloService时初始化新bean到SpringIoc
     *
     * @return
     */
    @Bean//创建HelloService实体bean
    @ConditionalOnMissingBean(AtomsBean.class)//缺失HelloService实体bean时，初始化HelloService并添加到SpringIoc
    public AtomsBean initAtoms() {
        AtomsBean atomsBean = atomsProperties.getAtomsbean();
        AtomsSpringConfig atomsSpringConfig=new AtomsSpringConfig();
        atomsSpringConfig.setAtomsBean(atomsBean);
        return atomsBean;
    }
}