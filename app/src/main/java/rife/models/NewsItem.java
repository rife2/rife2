package rife.models;

import rife.cmf.MimeType;
import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

public class NewsItem extends MetaData {
    private Integer id_;
    private String title_;
    private String text_;
    private byte[] newsImage_;

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("title")
            .notNull(true)
            .maxLength(90));

        // specify a type for automatic validation
        addConstraint(new ConstrainedProperty("text")
            .mimeType(MimeType.APPLICATION_XHTML)
            .autoRetrieved(true)
            .fragment(true)
            .notNull(true)
            .notEmpty(true));
        addConstraint(new ConstrainedProperty("newsImage")
            .persistent(false)
            .file(true));

        // specify types for content delivery and sizes for automatic rescaling
        addConstraint(new ConstrainedProperty("imageSmall")
            .mimeType(MimeType.IMAGE_JPEG)
            .contentAttribute("width", 100)
            .editable(false));
        addConstraint(new ConstrainedProperty("imageMedium")
            .mimeType(MimeType.IMAGE_JPEG)
            .contentAttribute("width", 480)
            .editable(false));

        addConstraint(new ConstrainedProperty("id")
            .editable(false)
            .saved(false)
            .identifier(true));
    }

    public void    setId(Integer id)                 { id_ = id; }
    public Integer getId()                           { return id_; }
    public void    setTitle(String title)            { title_ = title; }
    public String  getTitle()                        { return title_; }
    public void    setText(String text)              { text_ = text; }
    public String  getText()                         { return text_; }

    // A setter/getter pair defines a property.
    // In this case, imageSmall and imageMedium are both "virtual"
    // properties, which reference the newsImage property, but
    // are delivered after transformations, i.e. they are resized
    // to the dimensions specified in the Constraints.
    public void   setNewsImage(byte[] newsImage)     { newsImage_ = newsImage; }
    public byte[] getNewsImage()                     { return newsImage_; }
    public void   setImageSmall(byte[] imageSmall)   { }  // dummy setter
    public byte[] getImageSmall()                    { return newsImage_; }
    public void   setImageMedium(byte[] imageMedium) { }  // dummy setter
    public byte[] getImageMedium()                   { return newsImage_; }

}
