package ru.loftschool.bashclient.rest.model;

import com.google.gson.annotations.Expose;

public class StoryModel {

    @Expose
    private String site;
    @Expose
    private String name;
    @Expose
    private String desc;
    @Expose
    private String link;
    @Expose
    private String elementPureHtml;

    /**
     *
     * @return
     * The site
     */
    public String getSite() {
        return site;
    }

    /**
     *
     * @param site
     * The site
     */
    public void setSite(String site) {
        this.site = site;
    }

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     *
     * @param desc
     * The desc
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     *
     * @return
     * The link
     */
    public String getLink() {
        return link;
    }

    /**
     *
     * @param link
     * The link
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     *
     * @return
     * The elementPureHtml
     */
    public String getElementPureHtml() {
        return elementPureHtml;
    }

    /**
     *
     * @param elementPureHtml
     * The elementPureHtml
     */
    public void setElementPureHtml(String elementPureHtml) {
        this.elementPureHtml = elementPureHtml;
    }

}