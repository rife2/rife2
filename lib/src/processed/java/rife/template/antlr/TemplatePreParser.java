// Generated from java-escape by ANTLR 4.11.1

    package rife.template.antlr;

import rife.antlr.v4.runtime.atn.*;
import rife.antlr.v4.runtime.dfa.DFA;
import rife.antlr.v4.runtime.*;
import rife.antlr.v4.runtime.misc.*;
import rife.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class TemplatePreParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.11.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		TSTART_I=1, CSTART_I=2, TCLOSE_C=3, TSTART_C=4, CCLOSE_C=5, CSTART_C=6, 
		TEXT=7, TSTERM=8, TS=9, TTagName=10, CSTERM=11, CS=12, CTagName=13, TENDI=14, 
		TComment=15, CENDI=16, CComment=17;
	public static final int
		RULE_document = 0, RULE_tagI = 1, RULE_tagC = 2, RULE_docData = 3, RULE_commentData = 4;
	private static String[] makeRuleNames() {
		return new String[] {
			"document", "tagI", "tagC", "docData", "commentData"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "TSTART_I", "CSTART_I", "TCLOSE_C", "TSTART_C", "CCLOSE_C", "CSTART_C", 
			"TEXT", "TSTERM", "TS", "TTagName", "CSTERM", "CS", "CTagName", "TENDI", 
			"TComment", "CENDI", "CComment"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "java-escape"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TemplatePreParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DocumentContext extends ParserRuleContext {
		public List<DocDataContext> docData() {
			return getRuleContexts(DocDataContext.class);
		}
		public DocDataContext docData(int i) {
			return getRuleContext(DocDataContext.class,i);
		}
		public List<TagIContext> tagI() {
			return getRuleContexts(TagIContext.class);
		}
		public TagIContext tagI(int i) {
			return getRuleContext(TagIContext.class,i);
		}
		public List<TagCContext> tagC() {
			return getRuleContexts(TagCContext.class);
		}
		public TagCContext tagC(int i) {
			return getRuleContext(TagCContext.class,i);
		}
		public DocumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_document; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplatePreParserListener ) ((TemplatePreParserListener)listener).enterDocument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplatePreParserListener ) ((TemplatePreParserListener)listener).exitDocument(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplatePreParserVisitor ) return ((TemplatePreParserVisitor<? extends T>)visitor).visitDocument(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DocumentContext document() throws RecognitionException {
		DocumentContext _localctx = new DocumentContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_document);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(15);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 214L) != 0) {
				{
				setState(13);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case TEXT:
					{
					setState(10);
					docData();
					}
					break;
				case TSTART_I:
				case CSTART_I:
					{
					setState(11);
					tagI();
					}
					break;
				case TSTART_C:
				case CSTART_C:
					{
					setState(12);
					tagC();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(17);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TagIContext extends ParserRuleContext {
		public TerminalNode TSTART_I() { return getToken(TemplatePreParser.TSTART_I, 0); }
		public List<TerminalNode> TS() { return getTokens(TemplatePreParser.TS); }
		public TerminalNode TS(int i) {
			return getToken(TemplatePreParser.TS, i);
		}
		public TerminalNode TTagName() { return getToken(TemplatePreParser.TTagName, 0); }
		public TerminalNode TSTERM() { return getToken(TemplatePreParser.TSTERM, 0); }
		public TerminalNode CSTART_I() { return getToken(TemplatePreParser.CSTART_I, 0); }
		public TerminalNode CS() { return getToken(TemplatePreParser.CS, 0); }
		public TerminalNode CTagName() { return getToken(TemplatePreParser.CTagName, 0); }
		public TerminalNode CSTERM() { return getToken(TemplatePreParser.CSTERM, 0); }
		public TagIContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tagI; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplatePreParserListener ) ((TemplatePreParserListener)listener).enterTagI(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplatePreParserListener ) ((TemplatePreParserListener)listener).exitTagI(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplatePreParserVisitor ) return ((TemplatePreParserVisitor<? extends T>)visitor).visitTagI(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TagIContext tagI() throws RecognitionException {
		TagIContext _localctx = new TagIContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_tagI);
		int _la;
		try {
			setState(32);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TSTART_I:
				enterOuterAlt(_localctx, 1);
				{
				setState(18);
				match(TSTART_I);
				setState(19);
				match(TS);
				setState(20);
				match(TTagName);
				setState(22);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TS) {
					{
					setState(21);
					match(TS);
					}
				}

				setState(24);
				match(TSTERM);
				}
				break;
			case CSTART_I:
				enterOuterAlt(_localctx, 2);
				{
				setState(25);
				match(CSTART_I);
				setState(26);
				match(CS);
				setState(27);
				match(CTagName);
				setState(29);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TS) {
					{
					setState(28);
					match(TS);
					}
				}

				setState(31);
				match(CSTERM);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TagCContext extends ParserRuleContext {
		public TerminalNode TSTART_C() { return getToken(TemplatePreParser.TSTART_C, 0); }
		public TerminalNode TENDI() { return getToken(TemplatePreParser.TENDI, 0); }
		public TerminalNode TCLOSE_C() { return getToken(TemplatePreParser.TCLOSE_C, 0); }
		public TerminalNode TComment() { return getToken(TemplatePreParser.TComment, 0); }
		public List<CommentDataContext> commentData() {
			return getRuleContexts(CommentDataContext.class);
		}
		public CommentDataContext commentData(int i) {
			return getRuleContext(CommentDataContext.class,i);
		}
		public TerminalNode CSTART_C() { return getToken(TemplatePreParser.CSTART_C, 0); }
		public TerminalNode CENDI() { return getToken(TemplatePreParser.CENDI, 0); }
		public TerminalNode CCLOSE_C() { return getToken(TemplatePreParser.CCLOSE_C, 0); }
		public TerminalNode CComment() { return getToken(TemplatePreParser.CComment, 0); }
		public TagCContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tagC; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplatePreParserListener ) ((TemplatePreParserListener)listener).enterTagC(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplatePreParserListener ) ((TemplatePreParserListener)listener).exitTagC(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplatePreParserVisitor ) return ((TemplatePreParserVisitor<? extends T>)visitor).visitTagC(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TagCContext tagC() throws RecognitionException {
		TagCContext _localctx = new TagCContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_tagC);
		int _la;
		try {
			setState(58);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TSTART_C:
				enterOuterAlt(_localctx, 1);
				{
				setState(34);
				match(TSTART_C);
				setState(36);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TComment) {
					{
					setState(35);
					match(TComment);
					}
				}

				setState(38);
				match(TENDI);
				setState(42);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==TEXT) {
					{
					{
					setState(39);
					commentData();
					}
					}
					setState(44);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(45);
				match(TCLOSE_C);
				}
				break;
			case CSTART_C:
				enterOuterAlt(_localctx, 2);
				{
				setState(46);
				match(CSTART_C);
				setState(48);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CComment) {
					{
					setState(47);
					match(CComment);
					}
				}

				setState(50);
				match(CENDI);
				setState(54);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==TEXT) {
					{
					{
					setState(51);
					commentData();
					}
					}
					setState(56);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(57);
				match(CCLOSE_C);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DocDataContext extends ParserRuleContext {
		public List<TerminalNode> TEXT() { return getTokens(TemplatePreParser.TEXT); }
		public TerminalNode TEXT(int i) {
			return getToken(TemplatePreParser.TEXT, i);
		}
		public DocDataContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_docData; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplatePreParserListener ) ((TemplatePreParserListener)listener).enterDocData(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplatePreParserListener ) ((TemplatePreParserListener)listener).exitDocData(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplatePreParserVisitor ) return ((TemplatePreParserVisitor<? extends T>)visitor).visitDocData(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DocDataContext docData() throws RecognitionException {
		DocDataContext _localctx = new DocDataContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_docData);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(61); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(60);
					match(TEXT);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(63); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			} while ( _alt!=2 && _alt!=rife.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CommentDataContext extends ParserRuleContext {
		public List<TerminalNode> TEXT() { return getTokens(TemplatePreParser.TEXT); }
		public TerminalNode TEXT(int i) {
			return getToken(TemplatePreParser.TEXT, i);
		}
		public CommentDataContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commentData; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplatePreParserListener ) ((TemplatePreParserListener)listener).enterCommentData(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplatePreParserListener ) ((TemplatePreParserListener)listener).exitCommentData(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplatePreParserVisitor ) return ((TemplatePreParserVisitor<? extends T>)visitor).visitCommentData(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommentDataContext commentData() throws RecognitionException {
		CommentDataContext _localctx = new CommentDataContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_commentData);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(66); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(65);
					match(TEXT);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(68); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			} while ( _alt!=2 && _alt!=rife.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u0011G\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0005\u0000\u000e\b\u0000\n\u0000\f\u0000"+
		"\u0011\t\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001"+
		"\u0017\b\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0003\u0001\u001e\b\u0001\u0001\u0001\u0003\u0001!\b\u0001\u0001\u0002"+
		"\u0001\u0002\u0003\u0002%\b\u0002\u0001\u0002\u0001\u0002\u0005\u0002"+
		")\b\u0002\n\u0002\f\u0002,\t\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0003\u00021\b\u0002\u0001\u0002\u0001\u0002\u0005\u00025\b\u0002\n\u0002"+
		"\f\u00028\t\u0002\u0001\u0002\u0003\u0002;\b\u0002\u0001\u0003\u0004\u0003"+
		">\b\u0003\u000b\u0003\f\u0003?\u0001\u0004\u0004\u0004C\b\u0004\u000b"+
		"\u0004\f\u0004D\u0001\u0004\u0000\u0000\u0005\u0000\u0002\u0004\u0006"+
		"\b\u0000\u0000N\u0000\u000f\u0001\u0000\u0000\u0000\u0002 \u0001\u0000"+
		"\u0000\u0000\u0004:\u0001\u0000\u0000\u0000\u0006=\u0001\u0000\u0000\u0000"+
		"\bB\u0001\u0000\u0000\u0000\n\u000e\u0003\u0006\u0003\u0000\u000b\u000e"+
		"\u0003\u0002\u0001\u0000\f\u000e\u0003\u0004\u0002\u0000\r\n\u0001\u0000"+
		"\u0000\u0000\r\u000b\u0001\u0000\u0000\u0000\r\f\u0001\u0000\u0000\u0000"+
		"\u000e\u0011\u0001\u0000\u0000\u0000\u000f\r\u0001\u0000\u0000\u0000\u000f"+
		"\u0010\u0001\u0000\u0000\u0000\u0010\u0001\u0001\u0000\u0000\u0000\u0011"+
		"\u000f\u0001\u0000\u0000\u0000\u0012\u0013\u0005\u0001\u0000\u0000\u0013"+
		"\u0014\u0005\t\u0000\u0000\u0014\u0016\u0005\n\u0000\u0000\u0015\u0017"+
		"\u0005\t\u0000\u0000\u0016\u0015\u0001\u0000\u0000\u0000\u0016\u0017\u0001"+
		"\u0000\u0000\u0000\u0017\u0018\u0001\u0000\u0000\u0000\u0018!\u0005\b"+
		"\u0000\u0000\u0019\u001a\u0005\u0002\u0000\u0000\u001a\u001b\u0005\f\u0000"+
		"\u0000\u001b\u001d\u0005\r\u0000\u0000\u001c\u001e\u0005\t\u0000\u0000"+
		"\u001d\u001c\u0001\u0000\u0000\u0000\u001d\u001e\u0001\u0000\u0000\u0000"+
		"\u001e\u001f\u0001\u0000\u0000\u0000\u001f!\u0005\u000b\u0000\u0000 \u0012"+
		"\u0001\u0000\u0000\u0000 \u0019\u0001\u0000\u0000\u0000!\u0003\u0001\u0000"+
		"\u0000\u0000\"$\u0005\u0004\u0000\u0000#%\u0005\u000f\u0000\u0000$#\u0001"+
		"\u0000\u0000\u0000$%\u0001\u0000\u0000\u0000%&\u0001\u0000\u0000\u0000"+
		"&*\u0005\u000e\u0000\u0000\')\u0003\b\u0004\u0000(\'\u0001\u0000\u0000"+
		"\u0000),\u0001\u0000\u0000\u0000*(\u0001\u0000\u0000\u0000*+\u0001\u0000"+
		"\u0000\u0000+-\u0001\u0000\u0000\u0000,*\u0001\u0000\u0000\u0000-;\u0005"+
		"\u0003\u0000\u0000.0\u0005\u0006\u0000\u0000/1\u0005\u0011\u0000\u0000"+
		"0/\u0001\u0000\u0000\u000001\u0001\u0000\u0000\u000012\u0001\u0000\u0000"+
		"\u000026\u0005\u0010\u0000\u000035\u0003\b\u0004\u000043\u0001\u0000\u0000"+
		"\u000058\u0001\u0000\u0000\u000064\u0001\u0000\u0000\u000067\u0001\u0000"+
		"\u0000\u000079\u0001\u0000\u0000\u000086\u0001\u0000\u0000\u00009;\u0005"+
		"\u0005\u0000\u0000:\"\u0001\u0000\u0000\u0000:.\u0001\u0000\u0000\u0000"+
		";\u0005\u0001\u0000\u0000\u0000<>\u0005\u0007\u0000\u0000=<\u0001\u0000"+
		"\u0000\u0000>?\u0001\u0000\u0000\u0000?=\u0001\u0000\u0000\u0000?@\u0001"+
		"\u0000\u0000\u0000@\u0007\u0001\u0000\u0000\u0000AC\u0005\u0007\u0000"+
		"\u0000BA\u0001\u0000\u0000\u0000CD\u0001\u0000\u0000\u0000DB\u0001\u0000"+
		"\u0000\u0000DE\u0001\u0000\u0000\u0000E\t\u0001\u0000\u0000\u0000\f\r"+
		"\u000f\u0016\u001d $*06:?D";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}