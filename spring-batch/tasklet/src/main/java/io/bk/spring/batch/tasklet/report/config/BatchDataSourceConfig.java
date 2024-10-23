package io.bk.spring.batch.tasklet.report.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
public class BatchDataSourceConfig {

  @Bean
  @Primary
  @ConfigurationProperties("spring.datasource.batch")
  public DataSourceProperties batchDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean(name = "batchDataSource")
  @Primary
  public DataSource batchDataSource() {
    return batchDataSourceProperties()
        .initializeDataSourceBuilder()
        .type(HikariDataSource.class)
        .build();
  }

  // Transaction Manager for Batch
  @Bean(name = "batchTransactionManager")
  @Primary
  public DataSourceTransactionManager batchTransactionManager(
      @Qualifier("batchDataSource") DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

//  @Bean
//  public DataSourceInitializer databasePopulator(
//      @Qualifier("batchDataSource") DataSource dataSource) {
//    ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
//    populator.addScript(
//        new ClassPathResource(
//            "org/springframework/batch/core/schema-mysql.sql")); // use custom schema
//    populator.addScript(new ClassPathResource("sql/batch-schema.sql"));
//    // 스크립트 실행 중 오류가 발생하면 중단되도록 설정합니다.
//    // 한 스크립트의 오류가 전체 실행을 중지시킵니다.
//    populator.setContinueOnError(false);
//    // 이전에 존재하는 객체를 삭제하려고 시도할 때 오류를 무시하지 않도록 설정합니다.
//    // 삭제가 실패하면 예외를 발생시킵니다.
//    populator.setIgnoreFailedDrops(false);
//    DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
//    dataSourceInitializer.setDataSource(dataSource);
//    dataSourceInitializer.setDatabasePopulator(populator);
//    return dataSourceInitializer;
//  }
}
