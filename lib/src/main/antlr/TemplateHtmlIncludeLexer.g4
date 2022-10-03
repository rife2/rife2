lexer grammar TemplateHtmlIncludeLexer;
@lexer::header {
    package rife.template.antlr;
}
// -------------------------------------------------------------------
// MODE: Everything OUTSIDE of a tag

fragment I      :   'i' ;
fragment TSTART :   '<!--' ;
fragment CSTART :   '{{' ;
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

//WS          :   (' '|'\t'|'\r'? '\n')+ ;

TSTART_I    :   TSTART I                    -> pushMode(TINSIDE) ;
CSTART_I    :   CSTART I                    -> pushMode(CINSIDE) ;

TEXT        :   ~[<{]+
            |   '<' ~'!'
            |   '<!' ~'-'
            |   '<!-' ~'-'
            |   '<!--' ~('i')
            |   '{' ~'{'
            |   '{{' ~('i')
            ;

// -------------------------------------------------------------------
// MODE: Everything INSIDE of a regular tag

mode TINSIDE;

TSTERM      :   '/-->'                      -> popMode ;
TS          :   [ \t\r\n]+ ;
TTagName    :   NameStartChar | NameStartChar NameChar* NameEndChar ;

// -------------------------------------------------------------------
// MODE: Everything INSIDE of a compact tag

mode CINSIDE;

CSTERM      :   '/}}'                       -> popMode ;
CS          :   [ \t\r\n]+ ;
CTagName    :   NameStartChar | NameStartChar NameChar* NameEndChar ;
