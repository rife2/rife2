/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifeinstrumenttests;

import java.util.List;

public class Note {
    private String title_;
    private List<String> tags_;

    public String getTitle() {
        return title_;
    }

    public void setTitle(String title) {
        title_ = title;
    }

    public List<String> getTags() {
        return tags_;
    }

    public void setTags(List<String> tags) {
        tags_ = tags;
    }
}
