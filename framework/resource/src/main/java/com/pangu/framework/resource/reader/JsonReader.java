package com.pangu.framework.resource.reader;

import com.pangu.framework.resource.exception.DecodeException;
import com.pangu.framework.utils.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class JsonReader implements ResourceReader {

    public <E> List<E> read(InputStream input, Class<E> clz) {
        int lineIndex = 1;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            List<E> list = new LinkedList<>();
            while (reader.ready()) {
                final String line = reader.readLine();
                list.add(JsonUtils.string2Object(line, clz));
                lineIndex++;
            }
            return list;
        } catch (Exception e) {
            log.error("当前资源[{}],第[{}]行出现异常，{}", clz.getSimpleName(), lineIndex, e);
            throw new DecodeException(e);
        }
    }

    @Override
    public String getFormat() {
        return "json";
    }

    @Override
    public void config(String config) {
    }

}
