/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

public class PersonCallbacks extends Person implements Cloneable {
    private Integer id_;

    public void setId(Integer id) {
        id_ = id;
    }

    public Integer getId() {
        return id_;
    }

}
