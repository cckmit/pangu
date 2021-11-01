package common;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Result<T> {

    private int code;
    private T content;

    public Result(int code) {
        this.code = code;
    }

    /**
     * 创建成功的返回对象
     *
     * @return
     */
    public static <T> Result<T> SUCCESS() {
        return new Result<T>();
    }

    /**
     * 创建成功的返回对象
     *
     * @param content 返回内容
     * @return
     */
    public static <T> Result<T> SUCCESS(T content) {
        return new Result<T>(0, content);
    }

    /**
     * 创建错误的返回对象
     *
     * @param code 错误状态码
     * @return
     */
    public static <T> Result<T> ERROR(int code) {
        return new Result<T>(code);
    }

    /**
     * 创建错误的返回对象
     *
     * @param code    错误状态码
     * @param content 返回内容
     * @return
     */
    public static <T> Result<T> ERROR(int code, T content) {
        return new Result<T>(code, content);
    }
}