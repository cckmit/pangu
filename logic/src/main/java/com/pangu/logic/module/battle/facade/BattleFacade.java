package com.pangu.logic.module.battle.facade;

import com.pangu.framework.socket.anno.InBody;
import com.pangu.logic.module.battle.model.UnitBuildInfo;
import com.pangu.logic.module.battle.model.report.BattleReport;
import com.pangu.framework.socket.anno.SocketCommand;
import com.pangu.framework.socket.anno.SocketModule;
import com.pangu.framework.utils.model.Result;

import static com.pangu.logic.module.battle.facade.BattleModule.*;


/**
 * 战斗模块
 */
@SocketModule(MODULE)
public interface BattleFacade {

    /**
     * 测试接口
     *
     * @param attacker
     * @param defenser
     * @return
     */
    @SocketCommand(value = COMMAND_TEST)
    Result<BattleReport> test(@InBody(value = "attacker") String attacker,
                              @InBody(value = "defender") String defenser);

    /**
     * 测试接口
     *
     * @param attacker
     * @param defender
     * @return
     */
    @SocketCommand(value = COMMAND_TEST_UNIT)
    Result<BattleReport> testUnit(@InBody(value = "attacker") UnitBuildInfo[][] attacker,
                                  @InBody(value = "defender") UnitBuildInfo[][] defender);

    /**
     * 测试接口
     *
     * @param attacker
     * @param defender
     * @return
     */
    @SocketCommand(value = COMMAND_TEST_ID)
    Result<BattleReport> testUnitId(@InBody(value = "attacker") String[] attacker,
                                  @InBody(value = "defender") String[] defender);


}
