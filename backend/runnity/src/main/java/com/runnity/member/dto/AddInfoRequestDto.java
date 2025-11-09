package com.runnity.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddInfoRequestDto {
    private String nickname;
    private Float height;
    private Float weight;
    private String gender;
    private String birth;
}
