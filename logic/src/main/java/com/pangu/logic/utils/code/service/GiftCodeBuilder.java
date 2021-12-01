package  com.pangu.logic.utils.code.service;

import  com.pangu.logic.utils.code.model.GiftCodeType;

import java.util.Optional;
import java.util.OptionalLong;

/**
 * 礼包码生成器 礼包码构成: 礼包id+类型id构成
 *
 * @author fei
 */
public class GiftCodeBuilder {


    private static final GiftCodeType TYPE = GiftCodeType.SERVER_GIFT;
    /**
     * 礼包码中礼包id部分最短位数最少位数
     */
    public static final int NUMBER_MIN_LEN = 8;
    
    public static final int MIN_LEN = 8 + 1;

    /**
     * 生成礼包码
     *
     * @param id 礼包码id
     */
    public static String buildGiftCode(long id) {
        String idStr = String.valueOf(id);
        int len = Math.max(idStr.length(), NUMBER_MIN_LEN);
        String fakeId = NumberFaker.fakeNumber(id, len);
        String fakeType = NumberFaker.fakeNumber(TYPE.getId(), 1);
        return fakeId + fakeType;
    }

    /**
     * 还原礼包码
     *
     * @param code 礼包码
     */
    public static OptionalLong restore(String code) {

        if (code.length() < MIN_LEN) {
            return OptionalLong.empty();
        }
        OptionalLong typeValue = NumberFaker
            .restoreNumber(code, code.length() - 1, code.length());
        if (!typeValue.isPresent() || typeValue.getAsLong() != TYPE.getId()) {
            return OptionalLong.empty();
        }

        return NumberFaker.restoreNumber(code, 0, code.length() - 1);
    }

    /**
     * 检查激活码是否合理，只做基础检查
     */
    public static boolean checkCodeBasicly(String code) {
        if (code == null) {
            return false;
        }
        if (code.length() < MIN_LEN) {
            return false;
        }

        Optional<GiftCodeType> optional = GiftCodeType.getGiftType(code);
        return optional.isPresent() && optional.get() == TYPE;
    }
}
