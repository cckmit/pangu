package  com.pangu.logic.utils.code.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

import  com.pangu.logic.utils.code.service.NumberFaker;
import lombok.Getter;

@Getter
public enum GiftCodeType {
    /**
     * 服务器礼包，每个可被多人领取，有数量限制
     */
    SERVER_GIFT(0),
    /**
     * 激活码,每个码只能被一个人领取
     */
    ACTIVE_CODE(1),

    ;

    private static final Map<Integer, GiftCodeType> map = new HashMap<>();

    static {
        for (GiftCodeType type : GiftCodeType.values()) {
            map.put(type.getId(), type);
        }
    }

    /**
     * 值，不能超过10
     */
    private final int id;

    GiftCodeType(int id) {
        this.id = id;
    }


    public static GiftCodeType getById(int id) {
        return map.get(id);
    }

    /**
     * 根据礼包码获取类型
     */
    public static Optional<GiftCodeType> getGiftType(String code) {
        OptionalLong typeValue = NumberFaker
            .restoreNumber(code, code.length() - 1, code.length());
        if (!typeValue.isPresent()) {
            return Optional.empty();
        }
        int v = (int) typeValue.getAsLong();
        GiftCodeType type = GiftCodeType.getById(v);
        return Optional.ofNullable(type);
    }
}
