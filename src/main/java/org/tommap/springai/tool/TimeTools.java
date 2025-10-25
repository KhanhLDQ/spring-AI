package org.tommap.springai.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;

@Component
@Slf4j
public class TimeTools {
    @Tool(name = "getCurrentLocalTime", description = "get the current time in the user's timezone")
    String getCurrentLocalTime() {
        log.info("get the current time in the user's timezone");
        return LocalTime.now().toString();
    }

    @Tool(name = "getCurrentTime", description = "get the current time in the specific timezone")
    String getCurrentTime(
        @ToolParam(description = "value represents the timezone") String timezone
    ) {
        log.info("get the current time in the specific timezone: {}", timezone);
        return LocalTime.now(
                ZoneId.of(timezone)
        ).toString();
    }
}
