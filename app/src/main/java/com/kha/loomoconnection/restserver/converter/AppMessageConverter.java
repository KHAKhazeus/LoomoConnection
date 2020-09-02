package com.kha.loomoconnection.restserver.converter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.yanzhenjie.andserver.annotation.Converter;
import com.yanzhenjie.andserver.framework.MessageConverter;
import com.yanzhenjie.andserver.http.ResponseBody;
import com.yanzhenjie.andserver.util.IOUtils;
import com.yanzhenjie.andserver.util.MediaType;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

@Converter
public class AppMessageConverter implements MessageConverter {

    @Override
    public ResponseBody convert(Object output, MediaType mediaType) {
        return (ResponseBody) output;
    }

    @Override
    public <T> T convert(InputStream stream, MediaType mediaType, Type type) {
        Gson gson = new Gson();
        Charset charset = mediaType == null ? null : mediaType.getCharset();
        if (charset == null) {
            try {
                String stringData = IOUtils.toString(stream);
                return gson.fromJson(stringData, type);
            } catch (IOException e) {
                return null;
            }
        } else {
            try {
                String stringData = IOUtils.toString(stream, charset);
                return gson.fromJson(stringData, type);
            } catch (IOException e) {
                return null;
            }
        }
    }
}