/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

public class RegularBeanImpl {
    public enum Color {red, blue, green}

    private String hidden_ = null;
    private String anotherhidden_ = null;
    private String login_ = null;
    private String anotherlogin_ = null;
    private String password_ = null;
    private String anotherpassword_ = null;
    private String email_ = null;
    private String url_ = null;
    private String comment_ = null;
    private String anothercomment_ = null;
    private String question_ = null;
    private String anotherquestion_ = null;
    private String customquestion_ = null;
    private String anothercustomquestion_ = null;
    private int[] options_ = null;
    private int[] otheroptions_ = null;
    private int[] customoptions_ = null;
    private int[] othercustomoptions_ = null;
    private boolean invoice_ = false;
    private boolean onemoreinvoice_ = false;
    private Color[] colors_ = null;
    private String[] morecolors_ = null;
    private String[] yourcolors_ = null;

    public void setHidden(String hidden) {
        hidden_ = hidden;
    }

    public String getHidden() {
        return hidden_;
    }

    public void setAnotherhidden(String anotherhidden) {
        anotherhidden_ = anotherhidden;
    }

    public String getAnotherhidden() {
        return anotherhidden_;
    }

    public void setLogin(String login) {
        login_ = login;
    }

    public String getLogin() {
        return login_;
    }

    public void setAnotherlogin(String anotherlogin) {
        anotherlogin_ = anotherlogin;
    }

    public String getAnotherlogin() {
        return anotherlogin_;
    }

    public void setPassword(String password) {
        password_ = password;
    }

    public String getPassword() {
        return password_;
    }

    public void setAnotherpassword(String anotherpassword) {
        anotherpassword_ = anotherpassword;
    }

    public String getAnotherpassword() {
        return anotherpassword_;
    }

    public void setEmail(String email) {
        email_ = email;
    }

    public String getEmail() {
        return email_;
    }

    public void setUrl(String url) {
        url_ = url;
    }

    public String getUrl() {
        return url_;
    }

    public void setComment(String comment) {
        comment_ = comment;
    }

    public String getComment() {
        return comment_;
    }

    public void setAnothercomment(String anothercomment) {
        anothercomment_ = anothercomment;
    }

    public String getAnothercomment() {
        return anothercomment_;
    }

    public void setQuestion(String question) {
        question_ = question;
    }

    public String getQuestion() {
        return question_;
    }

    public void setAnotherquestion(String anotherquestion) {
        anotherquestion_ = anotherquestion;
    }

    public String getAnotherquestion() {
        return anotherquestion_;
    }

    public void setCustomquestion(String customquestion) {
        customquestion_ = customquestion;
    }

    public String getCustomquestion() {
        return customquestion_;
    }

    public void setAnothercustomquestion(String anothercustomquestion) {
        anothercustomquestion_ = anothercustomquestion;
    }

    public String getAnothercustomquestion() {
        return anothercustomquestion_;
    }

    public void setOptions(int[] options) {
        options_ = options;
    }

    public int[] getOptions() {
        return options_;
    }

    public void setOtheroptions(int[] otheroptions) {
        otheroptions_ = otheroptions;
    }

    public int[] getOtheroptions() {
        return otheroptions_;
    }

    public void setCustomoptions(int[] customoptions) {
        customoptions_ = customoptions;
    }

    public int[] getCustomoptions() {
        return customoptions_;
    }

    public void setOthercustomoptions(int[] othercustomoptions) {
        othercustomoptions_ = othercustomoptions;
    }

    public int[] getOthercustomoptions() {
        return othercustomoptions_;
    }

    public void setInvoice(boolean invoice) {
        invoice_ = invoice;
    }

    public boolean isInvoice() {
        return invoice_;
    }

    public void setOnemoreinvoice(boolean onemoreinvoice) {
        onemoreinvoice_ = onemoreinvoice;
    }

    public boolean isOnemoreinvoice() {
        return onemoreinvoice_;
    }

    public void setColors(Color[] colors) {
        colors_ = colors;
    }

    public Color[] getColors() {
        return colors_;
    }

    public void setMorecolors(String[] morecolors) {
        morecolors_ = morecolors;
    }

    public String[] getMorecolors() {
        return morecolors_;
    }

    public void setYourcolors(String[] yourcolors) {
        yourcolors_ = yourcolors;
    }

    public String[] getYourcolors() {
        return yourcolors_;
    }
}

