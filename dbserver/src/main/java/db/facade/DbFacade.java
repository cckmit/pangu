package db.facade;

import common.Result;
import db.EntityRes;

/**
 * 数据服接口
 */
public interface DbFacade {

    Result<EntityRes> queryById();
}
