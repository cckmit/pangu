package  com.pangu.logic.utils.code.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 伪装、还原数字，伪装后的字符串包含：占位符+分隔符+真实数字伪装
 *
 * @author fei
 */
public class NumberFaker {

    /**
     * 0~9数字字符
     */
    private static final char[] NUMBERS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final int NUMBER_LEN = NUMBERS.length;
    /**
     * 除排除（0，O），分割位（6 7 8 9 ）之外的数字，大写字母，通过索引与NUMBERS做转换
     */
    private static final char[] LETTERS = {'B', 'I', 'P', 'Z', 'R', 'M', 'D', 'C', 'L', 'G', 'K',
            '2', 'Q', '1', '5', 'H', 'J', 'U', 'Y', '3', 'S', 'A', 'E', 'N', 'V', 'W', 'F', 'T', 'X',
            '4'};

    private static final int LETTER_LEN = LETTERS.length;

    private static final int MULTIPLE = LETTER_LEN / NUMBER_LEN;
    /**
     * 字母索引
     */
    private static final Map<Character, Integer> LETTER_INDEX_MAP;

    /**
     * 分割位，用以标识之后内容为真实数字
     */
    private static final char[] SEPARATE_ARRAY = {'6', '7', '8', '9'};
    private static final Set<Character> SEPARATE_SET;

    private static final int SEPARATE_LEN = SEPARATE_ARRAY.length;

    static {
        LETTER_INDEX_MAP = new HashMap<>();
        for (int i = 0; i < LETTERS.length; i++) {
            LETTER_INDEX_MAP.put(LETTERS[i], i);
        }

        SEPARATE_SET = new HashSet<>();
        for (char c : SEPARATE_ARRAY) {
            SEPARATE_SET.add(c);
        }
    }


    /**
     * 伪装数字
     *
     * @param formatLen 要生成的长度
     */
    public static String fakeNumber(long number, int formatLen) {
        String s = String.valueOf(number);
        int len = s.length();
        if (len > formatLen) {
            throw new IllegalArgumentException(
                    "数字" + s + "的长度大于最大长度" + formatLen + "，无法生成对应字符串");
        }

        StringBuilder sb = new StringBuilder(formatLen);
        int left = formatLen - len;
        if (left > 0) {
            fill(sb, left);
        }
        for (int i = 0; i < len; i++) {
            sb.append(fakeNumber(s.charAt(i)));
        }
        return sb.toString();
    }

    /**
     * 填充占位符，count-1位随机从LETTERS中取，最后一位为分隔符
     */
    private static void fill(StringBuilder sb, int count) {
        for (int i = 0; i < count - 1; i++) {
            sb.append(LETTERS[ThreadLocalRandom.current().nextInt(LETTER_LEN)]);
        }
        sb.append(SEPARATE_ARRAY[ThreadLocalRandom.current().nextInt(SEPARATE_LEN)]);
    }

    /**
     * 伪装数字
     */
    private static char fakeNumber(char c) {
        int index = ThreadLocalRandom.current().nextInt(MULTIPLE) * NUMBER_LEN + (c - '0');
        return LETTERS[index];
    }

    /**
     * 还原数字
     */
    public static OptionalLong restoreNumber(String str) {
        return restoreNumber(str, 0, str.length());
    }

    /**
     * 还原数字
     *
     * @param startIndex 开始索引，包括
     * @param endIndex   结束索引，不包括
     */
    public static OptionalLong restoreNumber(String str, int startIndex, int endIndex) {
        if (str == null || endIndex > str.length()) {
            return OptionalLong.empty();
        }
        int realStartIndex = findStartIndex(str, startIndex, endIndex);
        if (realStartIndex >= endIndex) {
            return OptionalLong.empty();
        }
        StringBuilder sb = new StringBuilder(endIndex - realStartIndex);
        for (int i = realStartIndex; i < endIndex; i++) {
            Optional<Character> optional = restoreNumber(str.charAt(i));
            if (!optional.isPresent()) {
                return OptionalLong.empty();
            } else {
                sb.append(optional.get());
            }
        }
        long number = Long.parseLong(sb.toString());
        return OptionalLong.of(number);
    }

    /**
     * 寻找真实开始索引，即分隔符后一位
     *
     * @param startIndex 开始索引，包括
     * @param endIndex   结束索引，不包括
     */
    public static int findStartIndex(String str, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            if (SEPARATE_SET.contains(str.charAt(i))) {
                return i + 1;
            }
        }
        return startIndex;
    }

    /**
     * 还原数字
     */
    private static Optional<Character> restoreNumber(char c) {
        Integer index = LETTER_INDEX_MAP.get(c);
        if (index == null) {
            return Optional.empty();
        }
        index %= NUMBER_LEN;
        return Optional.of(NUMBERS[index]);
    }
}
