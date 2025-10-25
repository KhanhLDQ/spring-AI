package org.tommap.springai.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter @Setter
@NoArgsConstructor
public class HelpdeskInputRequest {
    @NotNull
    private String message;
}
