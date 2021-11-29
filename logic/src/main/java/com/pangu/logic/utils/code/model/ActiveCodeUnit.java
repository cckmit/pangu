package  com.pangu.logic.utils.code.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ActiveCodeUnit {

    /**
     * 激活码id
     */
    private long id;
    /**
     * 激活码顺序号
     */
    private int number;

}
