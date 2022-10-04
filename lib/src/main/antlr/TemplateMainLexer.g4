lexer grammar TemplateMainLexer;
@lexer::header {
    package rife.template.antlr;

    import rife.template.TemplateConfig;
    import static rife.template.TemplateConfig.*;

}
@lexer::members {
    public TemplateConfig tc = TemplateConfig.XML;
}

// -------------------------------------------------------------------
// MODE: Everything OUTSIDE of a tag

fragment TSTART :   '<!--'  { tc == XML }? | '<!'  { tc == TXT }? | '/*'  { tc == SQL }? ;
fragment TEND   :   '-->'   { tc == XML }? | '>'   { tc == TXT }? | '*/'  { tc == SQL }? ;
fragment TTERM  :   '<!--/' { tc == XML }? | '<!/' { tc == TXT }? | '/*-' { tc == SQL }? ;
fragment STTERM :   '/-->'  { tc == XML }? | '/>'  { tc == TXT }? | '-*/' { tc == SQL }? ;
fragment FTEXT  :   ~[<{]+ { tc == XML }?
                |   ~[<{]+ { tc == TXT }?
                |   ~[/{]+ { tc == SQL }?
                ;
fragment TTEXT  :   ( ('<' ~'!' | '<!' ~'-' | '<!-' ~'-') { tc == XML }? |
                      '<' ~'!' { tc == TXT }? |
                      '/' ~'*' { tc == SQL }? )
                |   ( '<!--' ~('v'|'b'|'/') { tc == XML }? |
                      '<!'   ~('v'|'b'|'/') { tc == TXT }? |
                      '/*'   ~('v'|'b'|'-') { tc == SQL }? )
                |   ( '<!--/' ~('v'|'b') { tc == XML }? |
                      '<!/'   ~('v'|'b') { tc == TXT }? |
                      '/*-'   ~('v'|'b') { tc == SQL }? )
                ;
fragment V      :   'v' ;
fragment B      :   'b' ;
fragment BV     :   'bv' ;
fragment BA     :   'ba' ;

fragment CSTART :   '{{' ;
fragment CEND   :   '}}' ;
fragment CTERM  :   '{{/' ;
fragment CTTERM :   '/}}' ;
fragment CTEXT  :   '{' ~'{'
                |   '{{' ~('v'|'b'|'/')
                |   '{{/' ~('v'|'b')
                ;

fragment DIGIT  :   [0-9] ;

fragment
NameChar    :   NameStartChar
            |   NameEndChar
            |   '-' | '/'
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

TCLOSE_V    :   TTERM V TEND ;
TSTART_V    :   TSTART V                    -> pushMode(TINSIDE) ;
CCLOSE_V    :   CTERM V CEND ;
CSTART_V    :   CSTART V                    -> pushMode(CINSIDE) ;

TCLOSE_B    :   TTERM B TEND ;
TSTART_B    :   TSTART B                    -> pushMode(TINSIDE) ;
CCLOSE_B    :   CTERM B CEND ;
CSTART_B    :   CSTART B                    -> pushMode(CINSIDE) ;

TCLOSE_BV   :   TTERM BV TEND ;
TSTART_BV   :   TSTART BV                   -> pushMode(TINSIDE) ;
CCLOSE_BV   :   CTERM BV CEND ;
CSTART_BV   :   CSTART BV                   -> pushMode(CINSIDE) ;

TCLOSE_BA   :   TTERM BA TEND ;
TSTART_BA   :   TSTART BA                   -> pushMode(TINSIDE) ;
CCLOSE_BA   :   CTERM BA CEND ;
CSTART_BA   :   CSTART BA                   -> pushMode(CINSIDE) ;

TEXT        :   FTEXT
            |   TTEXT
            |   CTEXT
            ;

// -------------------------------------------------------------------
// MODE: Everything INSIDE of a regular tag

mode TINSIDE;

TENDI       :   TEND                        -> popMode ;
TSTERM      :   STTERM                      -> popMode ;
TS          :   [ \t\r\n]+ ;
TTagName    :   NameStartChar | NameStartChar NameChar* NameEndChar ;

// -------------------------------------------------------------------
// MODE: Everything INSIDE of a compact tag

mode CINSIDE;

CENDI       :   CEND                        -> popMode ;
CSTERM      :   CTTERM                      -> popMode ;
CS          :   [ \t\r\n]+ ;
CTagName    :   NameStartChar | NameStartChar NameChar* NameEndChar ;
