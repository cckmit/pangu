package com.pangu.gateway.module.facade;

import com.pangu.core.anno.ComponentGate;
import com.pangu.core.gate.facade.GateFacade;
import com.pangu.framework.utils.model.Result;
import com.pangu.gateway.module.service.GateService;

import java.util.Map;

@ComponentGate
public class GateFacadeImpl implements GateFacade {

    private final GateService gateService;

    public GateFacadeImpl(GateService gateService) {
        this.gateService = gateService;
    }

    @Override
    public Result<Map<Long, Long>> online() {
        Map<Long, Long> online = gateService.online();
        return Result.SUCCESS(online);
    }
}
