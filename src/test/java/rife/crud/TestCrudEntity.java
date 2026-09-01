/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.crud;

import org.junit.jupiter.api.Test;
import rife.crud.exceptions.*;
import rife.database.TestDatasources;
import rife.template.TemplateFactory;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;
import rife.validation.PropertyValidationRule;
import rife.validation.Validated;
import rife.validation.Validation;
import rife.validation.ValidationContext;
import rife.validation.ValidationError;
import rife.validation.ValidationGroup;
import rife.validation.ValidationRule;
import rifetestmodels.CrudArticle;
import rifetestmodels.CrudSubscriber;

import static org.junit.jupiter.api.Assertions.*;

public class TestCrudEntity {
    public static class Plain extends MetaData {
        private int id_ = -1;
        private String name_ = null;

        public void activateMetaData() {
            // deliberately nothing, so that the defaults are what decide
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setName(String name) { name_ = name; }
        public String getName() { return name_; }
    }

    public static class Chapter extends MetaData {
        private int id_ = -1;
        private int bookId_ = -1;
        private int position_ = -1;
        private String title_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("bookId").listed(true));
            addConstraint(new ConstrainedProperty("title").listed(true));
            addConstraint(new ConstrainedProperty("position").ordinal(true, "bookId").editable(false));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setBookId(int bookId) { bookId_ = bookId; }
        public int getBookId() { return bookId_; }
        public void setPosition(int position) { position_ = position; }
        public int getPosition() { return position_; }
        public void setTitle(String title) { title_ = title; }
        public String getTitle() { return title_; }
    }

    public static class Mapped extends MetaData {
        private int userId_ = -1;
        private int sortOrder_ = -1;
        private int groupId_ = -1;
        private byte[] avatar_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("userId").identifier(true).columnName("user_id").editable(false));
            addConstraint(new ConstrainedProperty("groupId").columnName("group_id"));
            addConstraint(new ConstrainedProperty("sortOrder").ordinal(true, "groupId").columnName("sort_order"));
            addConstraint(new ConstrainedProperty("avatar").file(true));
        }

        public void setUserId(int userId) { userId_ = userId; }
        public int getUserId() { return userId_; }
        public void setSortOrder(int sortOrder) { sortOrder_ = sortOrder; }
        public int getSortOrder() { return sortOrder_; }
        public void setGroupId(int groupId) { groupId_ = groupId; }
        public int getGroupId() { return groupId_; }
        public void setAvatar(byte[] avatar) { avatar_ = avatar; }
        public byte[] getAvatar() { return avatar_; }
    }

    private <T> CrudEntity<T> entity(Class<T> beanClass) {
        return new CrudEntity<>(beanClass, new CrudEntityOptions<>());
    }

    @Test
    void testResolvesTheMappedColumnNames() {
        var entity = entity(Mapped.class);

        // a property that is mapped to another column has to reach the
        // database under the name that it's stored with
        assertEquals("user_id", entity.getIdentifierColumn());
        assertEquals("sort_order", entity.getOrdinalColumn());
        assertEquals("group_id", entity.getOrdinalRestrictionColumn());
        // while an unmapped property keeps its own name
        assertEquals("title", entity(Chapter.class).getColumnName("title"));
        assertEquals("id", entity(Chapter.class).getIdentifierColumn());
        assertNull(entity(CrudSubscriber.class).getOrdinalColumn());
    }

    @Test
    void testTheRestrictionIsOnlySubmittedWhenAdding() {
        var entity = entity(Chapter.class);

        // an instance is placed in its list when it's created, changing it
        // afterwards would leave its ordinal behind in the old list
        assertTrue(entity.getAddableProperties().contains("bookId"));
        assertFalse(entity.getEditableProperties().contains("bookId"));
        // and the ordinal itself is never part of a form
        assertFalse(entity.getAddableProperties().contains("position"));
        assertFalse(entity.getEditableProperties().contains("position"));
    }

    @Test
    void testFilePropertiesArentPartOfTheForms() {
        var entity = entity(Mapped.class);

        // a field whose content would be silently dropped isn't rendered at
        // all as long as the forms don't upload
        assertFalse(entity.getAddableProperties().contains("avatar"));
        assertFalse(entity.getEditableProperties().contains("avatar"));
    }

    @Test
    void testRegisteredOptionsCantBeChangedAnymore() {
        var options = new CrudEntityOptions<CrudArticle>()
            .action("publish", "Publish", (c, article) -> null);
        var action = options.getActions().get(0);
        var admin = new CrudAdmin(TestDatasources.H2).entity(CrudArticle.class, options);
        var entity = admin.getEntities().get(0);

        // a change that comes after the routes were set up would describe an
        // administration that isn't mounted anywhere
        assertThrows(ConfigurationRegisteredException.class, () -> entity.getOptions().pageSize(50));
        assertThrows(ConfigurationRegisteredException.class, () -> entity.getOptions().section("Late", "title"));
        assertThrows(ConfigurationRegisteredException.class, () -> entity.getOptions().column("title", (article, value) -> null));
        assertThrows(ConfigurationRegisteredException.class,
            () -> entity.getOptions().action("archive", "Archive", (c, article) -> null));
        assertThrows(ConfigurationRegisteredException.class, () -> options.pageSize(50));
        assertThrows(ConfigurationRegisteredException.class, () -> action.confirm());
        assertThrows(ConfigurationRegisteredException.class, () -> action.visibleWhen(article -> false));
    }

    @Test
    void testTheActionBuilderIsCheckedBeforeItRuns() {
        var options = new CrudEntityOptions<CrudArticle>();
        assertThrows(IllegalArgumentException.class,
            () -> options.action("publish", "Publish", (c, article) -> null, null));

        var admin = new CrudAdmin(TestDatasources.H2).entity(CrudArticle.class, options);
        var registered = admin.getEntities().get(0).getOptions();

        // the builder doesn't run at all when it has nothing to configure
        var ran = new boolean[]{false};
        assertThrows(ConfigurationRegisteredException.class,
            () -> registered.action("publish", "Publish", (c, article) -> null, a -> ran[0] = true));
        assertFalse(ran[0]);
    }

    @Test
    void testARefusedRegistrationLeavesItsOptionsChangeable() {
        var admin = new CrudAdmin(TestDatasources.H2).entity(CrudArticle.class);

        var options = new CrudEntityOptions<CrudArticle>()
            .action("publish", "Publish", (c, article) -> null);
        var action = options.getActions().get(0);

        // the slug is already taken, so this registration is refused
        assertThrows(DuplicateSlugException.class, () -> admin.entity(CrudArticle.class, options));

        // and what it was given can still be corrected
        assertDoesNotThrow(() -> options.slug("articles").pageSize(5));
        assertDoesNotThrow(() -> action.confirm());
        assertDoesNotThrow(() -> admin.entity(CrudArticle.class, options));
    }

    @Test
    void testARegisteredActionKeepsBeingTheOneThatWasProvided() {
        var action = new PublishDrafts();
        var options = new CrudEntityOptions<CrudArticle>().action(action);
        var admin = new CrudAdmin(TestDatasources.H2).entity(CrudArticle.class, options);

        // an action is allowed to be a class of its own, which copying it
        // into a plain one would leave behind
        @SuppressWarnings("unchecked")
        var entity = (CrudEntity<CrudArticle>) admin.getEntities().get(0);
        var registered = entity.getOptions().getActions().get(0);
        assertSame(action, registered);

        var draft = new CrudArticle();
        draft.setStatus("draft");
        var published = new CrudArticle();
        published.setStatus("published");
        assertTrue(registered.isVisible(draft));
        assertFalse(registered.isVisible(published));

        // what it describes stays what was registered, since changing it
        // afterwards is refused rather than copied away
        assertThrows(ConfigurationRegisteredException.class, action::confirm);
        assertFalse(registered.isConfirmed());
    }

    public static class PublishDrafts extends CrudAction<CrudArticle> {
        public PublishDrafts() {
            super("publish", "Publish", (c, article) -> null);
        }

        public boolean isVisible(CrudArticle instance) {
            return "draft".equals(instance.getStatus());
        }
    }

    @Test
    void testConstraintsCantBeChangedThroughTheEntity() {
        var entity = entity(Mapped.class);

        // a changed constraint would describe an entity that differs from
        // the one whose columns were derived when it was registered
        entity.getConstraint("groupId").columnName("something_else");
        assertEquals("group_id", entity.getOrdinalRestrictionColumn());
        assertEquals("group_id", entity.getConstraint("groupId").getColumnName());
    }

    @Test
    void testFractionalNumbersAcceptTheirOwnSteps() {
        var builder = new CrudFormBuilder(entity(Priced.class), TemplateFactory.HTML.get("crud.fields"), true);
        var html = builder.generateFields(new Priced());

        // a number input only accepts whole numbers without a step
        assertTrue(html.contains("name=\"amount\""));
        assertTrue(html.contains("step=\"any\""));
    }

    @Test
    void testRejectsARolePropertyThatIsntStored() {
        // the column has to exist for the queries that are generated
        var e = assertThrows(UnstoredPropertyException.class, () -> entity(UnstoredOrdinal.class));
        assertTrue(e.getMessage().contains("isn't stored"));
    }

    @Test
    void testAHiddenMandatoryPropertyNeedsAValueOfItsOwn() {
        // a default of the constraints is only rendered into a field that is
        // shown, so it can't fill in for a field that isn't
        var e = assertThrows(MandatoryPropertyWithoutFieldException.class, () -> entity(HiddenMandatory.class));
        assertTrue(e.getMessage().contains("secret"));

        // while a value that the constructor gives it is enough
        assertDoesNotThrow(() -> entity(HiddenConstructed.class));
    }

    @Test
    void testAHiddenPropertyIsHeldToEveryRuleOfItsConstraints() {
        // an addition is refused by every rule that a constraint generates,
        // not only by the ones about holding nothing
        var e = assertThrows(MandatoryPropertyWithoutFieldException.class, () -> entity(HiddenTooShort.class));
        assertTrue(e.getMessage().contains("code"));

        // and a value that those rules accept is accepted here too
        assertDoesNotThrow(() -> entity(HiddenEmptyCollection.class));
    }

    @Test
    void testAHiddenValueIsntJudgedByWhatAFormStillFillsIn() {
        // the form fills in the property that this one has to match, so a new
        // instance says nothing about whether an addition can succeed
        assertDoesNotThrow(() -> entity(SameAsHidden.class));
    }

    @Test
    void testTwoHiddenPropertiesThatHaveToMatchAreStillChecked() {
        // no field fills either of them in, so nothing can make them match
        var e = assertThrows(MandatoryPropertyWithoutFieldException.class, () -> entity(SameAsHiddenPair.class));
        assertTrue(e.getMessage().contains("channel"));
    }

    @Test
    void testAHiddenValueThatHasToMatchTheOrdinalIsRefused() {
        // the ordinal is handed out one number at a time while the hidden
        // value keeps what a new instance gave it, so at most one addition
        // could ever match and the rest can't be corrected on any field
        var e = assertThrows(MandatoryPropertyWithoutFieldException.class, () -> entity(SameAsOrdinal.class));
        assertTrue(e.getMessage().contains("mirror"));
    }

    @Test
    void testTwoPropertiesCantBothReportAsTheIdentifier() {
        // an addition leaves out what is wrong with the identifier by the
        // name that those errors carry, which has to name only the identifier
        var e = assertThrows(DuplicateSubjectException.class, () -> entity(SharedIdentifierSubject.class));
        assertTrue(e.getMessage().contains("id"));
    }

    @Test
    void testASubjectThatNoPropertyAnswersToIsStillReportable() {
        // an error can carry a subject of its own that nothing claims, and
        // what is wrong still has to reach the page instead of an exception
        var entity = entity(NamedSubject.class);
        assertEquals("", entity.propertyOfSubject(""));
        assertEquals("", entity.getPropertyLabel(""));
        assertEquals("", entity.getPropertyLabel(null));
        // and the ones that do answer keep reading the way they did
        assertEquals("Email", entity.getPropertyLabel("email"));
    }

    @Test
    void testABeanThatValidatesNothingIsntAdministered() {
        // it has nowhere to report a value that a submission couldn't be read
        // into, so every operation on it would report that it succeeded
        var e = assertThrows(UnvalidatedBeanException.class, () -> entity(NotValidated.class));
        assertTrue(e.getMessage().contains(NotValidated.class.getName()));
    }

    public static class NotValidated {
        private int id_ = -1;

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
    }

    @Test
    void testABeanThatCantBeBuiltIsntAdministered() {
        // every operation constructs an instance, so a bean without a
        // constructor to do it with is refused where it's registered instead
        // of on the first request that reaches it
        var e = assertThrows(InstanceCreationException.class, () -> entity(Unbuildable.class));
        assertTrue(e.getMessage().contains(Unbuildable.class.getName()));
    }

    public static class Unbuildable extends DelegatedValidation {
        private int id_ = -1;

        public Unbuildable(int id) { id_ = id; }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
    }

    /**
     * Validates through an instance of its own instead of through the
     * hierarchy, which is the only shape of bean that validates without also
     * being constrained.
     */
    public static abstract class DelegatedValidation implements Validated {
        private final Validation delegate_ = new Validation();

        public boolean validate() { return delegate_.validate(); }
        public boolean validate(ValidationContext context) { return delegate_.validate(context); }
        public void addRule(ValidationRule rule) { delegate_.addRule(rule); }
        public List<ValidationRule> getRules() { return delegate_.getRules(); }
        public void resetValidation() { delegate_.resetValidation(); }
        public void addValidationError(ValidationError error) { delegate_.addValidationError(error); }
        public Set<ValidationError> getValidationErrors() { return delegate_.getValidationErrors(); }
        public int countValidationErrors() { return delegate_.countValidationErrors(); }
        public void replaceValidationErrors(Set<ValidationError> errors) { delegate_.replaceValidationErrors(errors); }
        public void limitSubjectErrors(String subject) { delegate_.limitSubjectErrors(subject); }
        public void unlimitSubjectErrors(String subject) { delegate_.unlimitSubjectErrors(subject); }
        public List<String> getValidatedSubjects() { return delegate_.getValidatedSubjects(); }
        public boolean isSubjectValid(String subject) { return delegate_.isSubjectValid(subject); }
        public void makeErrorValid(String identifier, String subject) { delegate_.makeErrorValid(identifier, subject); }
        public void makeSubjectValid(String subject) { delegate_.makeSubjectValid(subject); }
        public void provideValidatedBean(Validated bean) { delegate_.provideValidatedBean(bean); }
        public Validated retrieveValidatedBean() { return delegate_.retrieveValidatedBean(); }
        public ValidationGroup addGroup(String name) { return delegate_.addGroup(name); }
        public void focusGroup(String name) { delegate_.focusGroup(name); }
        public void resetGroup(String name) { delegate_.resetGroup(name); }
        public Collection<ValidationGroup> getGroups() { return delegate_.getGroups(); }
        public ValidationGroup getGroup(String name) { return delegate_.getGroup(name); }
        public boolean validateGroup(String name) { return delegate_.validateGroup(name); }
        public boolean validateGroup(String name, ValidationContext context) { return delegate_.validateGroup(name, context); }
        public List<PropertyValidationRule> addConstrainedPropertyRules(ConstrainedProperty property) { return delegate_.addConstrainedPropertyRules(property); }
        public List<PropertyValidationRule> generateConstrainedPropertyRules(ConstrainedProperty property) { return delegate_.generateConstrainedPropertyRules(property); }
        public Collection<String> getLoadingErrors(String propertyName) { return delegate_.getLoadingErrors(propertyName); }
    }

    @Test
    void testABeanWithCallbacksStillCantShareTheIdentifierSubject() {
        // what an addition leaves out is decided by the name that an error
        // carries, which callbacks have nothing to do with, so a value that
        // somebody has to correct would be dropped along with it
        var e = assertThrows(DuplicateSubjectException.class, () -> entity(CallbackSharedSubject.class));
        assertTrue(e.getMessage().contains("title"));
    }

    @Test
    void testAnIdentifierWithASubjectOfItsOwnIsStillLeftOut() {
        // the errors of the identifier carry the subject that its own
        // constraints give it, not the name of the property
        var entity = entity(NamedIdentifier.class);
        assertEquals("Number", entity.subjectOfProperty("id"));
        assertDoesNotThrow(() -> entity(NamedIdentifier.class));
    }

    public static class CallbackSharedSubject extends MetaData implements rife.database.querymanagers.generic.Callbacks<CallbackSharedSubject> {
        private int id_ = -1;
        private String title_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            // a callback can fill in what a new instance doesn't carry, but
            // it can't make two properties tell their errors apart
            addConstraint(new ConstrainedProperty("title").subjectName("id").regexp("[a-z]+"));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setTitle(String title) { title_ = title; }
        public String getTitle() { return title_; }

        public boolean beforeValidate(CallbackSharedSubject object) { return true; }
        public boolean afterRestore(CallbackSharedSubject object) { return true; }
        public boolean beforeInsert(CallbackSharedSubject object) { return true; }
        public boolean beforeDelete(int objectId) { return true; }
        public boolean beforeSave(CallbackSharedSubject object) { return true; }
        public boolean beforeUpdate(CallbackSharedSubject object) { return true; }
        public boolean afterValidate(CallbackSharedSubject object) { return true; }
        public boolean afterInsert(CallbackSharedSubject object, boolean success) { return true; }
        public boolean afterDelete(int objectId, boolean success) { return true; }
        public boolean afterSave(CallbackSharedSubject object, boolean success) { return true; }
        public boolean afterUpdate(CallbackSharedSubject object, boolean success) { return true; }
    }

    public static class SharedIdentifierSubject extends MetaData {
        private int id_ = -1;
        private String title_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            // it reports under the name of the identifier
            addConstraint(new ConstrainedProperty("title").subjectName("id"));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setTitle(String title) { title_ = title; }
        public String getTitle() { return title_; }
    }

    public static class NamedIdentifier extends MetaData {
        private int id_ = -1;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false)
                .subjectName("Number").rangeBegin(0).rangeEnd(9999));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
    }

    @Test
    void testAnIdentifierWithConstraintsOfItsOwnIsStillAdministered() {
        // the database assigns it, so what its own constraints make of the
        // value that says it isn't stored yet is nothing to refuse over
        assertDoesNotThrow(() -> entity(RangedIdentifier.class));
    }

    @Test
    void testASubjectThatAnUnconstrainedPropertyIsCalledIsntTranslated() {
        // what is reported about that property would otherwise be marked on
        // the field of another one
        var entity = entity(SubjectOfUnconstrained.class);
        assertEquals("plain", entity.propertyOfSubject("plain"));
    }

    @Test
    void testAMandatoryChoiceIsntOfferedAnEmptyOne() {
        var entity = entity(MandatoryChoice.class);
        var instance = new MandatoryChoice();

        var builder = new CrudFormBuilder(entity, TemplateFactory.HTML.get("crud.fields"), true);
        var fields = builder.generateFields(instance);

        // the list holds a null, which a property that has to hold something
        // can't be stored with
        assertFalse(fields.contains("<option value=\"\">"), fields);
        assertTrue(fields.contains("<option value=\"draft\""), fields);
        assertTrue(fields.contains("<option value=\"published\""), fields);
    }

    @Test
    void testASubjectThatSeveralPropertiesAnswerToIsntTranslated() {
        // translating it would attach what is wrong with one property to the
        // field of another one
        var shared = entity(AmbiguousSubjects.class);
        assertEquals("Shared", shared.propertyOfSubject("Shared"));

        // the same goes for a subject that is the name of another property
        var taken = entity(SubjectOfAnother.class);
        assertEquals("target", taken.propertyOfSubject("target"));
    }

    @Test
    void testASlugIsTheSameWhateverTheLocaleIs() {
        var previous = Locale.getDefault();
        try {
            // a Turkish locale lowercases an I to one without a dot, which
            // isn't a character that a URL part is allowed to hold
            Locale.setDefault(new Locale("tr", "TR"));
            assertEquals("invoice", entity(Invoice.class).getSlug());
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    void testABufferIsShownAsTheTextThatComesBack() {
        // a buffer is filled from the submitted text without its format
        // being consulted, so showing it through the format would store the
        // formatted text on the next unchanged submission
        var entity = entity(FormattedBuffer.class);
        var instance = new FormattedBuffer();
        instance.setNotes(new StringBuilder("plain text"));

        var builder = new CrudFormBuilder(entity, TemplateFactory.HTML.get("crud.fields"), false);
        var fields = builder.generateFields(instance);

        assertTrue(fields.contains("value=\"plain text\""), fields);
        assertFalse(fields.contains("&lt;&lt;plain text"), fields);
    }

    @Test
    void testADatasourceWithoutTransactionsIsntAdministeredWith() {
        // an operation and the hooks that follow it share one transaction, so
        // a driver that has none to share is refused rather than leaving what
        // an operation claims to be a claim
        var asked = new int[]{0};
        var admin = new CrudAdmin(transactionless(asked)).entity(CrudArticle.class);

        var e = assertThrows(TransactionsRequiredException.class, admin::verifyTransactions);
        assertTrue(e.getMessage().contains("transactions"));
        assertEquals(1, asked[0]);

        // the driver is asked once and remembered, since every operation goes
        // through this
        assertThrows(TransactionsRequiredException.class, admin::verifyTransactions);
        assertEquals(1, asked[0]);

        // while the datasources that these tests run against are administered
        new CrudAdmin(TestDatasources.H2).entity(CrudArticle.class).verifyTransactions();
    }

    /**
     * A datasource of connections that say they have no transactions, which
     * none of the databases that these tests run against do, and that count
     * how often one of them was asked.
     */
    private rife.database.Datasource transactionless(int[] asked) {
        var real = TestDatasources.H2;
        var source = new javax.sql.DataSource() {
            public java.sql.Connection getConnection()
            throws java.sql.SQLException {
                var connection = java.sql.DriverManager.getConnection(real.getUrl(), real.getUser(), real.getPassword());
                return (java.sql.Connection) java.lang.reflect.Proxy.newProxyInstance(
                    getClass().getClassLoader(), new Class<?>[]{java.sql.Connection.class},
                    (proxy, method, args) -> {
                        if ("getMetaData".equals(method.getName())) {
                            var data = connection.getMetaData();
                            return java.lang.reflect.Proxy.newProxyInstance(
                                getClass().getClassLoader(), new Class<?>[]{java.sql.DatabaseMetaData.class},
                                (p, m, a) -> {
                                    if ("supportsTransactions".equals(m.getName())) {
                                        asked[0]++;
                                        return Boolean.FALSE;
                                    }
                                    return m.invoke(data, a);
                                });
                        }
                        return method.invoke(connection, args);
                    });
            }

            public java.sql.Connection getConnection(String user, String password)
            throws java.sql.SQLException {
                return getConnection();
            }

            public java.util.logging.Logger getParentLogger() { return null; }
            public java.io.PrintWriter getLogWriter() { return null; }
            public void setLogWriter(java.io.PrintWriter out) { }
            public void setLoginTimeout(int seconds) { }
            public int getLoginTimeout() { return 0; }
            public <X> X unwrap(Class<X> iface) { return null; }
            public boolean isWrapperFor(Class<?> iface) { return false; }
        };

        return new rife.database.Datasource(source, 0);
    }

    @Test
    void testAFieldSaysWhatItIsFor() {
        // the constraints of a property say what a value of it has to be,
        // while this says what it means
        var options = new CrudEntityOptions<CrudArticle>()
            .help("title", "This is what the <readers> see first.");
        var entity = new CrudEntity<>(CrudArticle.class, options);

        var builder = new CrudFormBuilder(entity, TemplateFactory.HTML.get("crud.fields"), false);
        var fields = builder.generateFields(new CrudArticle());

        // it sits with the field that enters it, and what it says is text
        assertTrue(fields.contains("<p class=\"crud-help\">This is what the &lt;readers&gt; see first.</p>"), fields);

        // while the fields that nothing was said about say nothing
        var quiet = new CrudFormBuilder(entity(CrudArticle.class), TemplateFactory.HTML.get("crud.fields"), false)
            .generateFields(new CrudArticle());
        assertFalse(quiet.contains("crud-help"), quiet);

        // and help that names a property nobody has is the typo that it is
        var e = assertThrows(UnknownPropertyException.class, () -> new CrudEntity<>(CrudArticle.class,
            new CrudEntityOptions<CrudArticle>().help("tilte", "This never renders.")));
        assertTrue(e.getMessage().contains("tilte"), e.getMessage());
    }

    @Test
    void testFieldsSitTogetherInTheSectionThatClaimsThem() {
        // the section lists body before title on purpose, so that the order
        // it wants is told apart from the order the form falls back to
        var options = new CrudEntityOptions<CrudArticle>()
            .section("Content <first>", "body", "title")
            .section("Publication", "status", "featured");
        var entity = new CrudEntity<>(CrudArticle.class, options);

        var fields = new CrudFormBuilder(entity, TemplateFactory.HTML.get("crud.fields"), false)
            .generateFields(new CrudArticle());

        // the properties that no section claims keep their place at the top,
        // and what a label says is text
        var author = fields.indexOf("crud-author");
        var content = fields.indexOf("<fieldset class=\"crud-section\"><legend>Content &lt;first&gt;</legend>");
        var publication = fields.indexOf("<legend>Publication</legend>");
        assertTrue(author >= 0 && content > author, fields);
        // the sections follow each other in the order they were declared
        assertTrue(publication > content, fields);
        // and a section shows its fields in the order that it lists them
        var body = fields.indexOf("crud-body");
        var title = fields.indexOf("crud-title");
        assertTrue(content < body && body < title && title < publication, fields);

        // a form that no section was declared for is one flat run of fields
        var flat = new CrudFormBuilder(entity(CrudArticle.class), TemplateFactory.HTML.get("crud.fields"), false)
            .generateFields(new CrudArticle());
        assertFalse(flat.contains("crud-section"), flat);

        // a section that names a property nobody has is the typo that it is
        var e = assertThrows(UnknownPropertyException.class, () -> new CrudEntity<>(CrudArticle.class,
            new CrudEntityOptions<CrudArticle>().section("Content", "tilte")));
        assertTrue(e.getMessage().contains("tilte"), e.getMessage());

        // while one whose properties the form keeps out isn't rendered at all
        var placed = new CrudEntity<>(CrudArticle.class,
            new CrudEntityOptions<CrudArticle>().section("Placement", "ordinal"));
        var quiet = new CrudFormBuilder(placed, TemplateFactory.HTML.get("crud.fields"), false)
            .generateFields(new CrudArticle());
        assertFalse(quiet.contains("crud-section"), quiet);
    }

    @Test
    void testASectionFollowsWhatEachFormEnters() {
        // the list that an instance is placed in is entered when it's added
        // and can't be changed afterwards, so its section comes and goes with
        // that field
        var chapter = new CrudEntity<>(Chapter.class,
            new CrudEntityOptions<Chapter>().section("Placement", "bookId"));
        var add = new CrudFormBuilder(chapter, TemplateFactory.HTML.get("crud.fields"), true)
            .generateFields(new Chapter());
        var edit = new CrudFormBuilder(chapter, TemplateFactory.HTML.get("crud.fields"), false)
            .generateFields(new Chapter());

        assertTrue(add.contains("<legend>Placement</legend>"), add);
        assertFalse(edit.contains("crud-section"), edit);
    }

    public static class Timed extends MetaData {
        private int id_ = -1;
        private java.util.Date publishAt_ = null;
        private java.time.LocalDate eventDay_ = null;
        private java.time.LocalTime doorsOpen_ = null;
        private java.time.LocalDate plainDay_ = null;
        private java.util.Date longForm_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("publishAt").format(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm")));
            addConstraint(new ConstrainedProperty("eventDay").format(new java.text.SimpleDateFormat("yyyy-MM-dd")));
            addConstraint(new ConstrainedProperty("doorsOpen").format(new java.text.SimpleDateFormat("HH:mm")));
            addConstraint(new ConstrainedProperty("longForm").format(new java.text.SimpleDateFormat("dd/MM/yyyy")));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setPublishAt(java.util.Date publishAt) { publishAt_ = publishAt; }
        public java.util.Date getPublishAt() { return publishAt_; }
        public void setEventDay(java.time.LocalDate eventDay) { eventDay_ = eventDay; }
        public java.time.LocalDate getEventDay() { return eventDay_; }
        public void setDoorsOpen(java.time.LocalTime doorsOpen) { doorsOpen_ = doorsOpen; }
        public java.time.LocalTime getDoorsOpen() { return doorsOpen_; }
        public void setPlainDay(java.time.LocalDate plainDay) { plainDay_ = plainDay; }
        public java.time.LocalDate getPlainDay() { return plainDay_; }
        public void setLongForm(java.util.Date longForm) { longForm_ = longForm; }
        public java.util.Date getLongForm() { return longForm_; }
    }

    @Test
    void testADateEntersThroughTheControlOfItsFormat() {
        var entity = entity(Timed.class);
        var instance = new Timed();
        instance.setPublishAt(new java.util.GregorianCalendar(2026, java.util.Calendar.AUGUST, 23, 14, 30).getTime());
        instance.setEventDay(java.time.LocalDate.of(2026, 8, 23));
        instance.setDoorsOpen(java.time.LocalTime.of(9, 30));
        instance.setLongForm(new java.util.GregorianCalendar(2026, java.util.Calendar.AUGUST, 23).getTime());

        var fields = new CrudFormBuilder(entity, TemplateFactory.HTML.get("crud.fields"), false)
            .generateFields(instance);

        // a control that submits exactly what the format reads is offered
        assertTrue(fields.contains("name=\"eventDay\" type=\"date\" value=\"2026-08-23\""), fields);
        assertTrue(fields.contains("name=\"doorsOpen\" type=\"time\" value=\"09:30\""), fields);
        assertTrue(fields.contains("name=\"publishAt\" type=\"datetime-local\" value=\"2026-08-23T14:30\""), fields);
        // any other format keeps the text field that shows it
        assertTrue(fields.contains("name=\"longForm\" type=\"text\" value=\"23/08/2026\""), fields);
        // and so does a date without a format of its own, since the default
        // differs from every control's by its separator
        assertTrue(fields.contains("name=\"plainDay\" type=\"text\""), fields);
    }

    public static class Summarized extends MetaData {
        private int id_ = -1;
        private String title_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("title").listed(true));
            addConstraint(new ConstrainedProperty("summary").listed(true).persistent(false).editable(false));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setTitle(String title) { title_ = title; }
        public String getTitle() { return title_; }
        public void setSummary(String summary) { }
        public String getSummary() { return title_ + "!"; }
    }

    @Test
    void testWhatCanBeSearchedAndSortedFollowsTheTable() {
        var entity = entity(CrudArticle.class);

        // a search matches the columns of the table that hold text, so a
        // match never has to be explained by a column that isn't there
        assertEquals(List.of("title", "author", "status"), entity.getSearchableProperties());

        // the database does the sorting, so a column it keeps is sortable
        assertTrue(entity.isSortable("title"));
        assertTrue(entity.isSortable("featured"));
        // while what isn't a column, or isn't kept, isn't
        assertFalse(entity.isSortable("body"));
        assertFalse(entity.isSortable("ordinal"));
        assertFalse(entity.isSortable("id"));
        assertFalse(entity.isSortable("nonsense"));

        // a computed column reads just as well as a stored one, but the
        // database that matches and sorts never sees it
        var summarized = entity(Summarized.class);
        assertTrue(summarized.getListedProperties().contains("summary"));
        assertEquals(List.of("title"), summarized.getSearchableProperties());
        assertTrue(summarized.isSortable("title"));
        assertFalse(summarized.isSortable("summary"));
    }

    @Test
    void testAColumnRendererNeedsAColumnToRender() {
        // a renderer that names a property nobody has is the typo that it is
        var unknown = assertThrows(UnknownPropertyException.class, () -> new CrudEntity<>(CrudArticle.class,
            new CrudEntityOptions<CrudArticle>().column("stauts", (article, value) -> null)));
        assertTrue(unknown.getMessage().contains("stauts"), unknown.getMessage());

        // while one for a property that the browse table doesn't show would
        // never run at all
        var unlisted = assertThrows(UnlistedColumnException.class, () -> new CrudEntity<>(CrudArticle.class,
            new CrudEntityOptions<CrudArticle>().column("body", (article, value) -> null)));
        assertEquals("body", unlisted.getProperty());
    }

    @Test
    void testACellRefusesWhatItCantHold() {
        assertThrows(IllegalArgumentException.class, () -> CrudCell.text(null));
        assertThrows(IllegalArgumentException.class, () -> CrudCell.badge(null));
        assertThrows(IllegalArgumentException.class, () -> CrudCell.html(null));

        // the style ends up inside the class attribute of the badge
        var e = assertThrows(IllegalArgumentException.class, () -> CrudCell.badge("Draft", "no good"));
        assertTrue(e.getMessage().contains("no good"), e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> CrudCell.badge("Draft", "bad\"quote"));
    }

    @Test
    void testAPropertyBelongsToASingleSection() {
        var options = new CrudEntityOptions<CrudArticle>().section("Content", "title", "body");

        // a property that two sections claim would show its field twice
        var claimed = assertThrows(PropertyInMultipleSectionsException.class,
            () -> options.section("Extra", "body"));
        assertEquals("body", claimed.getProperty());
        assertEquals("Content", claimed.getSection());
        assertThrows(PropertyInMultipleSectionsException.class,
            () -> new CrudEntityOptions<CrudArticle>().section("Content", "title", "title"));

        // and two sections with the same label read as one section
        assertThrows(DuplicateSectionException.class, () -> options.section("Content", "status"));
    }

    @Test
    void testWhatTheAdministrationCantShowIsSaidOutLoud() {
        // a property that is quietly left out of every page is one that
        // nobody is able to look for, so the reason of each is kept
        var entity = entity(Related.class);
        var undisplayable = entity.getUndisplayableProperties();

        assertEquals(java.util.Set.of("avatar", "readers", "tags", "aliases"), undisplayable.keySet());
        assertTrue(undisplayable.get("avatar").contains("uploads"), undisplayable.get("avatar"));
        assertTrue(undisplayable.get("readers").contains("picks it"), undisplayable.get("readers"));
        assertTrue(undisplayable.get("tags").contains("picks them"), undisplayable.get("tags"));
        assertTrue(undisplayable.get("aliases").contains("several values"), undisplayable.get("aliases"));

        // a relationship that points at one other instance is stored as the
        // identifier of it, so it shows and enters as the number that it is
        assertFalse(undisplayable.containsKey("author"), "a manyToOne is the number that it is");
        assertTrue(entity.getAddableProperties().contains("author"));

        // and the pages leave out exactly what it names
        for (var property : undisplayable.keySet()) {
            assertFalse(entity.getListedProperties().contains(property), property);
            assertFalse(entity.getAddableProperties().contains(property), property);
            assertFalse(entity.getEditableProperties().contains(property), property);
        }

        // while the ones it does show say nothing of the sort
        assertTrue(entity.getAddableProperties().contains("title"));
        assertTrue(entity(CrudArticle.class).getUndisplayableProperties().isEmpty());
    }

    public static class Related extends MetaData {
        private int id_ = -1;
        private String title_ = null;
        private byte[] avatar_ = null;
        private int author_ = -1;
        private int readers_ = -1;
        private java.util.List<String> tags_ = null;
        private String[] aliases_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("avatar").file(true));
            addConstraint(new ConstrainedProperty("author").manyToOne(CrudArticle.class));
            addConstraint(new ConstrainedProperty("readers").manyToOneAssociation());
            addConstraint(new ConstrainedProperty("tags").manyToMany());
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setTitle(String title) { title_ = title; }
        public String getTitle() { return title_; }
        public void setAvatar(byte[] avatar) { avatar_ = avatar; }
        public byte[] getAvatar() { return avatar_; }
        public void setAuthor(int author) { author_ = author; }
        public int getAuthor() { return author_; }
        public void setReaders(int readers) { readers_ = readers; }
        public int getReaders() { return readers_; }
        public void setTags(java.util.List<String> tags) { tags_ = tags; }
        public java.util.List<String> getTags() { return tags_; }
        public void setAliases(String[] aliases) { aliases_ = aliases; }
        public String[] getAliases() { return aliases_; }
    }

    @Test
    void testAnEnumOffersWhatItIsAbleToHold() {
        // the values of an enum are the only ones that the property can hold,
        // so they're offered instead of being typed out by hand
        var entity = entity(Scheduled.class);
        var instance = new Scheduled();
        instance.setDay(java.time.DayOfWeek.FRIDAY);
        instance.setMood(Mood.CALM);

        var builder = new CrudFormBuilder(entity, TemplateFactory.HTML.get("crud.fields"), false);
        var fields = builder.generateFields(instance);

        assertTrue(fields.contains("<select class=\"crud-select\" id=\"crud-day\""), fields);
        assertTrue(fields.contains("value=\"MONDAY\""), fields);
        assertTrue(fields.contains("value=\"FRIDAY\" selected"), fields);
        assertFalse(fields.contains("id=\"crud-day\" name=\"day\" type=\"text\""), fields);

        // the option's label comes from the format, while its value stays
        // the one that a submission is read from
        assertTrue(fields.contains("<option value=\"CALM\" selected>Relaxed</option>"), fields);
        assertTrue(fields.contains("<option value=\"EAGER\">Keen</option>"), fields);
    }

    public enum Mood { CALM, EAGER }

    public static class Scheduled extends MetaData {
        private int id_ = -1;
        private java.time.DayOfWeek day_ = null;
        private Mood mood_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("mood").format(new java.text.Format() {
                public StringBuffer format(Object object, StringBuffer buffer, java.text.FieldPosition position) {
                    return buffer.append(Mood.CALM == object ? "Relaxed" : "Keen");
                }

                public Object parseObject(String source, java.text.ParsePosition position) {
                    position.setIndex(source.length());
                    return "Relaxed".equals(source) ? Mood.CALM : Mood.EAGER;
                }
            }));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setDay(java.time.DayOfWeek day) { day_ = day; }
        public java.time.DayOfWeek getDay() { return day_; }
        public void setMood(Mood mood) { mood_ = mood; }
        public Mood getMood() { return mood_; }
    }

    @Test
    void testACharacterFieldCantBeGivenMoreThanOneCharacter() {
        // the field of a property that holds one character says so, since text
        // that holds more is refused rather than shortened to what fits
        var entity = entity(SingleCharacter.class);
        var builder = new CrudFormBuilder(entity, TemplateFactory.HTML.get("crud.fields"), false);
        var fields = builder.generateFields(new SingleCharacter());

        assertTrue(fields.contains("name=\"grade\" type=\"text\" value=\"a\" maxlength=\"1\""), fields);
        // the kind of control that a property gets is on the control itself,
        // so the styles of an application reach it without reaching through
        // what happens to be around it
        assertTrue(fields.contains("<input class=\"crud-input\""), fields);
        assertTrue(fields.contains("name=\"boxedGrade\" type=\"text\" value=\"\" maxlength=\"1\""), fields);
        // while the length that the constraints of a text give it is its own
        assertTrue(fields.contains("name=\"name\" type=\"text\" value=\"\" maxlength=\"20\""), fields);
    }

    public static class SingleCharacter extends MetaData {
        private int id_ = -1;
        private char grade_ = 'a';
        private Character boxedGrade_ = null;
        private String name_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("name").maxLength(20));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setGrade(char grade) { grade_ = grade; }
        public char getGrade() { return grade_; }
        public void setBoxedGrade(Character boxedGrade) { boxedGrade_ = boxedGrade; }
        public Character getBoxedGrade() { return boxedGrade_; }
        public void setName(String name) { name_ = name; }
        public String getName() { return name_; }
    }

    @Test
    void testEveryKindOfControlSaysWhatKindItIs() {
        var entity = entity(rifetestmodels.CrudPriced.class);
        var builder = new CrudFormBuilder(entity, TemplateFactory.HTML.get("crud.fields"), false);
        var fields = builder.generateFields(new rifetestmodels.CrudPriced());

        assertTrue(fields.contains("<input class=\"crud-input\""), fields);
        assertTrue(fields.contains("<select class=\"crud-select\""), fields);
        assertTrue(fields.contains("<input type=\"checkbox\" class=\"crud-checkbox\""), fields);
    }

    @Test
    void testATypeThatOnlyItsFormatReadsStillSelectsItsOption() {
        // an option is read the way a submission reads that same text, so a
        // type that nothing constructs from a string of its own still says
        // the same thing in the option as the property holds
        var entity = entity(FormattedTag.class);
        var instance = new FormattedTag();
        instance.setTag(new Tag("beta"));

        var builder = new CrudFormBuilder(entity, TemplateFactory.HTML.get("crud.fields"), false);
        var fields = builder.generateFields(instance);

        assertTrue(fields.contains("value=\"#beta\" selected"), fields);
        assertTrue(fields.contains("value=\"#alpha\""), fields);
        assertFalse(fields.contains("value=\"beta\""), fields);
    }

    public static class FormattedTag extends MetaData {
        private int id_ = -1;
        private Tag tag_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("tag").inList("alpha", "beta").format(new TagFormat()));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setTag(Tag tag) { tag_ = tag; }
        public Tag getTag() { return tag_; }
    }

    /**
     * A type that only the format of its property is able to read from text,
     * since nothing constructs one from a string of its own.
     */
    public static class Tag {
        private final String name_;

        public Tag(String name) { name_ = name; }
        public String getName() { return name_; }
    }

    public static class TagFormat extends java.text.Format {
        public StringBuffer format(Object object, StringBuffer buffer, java.text.FieldPosition position) {
            return buffer.append("#").append(((Tag) object).getName());
        }

        public Object parseObject(String source, java.text.ParsePosition position) {
            position.setIndex(source.length());
            return new Tag(source.startsWith("#") ? source.substring(1) : source);
        }
    }

    @Test
    void testAPropertyWithASubjectOfItsOwnIsStillItsOwnField() {
        // errors name the subject that a constraint carries, which isn't the
        // name of the property that the form fills in
        var entity = entity(NamedSubject.class);
        assertEquals("email", entity.propertyOfSubject("Email address"));
        // and a subject that nothing claims stays what it was
        assertEquals("id", entity.propertyOfSubject("id"));
    }

    @Test
    void testAValueThatACallbackFillsInIsLeftToTheCallback() {
        // the validation of an addition runs the callbacks of the bean, so a
        // property that one of them fills in isn't one that no addition can
        // provide
        assertDoesNotThrow(() -> entity(CallbackFilled.class));
    }

    @Test
    void testAHiddenPropertyNeedsAValueThatItCanBeStoredWith() {
        // an addition is validated before it's stored, so a value that a new
        // instance carries but isn't allowed to keep refuses every addition
        // just as much as one that is missing
        var e = assertThrows(MandatoryPropertyWithoutFieldException.class, () -> entity(HiddenUnsatisfiable.class));
        assertTrue(e.getMessage().contains("code"));
    }

    public static class UnstoredOrdinal extends MetaData {
        private int id_ = -1;
        private int position_ = -1;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("position").ordinal(true).persistent(false));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setPosition(int position) { position_ = position; }
        public int getPosition() { return position_; }
    }

    public static class Invoice extends MetaData {
        private int id_ = -1;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
    }

    public static class HiddenTooShort extends MetaData {
        private int id_ = -1;
        private String code_ = "";

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            // it isn't empty as far as this administration used to look, but
            // it's still nothing that the property is allowed to hold
            addConstraint(new ConstrainedProperty("code").minLength(1).editable(false));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setCode(String code) { code_ = code; }
        public String getCode() { return code_; }
    }

    public static class HiddenEmptyCollection extends MetaData {
        private int id_ = -1;
        private String title_ = "a title";

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("title").notEmpty(true).editable(false));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setTitle(String title) { title_ = title; }
        public String getTitle() { return title_; }
    }

    public static class SameAsHidden extends MetaData {
        private int id_ = -1;
        private String channel_ = "main";
        private String preference_ = "not chosen yet";

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            // it has to end up holding what the form fills in, which still
            // holds something else of its own until it does
            addConstraint(new ConstrainedProperty("channel").sameAs("preference").editable(false));
            addConstraint(new ConstrainedProperty("preference"));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setChannel(String channel) { channel_ = channel; }
        public String getChannel() { return channel_; }
        public void setPreference(String preference) { preference_ = preference; }
        public String getPreference() { return preference_; }
    }

    public static class SameAsHiddenPair extends MetaData {
        private int id_ = -1;
        private String channel_ = "main";
        private String mirror_ = "other";

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            // neither of them is filled in by the form, so every addition is
            // held to what they were constructed with
            addConstraint(new ConstrainedProperty("channel").sameAs("mirror").editable(false));
            addConstraint(new ConstrainedProperty("mirror").editable(false));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setChannel(String channel) { channel_ = channel; }
        public String getChannel() { return channel_; }
        public void setMirror(String mirror) { mirror_ = mirror; }
        public String getMirror() { return mirror_; }
    }

    public static class SameAsOrdinal extends MetaData {
        private int id_ = -1;
        private int position_ = -1;
        private int mirror_ = 7;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("position").ordinal(true));
            // the ordinal is handed out before an addition is validated, so
            // what a new instance carries for it says nothing about this
            addConstraint(new ConstrainedProperty("mirror").sameAs("position").editable(false));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setPosition(int position) { position_ = position; }
        public int getPosition() { return position_; }
        public void setMirror(int mirror) { mirror_ = mirror; }
        public int getMirror() { return mirror_; }
    }

    public static class RangedIdentifier extends MetaData {
        private int id_ = -1;

        public void activateMetaData() {
            // an addition marks the instance as not stored yet before it
            // validates, which this refuses
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false).rangeBegin(0).rangeEnd(9999));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
    }

    public static class SubjectOfUnconstrained extends MetaData {
        private int id_ = -1;
        private String alias_ = null;
        private String plain_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            // it answers to the name of a property that carries no
            // constraints of its own
            addConstraint(new ConstrainedProperty("alias").subjectName("plain"));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setAlias(String alias) { alias_ = alias; }
        public String getAlias() { return alias_; }
        public void setPlain(String plain) { plain_ = plain; }
        public String getPlain() { return plain_; }
    }

    public static class MandatoryChoice extends MetaData {
        private int id_ = -1;
        private String status_ = "draft";

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            // the list offers the choice that isn't one of the others, which
            // a property that has to hold something can't keep
            addConstraint(new ConstrainedProperty("status").notNull(true).inList("draft", null, "published"));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setStatus(String status) { status_ = status; }
        public String getStatus() { return status_; }
    }

    public static class AmbiguousSubjects extends MetaData {
        private int id_ = -1;
        private String first_ = null;
        private String second_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            // both answer to the same name, so neither of them owns it
            addConstraint(new ConstrainedProperty("first").subjectName("Shared"));
            addConstraint(new ConstrainedProperty("second").subjectName("Shared"));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setFirst(String first) { first_ = first; }
        public String getFirst() { return first_; }
        public void setSecond(String second) { second_ = second; }
        public String getSecond() { return second_; }
    }

    public static class SubjectOfAnother extends MetaData {
        private int id_ = -1;
        private String alias_ = null;
        private String target_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            // it answers to the name of another property of the same bean
            addConstraint(new ConstrainedProperty("alias").subjectName("target"));
            addConstraint(new ConstrainedProperty("target").notNull(true));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setAlias(String alias) { alias_ = alias; }
        public String getAlias() { return alias_; }
        public void setTarget(String target) { target_ = target; }
        public String getTarget() { return target_; }
    }

    public static class FormattedBuffer extends MetaData {
        private int id_ = -1;
        private StringBuilder notes_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            // the text of a buffer is assigned as it arrives, the format only
            // decides how it's written out
            addConstraint(new ConstrainedProperty("notes").format(new QuotedFormat()).maxLength(80));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setNotes(StringBuilder notes) { notes_ = notes; }
        public StringBuilder getNotes() { return notes_; }
    }

    public static class QuotedFormat extends java.text.Format {
        public StringBuffer format(Object object, StringBuffer buffer, java.text.FieldPosition position) {
            return buffer.append("<<").append(object).append(">>");
        }

        public Object parseObject(String source, java.text.ParsePosition position) {
            position.setIndex(source.length());
            return source;
        }
    }

    public static class NamedSubject extends MetaData {
        private int id_ = -1;
        private String email_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            // what is reported about this property isn't called by its name
            addConstraint(new ConstrainedProperty("email").notNull(true).subjectName("Email address"));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setEmail(String email) { email_ = email; }
        public String getEmail() { return email_; }
    }

    public static class CallbackFilled extends MetaData implements rife.database.querymanagers.generic.Callbacks<CallbackFilled> {
        private int id_ = -1;
        private String stamp_ = "";

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            // it carries nothing that it's allowed to be stored with, and no
            // field is generated for it, but a callback fills it in before
            // anything is validated
            addConstraint(new ConstrainedProperty("stamp").notEmpty(true).editable(false));
        }

        public boolean beforeValidate(CallbackFilled object) {
            object.setStamp("filled in");
            return true;
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setStamp(String stamp) { stamp_ = stamp; }
        public String getStamp() { return stamp_; }

        public boolean afterRestore(CallbackFilled object) { return true; }
        public boolean beforeInsert(CallbackFilled object) { return true; }
        public boolean beforeDelete(int objectId) { return true; }
        public boolean beforeSave(CallbackFilled object) { return true; }
        public boolean beforeUpdate(CallbackFilled object) { return true; }
        public boolean afterValidate(CallbackFilled object) { return true; }
        public boolean afterInsert(CallbackFilled object, boolean success) { return true; }
        public boolean afterDelete(int objectId, boolean success) { return true; }
        public boolean afterSave(CallbackFilled object, boolean success) { return true; }
        public boolean afterUpdate(CallbackFilled object, boolean success) { return true; }
    }

    public static class HiddenUnsatisfiable extends MetaData {
        private int id_ = -1;
        private String code_ = "";

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            // it carries a value, but not one that it's allowed to be stored
            // with, and no field is generated to correct it
            addConstraint(new ConstrainedProperty("code").notEmpty(true).editable(false));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setCode(String code) { code_ = code; }
        public String getCode() { return code_; }
    }

    public static class HiddenMandatory extends MetaData {
        private int id_ = -1;
        private String secret_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("secret").notNull(true).editable(false).defaultValue("hidden"));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setSecret(String secret) { secret_ = secret; }
        public String getSecret() { return secret_; }
    }

    public static class HiddenConstructed extends MetaData {
        private int id_ = -1;
        private String secret_ = "already there";

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("secret").notNull(true).editable(false));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setSecret(String secret) { secret_ = secret; }
        public String getSecret() { return secret_; }
    }

    @Test
    void testRejectsAmbiguousMetadata() {
        // this and the query manager would each pick a different one, so the
        // URLs would point at another property than the persistence uses
        var identifiers = assertThrows(DuplicateConstraintException.class, () -> entity(TwoIdentifiers.class));
        assertTrue(identifiers.getMessage().contains("identifier"));

        var ordinals = assertThrows(DuplicateConstraintException.class, () -> entity(TwoOrdinals.class));
        assertTrue(ordinals.getMessage().contains("ordinal"));
    }

    @Test
    void testRejectsARestrictionThatTheBeanDoesntHave() {
        var e = assertThrows(UnknownPropertyException.class, () -> entity(MissingScope.class));
        assertTrue(e.getMessage().contains("nosuchproperty"));
    }

    public static class TwoIdentifiers extends MetaData {
        private int id_ = -1;
        private int other_ = -1;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("other").identifier(true).editable(false));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setOther(int other) { other_ = other; }
        public int getOther() { return other_; }
    }

    public static class TwoOrdinals extends MetaData {
        private int id_ = -1;
        private int first_ = -1;
        private int second_ = -1;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("first").ordinal(true));
            addConstraint(new ConstrainedProperty("second").ordinal(true));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setFirst(int first) { first_ = first; }
        public int getFirst() { return first_; }
        public void setSecond(int second) { second_ = second; }
        public int getSecond() { return second_; }
    }

    public static class MissingScope extends MetaData {
        private int id_ = -1;
        private int position_ = -1;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("position").ordinal(true, "nosuchproperty"));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setPosition(int position) { position_ = position; }
        public int getPosition() { return position_; }
    }

    @Test
    void testAPrimitiveFallsBackToItsDeclaredDefault() {
        var builder = new CrudFormBuilder(entity(Defaulted.class), TemplateFactory.HTML.get("crud.fields"), true);
        var html = builder.generateFields(new Defaulted());

        // a primitive is never null, so carrying the value of a new instance
        // is what counts as not having been set
        assertTrue(html.contains("name=\"featured\""));
        assertTrue(html.contains("checked"));
        assertTrue(html.contains("value=\"7\""));

        // while a stored value is left exactly as it is
        var stored = new Defaulted();
        stored.setAmount(3);
        var edit = new CrudFormBuilder(entity(Defaulted.class), TemplateFactory.HTML.get("crud.fields"), false);
        assertTrue(edit.generateFields(stored).contains("value=\"3\""));
    }

    @Test
    void testAFormLevelMessageIsShownWithoutAnyValidation() {
        var builder = new CrudFormBuilder(entity(Plain.class), TemplateFactory.HTML.get("crud.fields"), true);

        // a bean that validates nothing still has somewhere to be told that
        // the database refused what was submitted
        var errors = builder.generateErrors(new Plain(), "The database didn't accept this.");
        assertTrue(errors.contains("crud-errors"));
        assertTrue(errors.contains("accept this"));

        assertEquals("", builder.generateErrors(new Plain(), null));
    }

    public static class Defaulted extends MetaData {
        private int id_ = -1;
        private boolean featured_ = false;
        private int amount_ = 0;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("featured").defaultValue(true));
            addConstraint(new ConstrainedProperty("amount").defaultValue(7));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setFeatured(boolean featured) { featured_ = featured; }
        public boolean isFeatured() { return featured_; }
        public void setAmount(int amount) { amount_ = amount; }
        public int getAmount() { return amount_; }
    }

    @Test
    void testRejectsTheTypesThatTheManagersCantUse() {
        // the query manager works with an int identifier and the ordinals
        // are int as well, so a wider or fractional one can't be accepted
        var e = assertThrows(InvalidPropertyTypeException.class, () -> entity(WideIdentifier.class));
        assertTrue(e.getMessage().contains("identifier"));
        assertTrue(e.getMessage().contains("long"));

        var ordinal = assertThrows(InvalidPropertyTypeException.class, () -> entity(TextScope.class));
        assertTrue(ordinal.getMessage().contains("ordinal restriction"));
        assertTrue(ordinal.getMessage().contains("String"));
    }

    @Test
    void testRejectsAnEntityThatCantBeAdded() {
        // the property has to be provided while no field is generated for
        // it, so every addition would fail on a field that isn't there
        var e = assertThrows(MandatoryPropertyWithoutFieldException.class, () -> entity(MandatoryUpload.class));
        assertTrue(e.getMessage().contains("avatar"));
    }

    public static class WideIdentifier extends MetaData {
        private long id_ = -1;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
        }

        public void setId(long id) { id_ = id; }
        public long getId() { return id_; }
    }

    public static class TextScope extends MetaData {
        private int id_ = -1;
        private String section_ = null;
        private int position_ = -1;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("section").notNull(true));
            addConstraint(new ConstrainedProperty("position").ordinal(true, "section"));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setSection(String section) { section_ = section; }
        public String getSection() { return section_; }
        public void setPosition(int position) { position_ = position; }
        public int getPosition() { return position_; }
    }

    public static class MandatoryUpload extends MetaData {
        private int id_ = -1;
        private byte[] avatar_ = null;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("avatar").file(true).notNull(true));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setAvatar(byte[] avatar) { avatar_ = avatar; }
        public byte[] getAvatar() { return avatar_; }
    }

    @Test
    void testUnsupportedPropertiesArentListedByDefault() {
        var entity = entity(Mapped.class);

        // the content of a file doesn't fit in a table cell, so a column
        // that was never asked for isn't generated for it
        assertFalse(entity.getListedProperties().contains("avatar"));
        assertTrue(entity.getListedProperties().contains("groupId"));
    }

    @Test
    void testRejectsAnOrdinalRestrictedToANullableProperty() {
        // a list that is named by null can't be told apart from the others, so
        // its ordinals couldn't be kept apart, renumbered or locked
        var e = assertThrows(NullableOrderingPropertyException.class, () -> entity(NullableScope.class));
        assertTrue(e.getMessage().contains("bookId"));
        assertTrue(e.getMessage().contains("notNull"));

        // a primitive can't be null, and a constrained one is guaranteed
        assertDoesNotThrow(() -> entity(Chapter.class));
        assertDoesNotThrow(() -> entity(ConstrainedScope.class));
    }

    public static class NullableScope extends MetaData {
        private int id_ = -1;
        private Integer bookId_ = null;
        private int position_ = -1;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("position").ordinal(true, "bookId"));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setBookId(Integer bookId) { bookId_ = bookId; }
        public Integer getBookId() { return bookId_; }
        public void setPosition(int position) { position_ = position; }
        public int getPosition() { return position_; }
    }

    public static class ConstrainedScope extends MetaData {
        private int id_ = -1;
        private Integer bookId_ = null;
        private int position_ = -1;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
            addConstraint(new ConstrainedProperty("bookId").notNull(true));
            addConstraint(new ConstrainedProperty("position").ordinal(true, "bookId"));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setBookId(Integer bookId) { bookId_ = bookId; }
        public Integer getBookId() { return bookId_; }
        public void setPosition(int position) { position_ = position; }
        public int getPosition() { return position_; }
    }

    @Test
    void testAValueThatCantBeRetrievedIsReported() {
        var entity = entity(Failing.class);

        // a value that can't be retrieved is reported instead of being
        // shown as empty, since an empty edit field would replace what is
        // actually stored
        var e = assertThrows(PropertyAccessException.class,
            () -> entity.formattedValue(new Failing(), "broken"));
        assertTrue(e.getMessage().contains("broken"));
        assertTrue(e.getMessage().contains(Failing.class.getName()));

        var builder = new CrudFormBuilder(entity, TemplateFactory.HTML.get("crud.fields"), false);
        assertThrows(PropertyAccessException.class, () -> builder.generateFields(new Failing()));
    }

    public static class Failing extends MetaData {
        private int id_ = -1;

        public void activateMetaData() {
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setBroken(String broken) { }
        public String getBroken() { throw new UnsupportedOperationException("broken getter"); }
    }

    public static class Priced extends MetaData {
        private int id_ = -1;
        private double amount_ = 0.0;

        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("id").identifier(true).editable(false));
        }

        public void setId(int id) { id_ = id; }
        public int getId() { return id_; }
        public void setAmount(double amount) { amount_ = amount; }
        public double getAmount() { return amount_; }
    }

    @Test
    void testRejectsADuplicateActionName() {
        var options = new CrudEntityOptions<CrudArticle>()
            .action("publish", "Publish", (c, article) -> null);

        var e = assertThrows(DuplicateActionNameException.class,
            () -> options.action("publish", "Publish again", (c, article) -> null));
        assertTrue(e.getMessage().contains("already used"));
    }

    @Test
    void testRejectsANameThatIsntUsableInAUrl() {
        var options = new CrudEntityOptions<CrudArticle>();

        assertThrows(InvalidActionNameException.class,
            () -> options.action("publish now", "Publish", (c, article) -> null));
        assertThrows(InvalidActionNameException.class,
            () -> options.action("../publish", "Publish", (c, article) -> null));
        assertThrows(InvalidSlugException.class, () -> options.slug("my articles"));
        assertThrows(InvalidSlugException.class, () -> options.slug(""));
    }

    @Test
    void testRejectsABlankLabel() {
        var options = new CrudEntityOptions<CrudArticle>();

        assertThrows(IllegalArgumentException.class, () -> options.label(" "));
        assertThrows(IllegalArgumentException.class, () -> options.labelPlural(" "));
    }

    @Test
    void testPluralLabelDefaultsToTheSingularOne() {
        assertEquals("CrudArticles", entity(CrudArticle.class).getLabelPlural());

        var entity = new CrudEntity<>(CrudArticle.class,
            new CrudEntityOptions<CrudArticle>().label("Entry").labelPlural("Entries"));
        assertEquals("Entry", entity.getLabel());
        assertEquals("Entries", entity.getLabelPlural());
    }

    @Test
    void testDerivesTheSlugFromTheClassName() {
        assertEquals("crudArticle", entity(CrudArticle.class).getSlug());

        var options = new CrudEntityOptions<CrudArticle>();
        options.slug("articles");
        assertEquals("articles", new CrudEntity<>(CrudArticle.class, options).getSlug());
    }

    @Test
    void testListedColumnsFollowTheirPositions() {
        // only the listed properties show up, in the order of their positions
        assertEquals(java.util.List.of("title", "author", "status", "featured"),
            entity(CrudArticle.class).getListedProperties());
    }

    @Test
    void testUnconstrainedBeanListsEveryProperty() {
        var listed = entity(Plain.class).getListedProperties();
        assertTrue(listed.contains("id"));
        assertTrue(listed.contains("name"));
    }

    @Test
    void testIdentifierIsDerivedFromTheConstraints() {
        assertEquals("id", entity(CrudArticle.class).getIdentifier());
        // an unconstrained bean falls back to the conventional name
        assertEquals("id", entity(Plain.class).getIdentifier());
    }

    @Test
    void testEditablePropertiesExcludeTheIdentifierAndTheOrdinal() {
        var editable = entity(CrudArticle.class).getEditableProperties();
        assertFalse(editable.contains("id"));
        assertFalse(editable.contains("ordinal"));
        assertEquals(java.util.List.of("title", "author", "status", "featured", "body"), editable);
    }

    @Test
    void testOrdinalIsDetected() {
        var article = entity(CrudArticle.class);
        assertTrue(article.isOrdered());
        assertEquals("ordinal", article.getOrdinal());
        assertNull(article.getOrdinalRestriction());

        var subscriber = entity(CrudSubscriber.class);
        assertFalse(subscriber.isOrdered());
        assertNull(subscriber.getOrdinal());
    }

    @Test
    void testRestrictedOrdinalCarriesItsRestriction() {
        var chapter = entity(Chapter.class);
        assertTrue(chapter.isOrdered());
        assertEquals("position", chapter.getOrdinal());
        assertEquals("bookId", chapter.getOrdinalRestriction());
    }

    @Test
    void testFormattedValuesAndIdentifiers() {
        var article = new CrudArticle();
        article.setId(42);
        article.setTitle("A title");
        article.setFeatured(true);

        var entity = entity(CrudArticle.class);
        assertEquals(42, entity.identifierValue(article));
        assertEquals("A title", entity.formattedValue(article, "title"));
        assertEquals("true", entity.formattedValue(article, "featured"));
        assertEquals("", entity.formattedValue(article, "author"));
    }

    @Test
    void testDuplicateSlugsAreRefused() {
        var admin = new CrudAdmin(TestDatasources.H2).entity(CrudArticle.class);
        var e = assertThrows(DuplicateSlugException.class,
            () -> admin.entity(CrudArticle.class));
        assertEquals("crudArticle", e.getSlug());
        assertEquals(CrudArticle.class, e.getRegisteredBeanClass());
    }

    @Test
    void testOptionDefaults() {
        var options = new CrudEntityOptions<CrudArticle>();
        assertEquals(20, options.getPageSize());
        assertNull(options.getRole());
        assertTrue(options.getActions().isEmpty());
        assertTrue(options.isDeletable(new CrudArticle()));

        assertThrows(IllegalArgumentException.class, () -> options.pageSize(0));
    }

    @Test
    void testActionVisibilityAndConfirmation() {
        var options = new CrudEntityOptions<CrudArticle>();
        options.action("publish", "Publish", (c, a) -> null,
            a -> a.visibleWhen(article -> !"published".equals(article.getStatus())).confirm());

        var action = options.getActions().get(0);
        assertEquals("publish", action.getName());
        assertEquals("Publish", action.getLabel());
        assertTrue(action.isConfirmed());

        var draft = new CrudArticle();
        draft.setStatus("draft");
        assertTrue(action.isVisible(draft));

        var published = new CrudArticle();
        published.setStatus("published");
        assertFalse(action.isVisible(published));
    }

    @Test
    void testFormBuilderRendersFieldsFromTheConstraints() {
        var builder = new CrudFormBuilder(entity(CrudArticle.class), TemplateFactory.HTML.get("crud.fields"), false);
        var article = new CrudArticle();
        article.setTitle("Hello & goodbye");
        var html = builder.generateFields(article);

        // the mandatory constraint becomes a required attribute
        assertTrue(html.contains("name=\"title\""));
        assertTrue(html.contains("required"));
        assertTrue(html.contains("maxlength=\"60\""));
        // values are encoded
        assertTrue(html.contains("Hello &amp; goodbye"));
        // the inList constraint becomes a select with its options
        assertTrue(html.contains("<select class=\"crud-select\" id=\"crud-status\" name=\"status\">"));
        assertTrue(html.contains("<option value=\"draft\""));
        // the boolean becomes a checkbox
        assertTrue(html.contains("type=\"checkbox\" class=\"crud-checkbox\" id=\"crud-featured\""));
        // the long text becomes a textarea
        assertTrue(html.contains("<textarea class=\"crud-textarea\" id=\"crud-body\""));
        // the identifier and the ordinal aren't part of the form
        assertFalse(html.contains("name=\"id\""));
        assertFalse(html.contains("name=\"ordinal\""));
    }

    @Test
    void testSelectOptionsDontOverwriteTheFieldLabel() {
        var builder = new CrudFormBuilder(entity(CrudArticle.class), TemplateFactory.HTML.get("crud.fields"), false);
        var article = new CrudArticle();
        article.setStatus("published");
        var html = builder.generateFields(article);

        // the label of the field stays its own, whatever the options are
        assertTrue(html.contains("for=\"crud-status\">Status"));
        assertTrue(html.contains("<option value=\"published\" selected>published</option>"));
        // and the fields that follow the select keep their labels too
        assertTrue(html.contains("for=\"crud-featured\">Featured"));
    }

    @Test
    void testFormBuilderMarksTheInvalidFields() {
        var subscriber = new CrudSubscriber();
        subscriber.setEmail("not an email");
        subscriber.validate();

        var builder = new CrudFormBuilder(entity(CrudSubscriber.class), TemplateFactory.HTML.get("crud.fields"), false);
        var html = builder.generateFields(subscriber);
        assertTrue(html.contains("crud-invalid"));
        assertTrue(html.contains("crud-fielderror"));

        var errors = builder.generateErrors(subscriber);
        assertTrue(errors.contains("crud-errors"));
        assertTrue(errors.contains("Email"));
    }

    @Test
    void testEmailConstraintBecomesAnEmailInput() {
        var builder = new CrudFormBuilder(entity(CrudSubscriber.class), TemplateFactory.HTML.get("crud.fields"), false);
        var html = builder.generateFields(new CrudSubscriber());
        assertTrue(html.contains("type=\"email\""));
    }
}
