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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
public class ReportBatchConfiguration {

    /**
     * Spring Batch에서 메타데이터를 저장하고 관리하는 컴포넌트
     *
     * <pre>
     *     현재 단계, 처리된 아이템 수 등과 같은 정보를 저장.
     *     작업이 중단 또는 실패했을 경우 재시작 하거나 상태 추적할 수 있도록 도와줌.
     * </pre>
     */
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public ReportBatchConfiguration( JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job helloTasklet() {
    return new JobBuilder("helloTasklet", jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(this.step1())
        .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                // 첫 번째 데이터 소스를 사용하는 작업
                System.out.println("Step 1: Using primary data source");
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

}
