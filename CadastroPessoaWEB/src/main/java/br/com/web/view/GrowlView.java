package br.com.web.view;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class GrowlView {
    public void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public void showInfo(String title,String message) {
        addMessage(FacesMessage.SEVERITY_INFO, title, message);
    }

    public void showWarn(String title,String message) {
        addMessage(FacesMessage.SEVERITY_WARN, title, message);
    }

    public void showError(String title,String message) {
        addMessage(FacesMessage.SEVERITY_ERROR, title, message);
    }
}
