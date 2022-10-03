lexer grammar TemplateHtmlPreProcessLexer;
@lexer::header {
    package rife.template.antlr;
}

fragment I      :   'i' ;
fragment C      :   'c' ;
fragment TSTART :   '<!--' ;
fragment TTERM  :   '<!--/' ;
fragment TEND   :   '-->' ;
fragment CSTART :   '{{' ;
fragment CEND   :   '}}' ;
fragment CTERM  :   '{{/' ;
fragment DIGIT  :   [0-9] ;

fragment
NameChar    :   NameStartChar
            |   NameEndChar
            |   '-'
            ;

fragment
NameEndChar :   NameStartChar
            |   '_' | '.' | '[' | ']' | DIGIT
            |   '\u00B7'
            |   '\u0300'..'\u036F'
            |   '\u203F'..'\u2040'
            ;

fragment
NameStartChar
            :   [:a-zA-Z]
            |   '\u2070'..'\u218F'
            |   '\u2C00'..'\u2FEF'
            |   '\u3001'..'\uD7FF'
            |   '\uF900'..'\uFDCF'
            |   '\uFDF0'..'\uFFFD'
            ;

TSTART_I    :   TSTART I                    -> pushMode(TINSIDE_I) ;
CSTART_I    :   CSTART I                    -> pushMode(CINSIDE_I) ;

TCLOSE_C    :   TTERM C TEND ;
TSTART_C    :   TSTART C                    -> pushMode(TINSIDE_C) ;
CCLOSE_C    :   CTERM C CEND ;
CSTART_C    :   CSTART C                    -> pushMode(CINSIDE_C) ;

TEXT        :   ~[<{]+
            |   '<' ~'!'
            |   '<!' ~'-'
            |   '<!-' ~'-'
            |   '<!--' ~('i')
            |   '{' ~'{'
            |   '{{' ~('i')
            ;

mode TINSIDE_I;

TSTERM      :   '/-->'                      -> popMode ;
TS          :   [ \t\r\n]+ ;
TTagName    :   NameStartChar | NameStartChar NameChar* NameEndChar ;

mode CINSIDE_I;

CSTERM      :   '/}}'                       -> popMode ;
CS          :   [ \t\r\n]+ ;
CTagName    :   NameStartChar | NameStartChar NameChar* NameEndChar ;

mode TINSIDE_C;

TENDI       :   TEND                        -> popMode ;
TComment    :   ~[-]*
            |   '-' ~'-'
            |   '--' ~'>'
            ;

mode CINSIDE_C;

CENDI       :   CEND                        -> popMode ;
CComment    :   ~[}]*
            |   '}' ~'-'
            ;
