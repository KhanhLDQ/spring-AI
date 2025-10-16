package org.tommap.springai.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class PromptTemplateRequest {
    private String customerName;
    private String customerMsg;
}
