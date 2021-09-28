package com.zw.platform.event;

import com.zw.platform.domain.basicinfo.form.AssignmentForm;
import org.springframework.context.ApplicationEvent;

public class UpdateAssignmentEvent extends ApplicationEvent {
    private AssignmentForm form;

    public UpdateAssignmentEvent(Object source, AssignmentForm form) {
        super(source);
        this.form = form;
    }

    public AssignmentForm getForm() {
        return form;
    }
}
