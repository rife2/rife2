/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.junit.jupiter.api.Test;
import rife.cmf.MimeType;
import rife.engine.exceptions.EngineException;
import rife.test.MockConversation;
import rife.test.MockFileUpload;
import rife.test.MockRequest;
import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class TestParametersBeanFill {
    public static class Person extends MetaData {
        private int id_ = -1;
        private String name_ = null;
        private String email_ = null;
        private boolean active_ = false;
        private String notes_ = null;
        private String content_ = null;
        private String bio_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addGroup("profile")
                .addConstraint(new ConstrainedProperty("name"))
                .addConstraint(new ConstrainedProperty("email"))
                .addConstraint(new ConstrainedProperty("active"))
                .addConstraint(new ConstrainedProperty("content").file(true))
                .addConstraint(new ConstrainedProperty("bio").mimeType(MimeType.TEXT_PLAIN));
            addConstraint(new ConstrainedProperty("notes"));
        }

        public void setId(int id) {
            id_ = id;
        }

        public int getId() {
            return id_;
        }

        public void setName(String name) {
            name_ = name;
        }

        public String getName() {
            return name_;
        }

        public void setEmail(String email) {
            email_ = email;
        }

        public String getEmail() {
            return email_;
        }

        public void setActive(boolean active) {
            active_ = active;
        }

        public boolean isActive() {
            return active_;
        }

        public void setNotes(String notes) {
            notes_ = notes;
        }

        public String getNotes() {
            return notes_;
        }

        public void setContent(String content) {
            content_ = content;
        }

        public String getContent() {
            return content_;
        }

        public void setBio(String bio) {
            bio_ = bio;
        }

        public String getBio() {
            return bio_;
        }
    }

    static Person storedPerson() {
        var person = new Person();
        person.setId(7);
        person.setName("stored name");
        person.setEmail("stored@email.com");
        person.setActive(true);
        person.setNotes("stored notes");
        person.setContent("stored content");
        person.setBio("stored bio");
        return person;
    }

    static String state(Person person) {
        return person.getId() + "," + person.getName() + "," + person.getEmail() + "," + person.isActive() + "," + person.getNotes() + "," + person.getContent() + "," + person.getBio();
    }

    public static class Settings extends MetaData {
        private String name_ = null;
        private boolean publiclyVisible_ = true;

        public void activateMetaData() {
            addGroup("form")
                .addConstraint(new ConstrainedProperty("name"))
                .addConstraint(new ConstrainedProperty("publiclyVisible"));
        }

        public void setName(String name) {
            name_ = name;
        }

        public String getName() {
            return name_;
        }

        public void setPubliclyVisible(boolean publiclyVisible) {
            publiclyVisible_ = publiclyVisible;
        }

        public boolean isPubliclyVisible() {
            return publiclyVisible_;
        }
    }

    public static class FillSite extends Site {
        public void setup() {
            post("/group", c -> {
                var person = storedPerson();
                c.parametersBean(person, null, "profile");
                c.print(state(person));
            });
            post("/group/prefix", c -> {
                var person = storedPerson();
                c.parametersBean(person, "p_", "profile");
                c.print(state(person));
            });
            post("/group/class", c -> {
                var person = c.parametersBean(Person.class, null, "profile");
                c.print(state(person));
            });
            post("/group/unknown", c -> {
                try {
                    c.parametersBean(storedPerson(), null, "nosuchgroup");
                    c.print("no exception");
                } catch (EngineException e) {
                    c.print("engine exception");
                }
            });
            post("/group/null", c -> {
                try {
                    c.parametersBean(storedPerson(), null, null);
                    c.print("no exception");
                } catch (IllegalArgumentException e) {
                    c.print("illegal argument");
                }
            });
            post("/group/unsupported", c -> {
                try {
                    c.parametersBean(new Object(), null, "profile");
                    c.print("no exception");
                } catch (EngineException e) {
                    c.print("engine exception");
                }
            });
            post("/settings/group", c -> {
                var settings = new Settings();
                settings.setName("stored");
                settings.setPubliclyVisible(true);
                c.parametersBean(settings, null, "form");
                c.print(settings.getName() + "," + settings.isPubliclyVisible());
            });
        }
    }

    @Test
    void testGroupFillUpdatesAndResets() {
        var conversation = new MockConversation(new FillSite());

        // the group's absent properties reset, the unchecked checkbox
        // becomes false, properties outside the group are preserved
        var response = conversation.doRequest("/group", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("name", "new name"));
        assertEquals("7,new name,null,false,stored notes,stored content,null", response.getText());
    }

    @Test
    void testGroupFillAllPresent() {
        var conversation = new MockConversation(new FillSite());

        var response = conversation.doRequest("/group", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("name", "new name")
            .parameter("email", "new@email.com")
            .parameter("active", "true"));
        assertEquals("7,new name,new@email.com,true,stored notes,stored content,null", response.getText());
    }

    @Test
    void testGroupFillEmptyValueResets() {
        var conversation = new MockConversation(new FillSite());

        var response = conversation.doRequest("/group", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("name", "new name")
            .parameter("email", ""));
        assertEquals("7,new name,null,false,stored notes,stored content,null", response.getText());
    }

    @Test
    void testGroupFillIdentifierNotBindable() {
        var conversation = new MockConversation(new FillSite());

        // the identifier is declared editable(false), which is what keeps
        // a forged parameter from retargeting the bean, being outside the
        // group keeps it from being reset
        var response = conversation.doRequest("/group", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("id", "99")
            .parameter("notes", "forged notes")
            .parameter("name", "new name"));
        assertEquals("7,new name,null,false,stored notes,stored content,null", response.getText());
    }

    @Test
    void testGroupFillPrefix() {
        var conversation = new MockConversation(new FillSite());

        var response = conversation.doRequest("/group/prefix", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("p_name", "prefixed name")
            .parameter("name", "unprefixed name"));
        assertEquals("7,prefixed name,null,false,stored notes,stored content,null", response.getText());
    }

    @Test
    void testGroupFillClassScopesBinding() {
        var conversation = new MockConversation(new FillSite());

        var response = conversation.doRequest("/group/class", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("name", "new name")
            .parameter("notes", "outside the group"));
        assertEquals("-1,new name,null,false,null,null,null", response.getText());
    }

    @Test
    void testGroupFillErrors() {
        var conversation = new MockConversation(new FillSite());

        assertEquals("engine exception", conversation.doRequest("/group/unknown", new MockRequest()
            .method(RequestMethod.POST).parameter("name", "x")).getText());
        assertEquals("illegal argument", conversation.doRequest("/group/null", new MockRequest()
            .method(RequestMethod.POST).parameter("name", "x")).getText());
        assertEquals("engine exception", conversation.doRequest("/group/unsupported", new MockRequest()
            .method(RequestMethod.POST).parameter("name", "x")).getText());
    }

    @Test
    void testGroupFillFileFieldPreservedWithoutUpload() {
        var conversation = new MockConversation(new FillSite());

        // no parameter and no file arrived for the file property, its
        // stored content stays while the mime-typed bio resets with the
        // rest of the group
        var response = conversation.doRequest("/group", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("name", "new name"));
        assertEquals("7,new name,null,false,stored notes,stored content,null", response.getText());
    }

    @Test
    void testGroupFillFileFieldIgnoresParameter() {
        var conversation = new MockConversation(new FillSite());

        // a file property is only entered through uploads, a regular
        // parameter can't bypass the upload handling
        var response = conversation.doRequest("/group", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("name", "new name")
            .parameter("content", "typed content"));
        assertEquals("7,new name,null,false,stored notes,stored content,null", response.getText());
    }

    @Test
    void testGroupFillParameterFieldIgnoresUpload() {
        var conversation = new MockConversation(new FillSite());

        // the bio property isn't a file field, an upload for it is ignored
        // and its absent parameter resets it with the rest of the group
        var request = new MockRequest().method(RequestMethod.POST);
        request.setFile("bio", new MockFileUpload("bio.txt",
            new ByteArrayInputStream("uploaded bio".getBytes()), "text/plain"));
        var response = conversation.doRequest("/group", request.parameter("name", "new name"));
        assertEquals("7,new name,null,false,stored notes,stored content,null", response.getText());
    }

    @Test
    void testGroupFillFileFieldUpload() {
        var conversation = new MockConversation(new FillSite());

        var request = new MockRequest().method(RequestMethod.POST);
        request.setFile("content", new MockFileUpload("content.txt",
            new ByteArrayInputStream("uploaded content".getBytes()), "text/plain"));
        var response = conversation.doRequest("/group", request.parameter("name", "new name"));
        assertEquals("7,new name,null,false,stored notes,uploaded content,null", response.getText());
    }

    @Test
    void testGroupFillUnselectedFileInput() {
        var conversation = new MockConversation(new FillSite());

        // an unselected file input produces an upload part without a
        // filename, it doesn't count as an upload and the file property
        // keeps its stored content
        var request = new MockRequest().method(RequestMethod.POST);
        request.setFile("content", new MockFileUpload("",
            new ByteArrayInputStream(new byte[0]), "application/octet-stream"));
        var response = conversation.doRequest("/group", request.parameter("name", "new name"));
        assertEquals("7,new name,null,false,stored notes,stored content,null", response.getText());
    }

    @Test
    void testGroupFillMixedFileParts() {
        var conversation = new MockConversation(new FillSite());

        // the first same-name part is an unselected input, the real
        // upload that follows it is the one that counts
        var request = new MockRequest().method(RequestMethod.POST);
        request.setFiles("content", new MockFileUpload[]{
            new MockFileUpload("", new ByteArrayInputStream(new byte[0]), "application/octet-stream"),
            new MockFileUpload("content.txt", new ByteArrayInputStream("uploaded content".getBytes()), "text/plain")});
        var response = conversation.doRequest("/group", request.parameter("name", "new name"));
        assertEquals("7,new name,null,false,stored notes,uploaded content,null", response.getText());
    }

    @Test
    void testDefaultTrueBooleanUnchecks() {
        var conversation = new MockConversation(new FillSite());

        // an unchecked checkbox means false, not whatever the property
        // defaults to, or a default-true flag could never be turned off
        var response = conversation.doRequest("/settings/group", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("name", "new name"));
        assertEquals("new name,false", response.getText());

        // a checked checkbox still arrives as its own parameter
        response = conversation.doRequest("/settings/group", new MockRequest()
            .method(RequestMethod.POST)
            .parameter("name", "new name")
            .parameter("publiclyVisible", "true"));
        assertEquals("new name,true", response.getText());
    }
}
