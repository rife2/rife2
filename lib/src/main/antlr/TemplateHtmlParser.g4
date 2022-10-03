parser grammar TemplateHtmlParser;
@parser::header {
    package rife.template.antlr;
}

options { tokenVocab=TemplateHtmlLexer; }

document        :   blockContent EOF ;

blockContent    :   blockData* ((tagV|tagVDefault|tagB|tagBV|tagBA|tagI) blockData*)* ;
valueContent    :   valueData* ((tagB|tagBV|tagBA|tagI) valueData*)* ;

tagV        :   TSTART_V TS TTagName TS? TSTERM
            |   CSTART_V CS CTagName CS? CSTERM
            ;

tagVDefault :   TSTART_V TS TTagName TS? TENDI valueContent TCLOSE_V
            |   CSTART_V CS CTagName CS? CENDI valueContent CCLOSE_V
            ;

tagB        :   TSTART_B TS TTagName TS? TENDI blockContent TCLOSE_B
            |   CSTART_B CS CTagName CS? CENDI blockContent CCLOSE_B
            ;

tagBV       :   TSTART_BV TS TTagName TS? TENDI blockContent TCLOSE_BV
            |   CSTART_BV CS CTagName CS? CENDI blockContent CCLOSE_BV
            ;

tagBA       :   TSTART_BA TS TTagName TS? TENDI blockContent TCLOSE_BA
            |   CSTART_BA CS CTagName CS? CENDI blockContent CCLOSE_BA
            ;

tagI        :   TSTART_I TS TTagName TS? TSTERM
            |   CSTART_I CS CTagName TS? CSTERM
            ;

// Character data in the document not part of the tags
blockData   :   TEXT+ | WS ;
valueData   :   TEXT+ | WS ;