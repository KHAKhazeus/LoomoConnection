package com.kha.loomoconnection.restserver.model.responses;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.yanzhenjie.andserver.http.ResponseBody;
import com.yanzhenjie.andserver.util.MediaType;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import lombok.Data;

@Data
public class BaseRsp implements ResponseBody {
    public String id;
    public String info;
    public Boolean success;
    public Object data;

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public long contentLength() {
        Gson gson = new Gson();
        String json = gson.toJson(this, BaseRsp.class);
        return json.length();
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return MediaType.APPLICATION_JSON;
    }

    @Override
    public void writeTo(@NonNull OutputStream output) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(this, BaseRsp.class);
        output.write(json.getBytes(StandardCharsets.UTF_8));
    }
}
