package com.kha.loomoconnection.restserver.model.requests;


import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class EmojiRequest extends BaseRequest{
    public boolean emojiMode;
    public String behavior;
}
