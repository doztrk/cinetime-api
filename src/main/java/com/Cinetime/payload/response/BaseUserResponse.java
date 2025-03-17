package com.Cinetime.payload.response;

import jdk.jshell.Snippet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class BaseUserResponse {

    private Long id;
    private String name;
    //Will concat the name + surname on mapping level
    private String email;
    private String phoneNumber;


}
