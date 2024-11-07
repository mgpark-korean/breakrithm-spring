package io.bk.spring.batch.restart;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.core.step.skip.ExceptionClassifierSkipPolicy;
import org.springframework.batch.core.step.skip.NeverSkipItemSkipPolicy;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.classify.SubclassClassifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class RestartJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean(name = "helloJob")
    public Job helloJob(Step step) {
        return new JobBuilder("helloJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(step)
            .build();
    }

    @Bean
    public Step jsonToDatabaseStep(
        JsonItemReader<UserDto> reader,
        ItemWriter<UserDto> writer,
        ItemProcessor<UserDto, UserDto> processor,
        ExceptionClassifierSkipPolicy exceptionClassifierSkipPolicy) {
        return new StepBuilder("step", jobRepository)
            .<UserDto, UserDto>chunk(5, transactionManager) // 청크 사이즈 설정
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .listener(new StepExecutionListener() {
                @Override
                public void beforeStep(StepExecution stepExecution) {
                    log.info("step before!!");
                }

                @Override
                public ExitStatus afterStep(StepExecution stepExecution) {
                    log.info("index : {}", stepExecution.getReadCount());
                    stepExecution.getExecutionContext()
                        .putLong("start.index", stepExecution.getReadCount());
                    return ExitStatus.COMPLETED;
                }
            })
            .listener(new ItemWriteListener<>() {


                @Override
                public void afterWrite(Chunk<? extends UserDto> items) {
                    ItemWriteListener.super.afterWrite(items);

                }
            })
            .faultTolerant()
            .skipPolicy(exceptionClassifierSkipPolicy)
            .build();
    }

    @Bean
    public ItemProcessor<UserDto, UserDto> processor() {
        return item -> {
            if (item.getName().equals("mgpark")) {
//                log.info("skip item {}.....", item);
//                throw new RuntimeException("ddd");
            }
            return item;
        };
    }

    @Bean
    @StepScope
    public JsonItemReader<UserDto> jsonItemReader(
        @Value("#{stepExecutionContext['start.index']}") Long startIndex) {

        return new JsonItemReaderBuilder<UserDto>()
            .name("jsonItemReader")
            .jsonObjectReader(new JacksonJsonObjectReader<>(UserDto.class))
            .resource(new ClassPathResource("user.json"))
            .saveState(true) //  현재 상태를 저장
            .currentItemCount(startIndex != null ? startIndex.intValue() : 0)
            .build();
    }

    @Bean
    public ItemWriter<UserDto> writer() {
        // JPA나 JDBC를 사용하여 데이터베이스에 쓰는 로직 구현
        // 예: JpaItemWriter 또는 JdbcBatchItemWriter 사용
        return items -> {
            log.info("Writing {} items", items.size());

            items.forEach(item -> log.info("Item: {}", item));
        };
    }

    @Bean
    public ExceptionClassifierSkipPolicy exceptionClassifierSkipPolicy() {
        // 1. 예외 유형에 따른 SkipPolicy 설정
        Map<Class<? extends Throwable>, SkipPolicy> policyMap = new HashMap<>();
        policyMap.put(ParseException.class, new AlwaysSkipItemSkipPolicy()); // 항상 건너뛰기

        SubclassClassifier<Throwable, SkipPolicy> classifier = new SubclassClassifier<>(
            new NeverSkipItemSkipPolicy());
        classifier.add(ParseException.class, new AlwaysSkipItemSkipPolicy());
        classifier.add(RuntimeException.class, new AlwaysSkipItemSkipPolicy());
        classifier.add(IllegalArgumentException.class, new AlwaysSkipItemSkipPolicy());
        classifier.add(InvalidFormatException.class, new AlwaysSkipItemSkipPolicy());

        ExceptionClassifierSkipPolicy policy = new ExceptionClassifierSkipPolicy();

        policy.setPolicyMap(policyMap);
        policy.setExceptionClassifier(classifier);
        return policy;
    }
}
