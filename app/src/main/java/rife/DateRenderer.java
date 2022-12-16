package rife;

import rife.config.RifeConfig;
import rife.template.*;
import java.util.Date;

public class DateRenderer implements ValueRenderer {
    public String render(Template template, String valueId, String differentiator) {
        return RifeConfig.tools().getDefaultShortDateFormat().format(new Date());
    }
}