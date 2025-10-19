package org.tommap.springai.model.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CountryCitiesResponse {
    private String country;
    private List<String> cities;
}
