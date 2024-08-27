module rife {
    requires java.datatransfer;
    requires java.desktop;
    requires java.instrument;
    requires java.logging;
    requires java.prefs;
    requires java.sql;
    requires java.xml;

    requires static ij;
    requires static jakarta.servlet;
    requires static org.eclipse.jetty.server;
    requires static org.eclipse.jetty.ee10.servlet;
    requires static org.jsoup;
    requires static tomcat.embed.core;

    exports rife;
    exports rife.authentication;
    exports rife.authentication.credentials;
    exports rife.authentication.credentialsmanagers;
    exports rife.authentication.credentialsmanagers.exceptions;
    exports rife.authentication.elements;
    exports rife.authentication.elements.exceptions;
    exports rife.authentication.exceptions;
    exports rife.authentication.remembermanagers;
    exports rife.authentication.remembermanagers.exceptions;
    exports rife.authentication.sessionmanagers;
    exports rife.authentication.sessionmanagers.exceptions;
    exports rife.authentication.sessionvalidators;
    exports rife.authentication.sessionvalidators.exceptions;
    exports rife.cmf;
    exports rife.cmf.dam;
    exports rife.cmf.dam.contentmanagers;
    exports rife.cmf.dam.contentmanagers.exceptions;
    exports rife.cmf.dam.contentstores;
    exports rife.cmf.dam.contentstores.exceptions;
    exports rife.cmf.dam.exceptions;
    exports rife.cmf.elements;
    exports rife.cmf.format;
    exports rife.cmf.format.exceptions;
    exports rife.cmf.loader;
    exports rife.cmf.loader.image;
    exports rife.cmf.loader.xhtml;
    exports rife.cmf.transform;
    exports rife.cmf.validation;
    exports rife.config;
    exports rife.config.exceptions;
    exports rife.continuations;
    exports rife.continuations.basic;
    exports rife.continuations.exceptions;
    exports rife.database;
    exports rife.database.exceptions;
    exports rife.database.queries;
    exports rife.database.querymanagers.generic;
    exports rife.database.querymanagers.generic.exceptions;
    exports rife.database.types;
    exports rife.datastructures;
    exports rife.engine;
    exports rife.engine.annotations;
    exports rife.engine.elements;
    exports rife.engine.exceptions;
    exports rife.feed;
    exports rife.feed.elements;
    exports rife.feed.exceptions;
    exports rife.forms;
    exports rife.ioc;
    exports rife.ioc.exceptions;
    exports rife.resources;
    exports rife.resources.exceptions;
    exports rife.scheduler;
    exports rife.scheduler.exceptions;
    exports rife.scheduler.schedulermanagers;
    exports rife.scheduler.schedulermanagers.exceptions;
    exports rife.scheduler.taskmanagers;
    exports rife.scheduler.taskmanagers.exceptions;
    exports rife.scheduler.taskoptionmanagers;
    exports rife.scheduler.taskoptionmanagers.exceptions;
    exports rife.selector;
    exports rife.servlet;
    exports rife.template;
    exports rife.template.exceptions;
    exports rife.test;
    exports rife.tools;
    exports rife.tools.exceptions;
    exports rife.validation;
    exports rife.validation.annotations;
    exports rife.validation.exceptions;
    exports rife.web;
    exports rife.workflow;
    exports rife.xml;
    exports rife.xml.exceptions;
}