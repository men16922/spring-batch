package io.springbatch.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class JdbcBatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private int chunkSize = 10;
    private final DataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job job() throws Exception {
        return jobBuilderFactory.get("batchJob")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() throws Exception {
        return stepBuilderFactory.get("step1")
                .<Customer, Customer>chunk(chunkSize)
                .reader(customItemReader())
                .writer(customItemWriter())
                .build();
    }

    @Bean
    public JdbcPagingItemReader<Customer> customItemReader() throws Exception {

        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.dataSource);
        reader.setFetchSize(chunkSize);
        reader.setRowMapper(new CustomerRowMapper());

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, firstName, lastName, birthdate");
        queryProvider.setFromClause("from customer");
//        queryProvider.setWhereClause("where firstname like :firstname");

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);
        reader.setQueryProvider(queryProvider);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("firstname", "A%");


        return reader;
    }

    @Bean
    public JdbcBatchItemWriter<Customer> customItemWriter() {
        return new JdbcBatchItemWriterBuilder<Customer>()
                .dataSource(dataSource)
                .sql("insert into customer2 values (:id, :firstName, :lastName, :birthdate)")
                .beanMapped()
//                .columnMapped()
                .build();
    }

}

