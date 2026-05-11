package com.example.mybatis.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(
        basePackages = "com.example.mybatis.mapper.write",
        sqlSessionFactoryRef = "writeSqlSessionFactory",
        sqlSessionTemplateRef = "writeSqlSessionTemplate"
)
public class WriteDataSourceConfig {

    @Primary
    @Bean(name = "writeDataSource")
    @ConfigurationProperties(prefix = "app.datasource.write")
    public DataSource writeDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Primary
    @Bean(name = "writeSqlSessionFactory")
    public SqlSessionFactory writeSqlSessionFactory(
            @Qualifier("writeDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setTypeAliasesPackage("com.example.mybatis.entity");
        bean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        bean.setConfiguration(configuration);
        return bean.getObject();
    }

    @Primary
    @Bean(name = "writeSqlSessionTemplate")
    public SqlSessionTemplate writeSqlSessionTemplate(
            @Qualifier("writeSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Primary
    @Bean(name = "writeTransactionManager")
    public PlatformTransactionManager writeTransactionManager(
            @Qualifier("writeDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
