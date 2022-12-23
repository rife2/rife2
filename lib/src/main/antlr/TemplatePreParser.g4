parser grammar TemplatePreParser;
@parser::header {
    package rife.template.antlr;
}

options { tokenVocab=TemplatePreLexer; }

document    :   (docData|tagI|tagC)* ;

tagI        :   TSTART_I TS TTagName TS? TSTERM
            |   CSTART_I CS CTagName TS? CSTERM
            ;

tagC        :   TSTART_C TComment? TENDI commentData* TCLOSE_C
            |   CSTART_C CComment? CENDI commentData* CCLOSE_C
            ;

// Character data in the document not part of the tags
docData     :   TEXT+;
commentData :   TEXT+;
