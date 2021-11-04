package  com.pangu.logic.utils.code.service;

import  com.pangu.logic.utils.code.model.ActiveCodeUnit;
import  com.pangu.logic.utils.code.model.GiftCodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * 激活码生成器 激活码构成: 填充+分割+顺序号+激活码id+类型id构成
 *
 * @author fei
 */
public class ActiveCodeBuilder {

    private static final GiftCodeType TYPE = GiftCodeType.ACTIVE_CODE;

    /**
     * 每个（同一id）激活码最大数量
     */
    public static final int MAX_NUMBER = 99_9999;
    /**
     * 激活码顺序号最大长度
     */
    public static final int NUMBER_MAX_LEN = String.valueOf(MAX_NUMBER).length();
    /**
     * 激活码最短长度，id最少1位，类型1位，所以最少 MAX_LEN + 2
     */
    public static final int MIN_LEN = NUMBER_MAX_LEN + 2;

    /**
     * 前缀最大8位
     */
    public static final int PREFIX_MAX_LEN = 8;

    /**
     * 生成激活码
     *
     * @param start  开始序号
     * @param count  生成数量
     * @param id     激活码id
     * @param prefix 前缀
     */
    public static Optional<List<String>> buildActiveCodes(int start, int count, long id,
        String prefix) {
        int end = start + count;
        if (end - 1 > MAX_NUMBER) {
            return Optional.empty();
        }
        int prefixLen = 0;
        if (prefix != null && !prefix.isEmpty()) {
            prefixLen = prefix.length();
            if (prefixLen > PREFIX_MAX_LEN) {
                return Optional.empty();
            }
        }

        String idStr = String.valueOf(id);

        List<String> codes = new ArrayList<>(count);
        for (int i = start; i < end; i++) {
            String fakeId = NumberFaker.fakeNumber(id, idStr.length());
            String fakeType = NumberFaker.fakeNumber(TYPE.getId(), 1);
            String prefixFake = NumberFaker.fakeNumber(prefixLen, 1);
            String code =
                NumberFaker.fakeNumber(i, NUMBER_MAX_LEN) + fakeId + prefixFake + fakeType;
            if (prefixLen > 0) {
                code = prefix + code;
            }
            codes.add(code);
        }

        return Optional.of(codes);
    }

    /**
     * 还原激活码
     *
     * @param code 激活码
     */
    public static Optional<ActiveCodeUnit> restore(String code) {
        if (code.length() < MIN_LEN) {
            return Optional.empty();
        }
        OptionalLong typeValue = NumberFaker
            .restoreNumber(code, code.length() - 1, code.length());
        if (!typeValue.isPresent() || typeValue.getAsLong() != TYPE.getId()) {
            return Optional.empty();
        }
        OptionalLong prefixLen = NumberFaker
            .restoreNumber(code, code.length() - 2, code.length() - 1);
        if (prefixLen.isPresent()) {
            int len = (int) prefixLen.getAsLong();
            code = code.substring(len);
        }
        OptionalLong indexOptional = NumberFaker.restoreNumber(code, 0, NUMBER_MAX_LEN);
        if (!indexOptional.isPresent()) {
            return Optional.empty();
        }
        int number = (int) indexOptional.getAsLong();

        OptionalLong idOptional = NumberFaker
            .restoreNumber(code, NUMBER_MAX_LEN, code.length() - 2);
        if (!idOptional.isPresent()) {
            return Optional.empty();
        }
        long id = idOptional.getAsLong();
        return Optional.of(new ActiveCodeUnit(id, number));
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
