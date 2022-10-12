package io.springbatch.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Configuration
public class CompositeItemConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private int chunkSize = 10;

    @Bean
    public Job job() {
        return jobBuilderFactory.get("batchJob")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<String, String>chunk(chunkSize)
                .reader(new ItemReader<String>() {
                    int i = 0;
                    
                    @Override
                    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        i++;
                        
                        return i > 10 ? null : "item";
                    }
                })
                .processor(customItemProcessor())
                .writer(items -> System.out.println(items))
                .build();
    }

    @Bean
    public ItemProcessor<? super String, String> customItemProcessor() {

        List itemProcessor = new ArrayList();
        itemProcessor.add(new CustomItemProcessor());
        itemProcessor.add(new CustomItemProcessor2());

        return new CompositeItemProcessorBuilder<>()
                .delegates(itemProcessor)
                .build();

    }


}

