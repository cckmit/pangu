package com.pangu.dbaccess.service;

import java.io.Serializable;

public interface EntityBuilder<PK extends Comparable<PK> & Serializable, T> {

    T newInstance(PK id);

}
