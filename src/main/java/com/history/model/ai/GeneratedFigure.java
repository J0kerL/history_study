package com.history.model.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * LLM 生成的历史人物数据结构。
 *
 * @author Diamond
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedFigure {

    @JsonProperty(required = true)
    private String name;

    @JsonProperty(required = true)
    private String subtitle;

    @JsonProperty(required = true)
    private String birthDate;

    @JsonProperty(required = true)
    private String deathDate;

    @JsonProperty(required = true)
    private String dynasty;

    @JsonProperty(required = true)
    private String birthPlace;

    @JsonProperty(required = true)
    private String biography;

    @JsonProperty(required = true)
    private String works;

    @JsonProperty(required = true)
    private List<String> tags;

    @JsonProperty(required = true)
    private String imagePrompt;
}
