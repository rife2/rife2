parser grammar TemplateHtmlParser;
@parser::header {
    package rife.template.antlr;
}

options { tokenVocab=TemplateHtmlLexer; }

document    :   content EOF ;

content     :   blockData*
                ((tagV|tagVDefault|tagB|tagBV|tagBA|tagI) blockData*)* ;

tagV        :   TSTART_V TS TTagName TS? TSTERM
            |   CSTART_V CS CTagName CS? CSTERM
            ;

tagVDefault :   TSTART_V TS TTagName TS? TENDI valueData? TCLOSE_V
            |   CSTART_V CS CTagName CS? CENDI valueData? CCLOSE_V
            ;

tagB        :   TSTART_B TS TTagName TS? TENDI content TCLOSE_B
            |   CSTART_B CS CTagName CS? CENDI content CCLOSE_B
            ;

tagBV       :   TSTART_BV TS TTagName TS? TENDI content TCLOSE_BV
            |   CSTART_BV CS CTagName CS? CENDI content CCLOSE_BV
            ;

tagBA       :   TSTART_BA TS TTagName TS? TENDI content TCLOSE_BA
            |   CSTART_BA CS CTagName CS? CENDI content CCLOSE_BA
            ;

tagI        :   TSTART_I TS TTagName TS? TSTERM
            |   CSTART_I CS CTagName TS? CSTERM
            ;

// Character data in the document not part of the tags
blockData   :   TEXT+ | WS ;
valueData   :   TEXT+ | WS ;