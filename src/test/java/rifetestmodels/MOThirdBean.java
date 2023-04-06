/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

public class MOThirdBean extends MetaData {
    private Integer id_;
    //	private Collection<MOFirstBean> firstBeans_;
    private String thirdString_;

    public MOThirdBean() {
    }

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("id").identifier(true));
//		addConstraint(new ConstrainedProperty("firstBeans").manyToManyAssociation());
    }

    public void setId(Integer id) {
        id_ = id;
    }

    public Integer getId() {
        return id_;
    }

//	public void setFirstBeans(Collection<MOFirstBean> firstBeans) {
//		firstBeans_ = firstBeans;
//	}
//	
//	public Collection<MOFirstBean> getFirstBeans() {
//		return firstBeans_;
//	}

    public void setThirdString(String thirdString) {
        thirdString_ = thirdString;
    }

    public String getThirdString() {
        return thirdString_;
    }
}

