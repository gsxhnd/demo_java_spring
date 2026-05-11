package com.example.datajpa.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.datajpa.repository.write",
        entityManagerFactoryRef = "writeEntityManagerFactory",
        transactionManagerRef = "writeTransactionManager"
)
public class WriteDataSourceConfig {

    @Primary
    @Bean(name = "writeDataSource")
    @ConfigurationProperties(prefix = "app.datasource.write")
    public DataSource writeDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Primary
    @Bean(name = "writeEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean writeEntityManagerFactory(
            @Qualifier("writeDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.datajpa.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaPropertyMap(Map.of(
                "hibernate.hbm2ddl.auto", "update",
                "hibernate.show_sql", "true",
                "hibernate.format_sql", "true",
                "hibernate.dialect", "org.hibernate.dialect.MySQLDialect"
        ));
        em.setPersistenceUnitName("write");
        return em;
    }

    @Primary
    @Bean(name = "writeTransactionManager")
    public PlatformTransactionManager writeTransactionManager(
            @Qualifier("writeEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
