package  com.pangu.logic.utils;

import com.pangu.framework.utils.id.IdGenerator;

public class ServerIdUtils {

    /**
     * 通过id拿到运营商服务器
     *
     * @param id
     * @return
     */
    public static String toOidSid(long id) {
        IdGenerator.IdInfo idInfo = new IdGenerator.IdInfo(id);
        return idInfo.getOperator() + "_" + idInfo.getServer();
    }
}
