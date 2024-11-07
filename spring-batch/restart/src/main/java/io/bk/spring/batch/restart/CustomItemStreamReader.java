package io.bk.spring.batch.restart;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * <p> 클래스 설명 </p>
 *
 * @author Mingi Park
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomItemStreamReader implements
    ItemStreamReader<UserDto> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private BufferedReader reader;
    private int offset = 0;

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            log.info("open CustomItemStreamReader.....");
            ClassPathResource resource = new ClassPathResource("data.jsonl");
            reader = new BufferedReader(new FileReader(resource.getFile()));
            offset = executionContext.getInt("READ_OFFSET", 0);
            log.info("Read offset: " + offset);

            for (int i = 0; i < offset; i++) {
                reader.readLine();
            }
        } catch (IOException e) {
            throw new ItemStreamException(e);
        }
    }

    @Override
    public UserDto read()
        throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        String jsonLine = reader.readLine();
        UserDto userDto = null;

        if(StringUtils.isNotBlank(jsonLine)) {
            userDto = objectMapper.readValue(jsonLine, UserDto.class);
            offset++;
        }

//        log.info("read item : {}", userDto);


        return userDto;
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
//        executionContext.putInt("READ_OFFSET", offset);
    }

    @Override
    public void close() throws ItemStreamException {
        try {
            reader.close();
        } catch (IOException e) {
            throw new ItemStreamException(e);
        }
    }
}
