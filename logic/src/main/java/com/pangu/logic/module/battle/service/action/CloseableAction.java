package com.pangu.logic.module.battle.service.action;

/**
 * 可提前关闭的行动
 */
public abstract class CloseableAction implements Action {

    private boolean done;

    public void done() {
        done = true;
    }

    public boolean isDone() {
        return done;
    }
}
