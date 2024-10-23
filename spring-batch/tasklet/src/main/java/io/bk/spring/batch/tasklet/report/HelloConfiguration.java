package io.bk.spring.batch.tasklet.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class HelloConfiguration {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  @Bean(name = "helloJob")
  public Job helloJob() {
    return new JobBuilder("helloJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(this.helloStep())
        .build();
  }

  @Bean
  public Step helloStep() {
    return new StepBuilder("helloStep", jobRepository)
        .tasklet(
            (contribution, chunkContext) -> {
              log.info("hello spring batch");
              return RepeatStatus.FINISHED;
            },
            transactionManager)
        .build();
  }
}
