parser grammar TemplateHtmlIncludeParser;
@parser::header {
    package rife.template.antlr;
}

options { tokenVocab=TemplateHtmlIncludeLexer; }

document    :   (docData|tagI)* ;

docData     :   TEXT+;

tagI        :   TSTART_I TS TTagName TS? TSTERM
            |   CSTART_I CS CTagName TS? CSTERM
            ;

