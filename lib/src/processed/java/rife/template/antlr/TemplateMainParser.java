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
public class TemplateMainParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.11.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		TCLOSE_V=1, TSTART_V=2, CCLOSE_V=3, CSTART_V=4, TCLOSE_B=5, TSTART_B=6, 
		CCLOSE_B=7, CSTART_B=8, TCLOSE_BV=9, TSTART_BV=10, CCLOSE_BV=11, CSTART_BV=12, 
		TCLOSE_BA=13, TSTART_BA=14, CCLOSE_BA=15, CSTART_BA=16, TEXT=17, TENDI=18, 
		TSTERM=19, TS=20, TTagName=21, CENDI=22, CSTERM=23, CS=24, CTagName=25;
	public static final int
		RULE_document = 0, RULE_blockContent = 1, RULE_valueContent = 2, RULE_tagV = 3, 
		RULE_tagVDefault = 4, RULE_tagB = 5, RULE_tagBV = 6, RULE_tagBA = 7, RULE_blockData = 8, 
		RULE_valueData = 9;
	private static String[] makeRuleNames() {
		return new String[] {
			"document", "blockContent", "valueContent", "tagV", "tagVDefault", "tagB", 
			"tagBV", "tagBA", "blockData", "valueData"
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
			null, "TCLOSE_V", "TSTART_V", "CCLOSE_V", "CSTART_V", "TCLOSE_B", "TSTART_B", 
			"CCLOSE_B", "CSTART_B", "TCLOSE_BV", "TSTART_BV", "CCLOSE_BV", "CSTART_BV", 
			"TCLOSE_BA", "TSTART_BA", "CCLOSE_BA", "CSTART_BA", "TEXT", "TENDI", 
			"TSTERM", "TS", "TTagName", "CENDI", "CSTERM", "CS", "CTagName"
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

	public TemplateMainParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DocumentContext extends ParserRuleContext {
		public BlockContentContext blockContent() {
			return getRuleContext(BlockContentContext.class,0);
		}
		public TerminalNode EOF() { return getToken(TemplateMainParser.EOF, 0); }
		public DocumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_document; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).enterDocument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).exitDocument(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateMainParserVisitor ) return ((TemplateMainParserVisitor<? extends T>)visitor).visitDocument(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DocumentContext document() throws RecognitionException {
		DocumentContext _localctx = new DocumentContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_document);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(20);
			blockContent();
			setState(21);
			match(EOF);
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
	public static class BlockContentContext extends ParserRuleContext {
		public List<BlockDataContext> blockData() {
			return getRuleContexts(BlockDataContext.class);
		}
		public BlockDataContext blockData(int i) {
			return getRuleContext(BlockDataContext.class,i);
		}
		public List<TagVContext> tagV() {
			return getRuleContexts(TagVContext.class);
		}
		public TagVContext tagV(int i) {
			return getRuleContext(TagVContext.class,i);
		}
		public List<TagVDefaultContext> tagVDefault() {
			return getRuleContexts(TagVDefaultContext.class);
		}
		public TagVDefaultContext tagVDefault(int i) {
			return getRuleContext(TagVDefaultContext.class,i);
		}
		public List<TagBContext> tagB() {
			return getRuleContexts(TagBContext.class);
		}
		public TagBContext tagB(int i) {
			return getRuleContext(TagBContext.class,i);
		}
		public List<TagBVContext> tagBV() {
			return getRuleContexts(TagBVContext.class);
		}
		public TagBVContext tagBV(int i) {
			return getRuleContext(TagBVContext.class,i);
		}
		public List<TagBAContext> tagBA() {
			return getRuleContexts(TagBAContext.class);
		}
		public TagBAContext tagBA(int i) {
			return getRuleContext(TagBAContext.class,i);
		}
		public BlockContentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blockContent; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).enterBlockContent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).exitBlockContent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateMainParserVisitor ) return ((TemplateMainParserVisitor<? extends T>)visitor).visitBlockContent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockContentContext blockContent() throws RecognitionException {
		BlockContentContext _localctx = new BlockContentContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_blockContent);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(31);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 218452L) != 0) {
				{
				setState(29);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(23);
					blockData();
					}
					break;
				case 2:
					{
					setState(24);
					tagV();
					}
					break;
				case 3:
					{
					setState(25);
					tagVDefault();
					}
					break;
				case 4:
					{
					setState(26);
					tagB();
					}
					break;
				case 5:
					{
					setState(27);
					tagBV();
					}
					break;
				case 6:
					{
					setState(28);
					tagBA();
					}
					break;
				}
				}
				setState(33);
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
	public static class ValueContentContext extends ParserRuleContext {
		public List<ValueDataContext> valueData() {
			return getRuleContexts(ValueDataContext.class);
		}
		public ValueDataContext valueData(int i) {
			return getRuleContext(ValueDataContext.class,i);
		}
		public List<TagBContext> tagB() {
			return getRuleContexts(TagBContext.class);
		}
		public TagBContext tagB(int i) {
			return getRuleContext(TagBContext.class,i);
		}
		public List<TagBVContext> tagBV() {
			return getRuleContexts(TagBVContext.class);
		}
		public TagBVContext tagBV(int i) {
			return getRuleContext(TagBVContext.class,i);
		}
		public List<TagBAContext> tagBA() {
			return getRuleContexts(TagBAContext.class);
		}
		public TagBAContext tagBA(int i) {
			return getRuleContext(TagBAContext.class,i);
		}
		public ValueContentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valueContent; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).enterValueContent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).exitValueContent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateMainParserVisitor ) return ((TemplateMainParserVisitor<? extends T>)visitor).visitValueContent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueContentContext valueContent() throws RecognitionException {
		ValueContentContext _localctx = new ValueContentContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_valueContent);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(40);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 218432L) != 0) {
				{
				setState(38);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case TEXT:
					{
					setState(34);
					valueData();
					}
					break;
				case TSTART_B:
				case CSTART_B:
					{
					setState(35);
					tagB();
					}
					break;
				case TSTART_BV:
				case CSTART_BV:
					{
					setState(36);
					tagBV();
					}
					break;
				case TSTART_BA:
				case CSTART_BA:
					{
					setState(37);
					tagBA();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(42);
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
	public static class TagVContext extends ParserRuleContext {
		public TerminalNode TSTART_V() { return getToken(TemplateMainParser.TSTART_V, 0); }
		public List<TerminalNode> TS() { return getTokens(TemplateMainParser.TS); }
		public TerminalNode TS(int i) {
			return getToken(TemplateMainParser.TS, i);
		}
		public TerminalNode TTagName() { return getToken(TemplateMainParser.TTagName, 0); }
		public TerminalNode TSTERM() { return getToken(TemplateMainParser.TSTERM, 0); }
		public TerminalNode CSTART_V() { return getToken(TemplateMainParser.CSTART_V, 0); }
		public List<TerminalNode> CS() { return getTokens(TemplateMainParser.CS); }
		public TerminalNode CS(int i) {
			return getToken(TemplateMainParser.CS, i);
		}
		public TerminalNode CTagName() { return getToken(TemplateMainParser.CTagName, 0); }
		public TerminalNode CSTERM() { return getToken(TemplateMainParser.CSTERM, 0); }
		public TagVContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tagV; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).enterTagV(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).exitTagV(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateMainParserVisitor ) return ((TemplateMainParserVisitor<? extends T>)visitor).visitTagV(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TagVContext tagV() throws RecognitionException {
		TagVContext _localctx = new TagVContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_tagV);
		int _la;
		try {
			setState(57);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TSTART_V:
				enterOuterAlt(_localctx, 1);
				{
				setState(43);
				match(TSTART_V);
				setState(44);
				match(TS);
				setState(45);
				match(TTagName);
				setState(47);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TS) {
					{
					setState(46);
					match(TS);
					}
				}

				setState(49);
				match(TSTERM);
				}
				break;
			case CSTART_V:
				enterOuterAlt(_localctx, 2);
				{
				setState(50);
				match(CSTART_V);
				setState(51);
				match(CS);
				setState(52);
				match(CTagName);
				setState(54);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CS) {
					{
					setState(53);
					match(CS);
					}
				}

				setState(56);
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
	public static class TagVDefaultContext extends ParserRuleContext {
		public TerminalNode TSTART_V() { return getToken(TemplateMainParser.TSTART_V, 0); }
		public List<TerminalNode> TS() { return getTokens(TemplateMainParser.TS); }
		public TerminalNode TS(int i) {
			return getToken(TemplateMainParser.TS, i);
		}
		public TerminalNode TTagName() { return getToken(TemplateMainParser.TTagName, 0); }
		public TerminalNode TENDI() { return getToken(TemplateMainParser.TENDI, 0); }
		public ValueContentContext valueContent() {
			return getRuleContext(ValueContentContext.class,0);
		}
		public TerminalNode TCLOSE_V() { return getToken(TemplateMainParser.TCLOSE_V, 0); }
		public TerminalNode CSTART_V() { return getToken(TemplateMainParser.CSTART_V, 0); }
		public List<TerminalNode> CS() { return getTokens(TemplateMainParser.CS); }
		public TerminalNode CS(int i) {
			return getToken(TemplateMainParser.CS, i);
		}
		public TerminalNode CTagName() { return getToken(TemplateMainParser.CTagName, 0); }
		public TerminalNode CENDI() { return getToken(TemplateMainParser.CENDI, 0); }
		public TerminalNode CCLOSE_V() { return getToken(TemplateMainParser.CCLOSE_V, 0); }
		public TagVDefaultContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tagVDefault; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).enterTagVDefault(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).exitTagVDefault(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateMainParserVisitor ) return ((TemplateMainParserVisitor<? extends T>)visitor).visitTagVDefault(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TagVDefaultContext tagVDefault() throws RecognitionException {
		TagVDefaultContext _localctx = new TagVDefaultContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_tagVDefault);
		int _la;
		try {
			setState(79);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TSTART_V:
				enterOuterAlt(_localctx, 1);
				{
				setState(59);
				match(TSTART_V);
				setState(60);
				match(TS);
				setState(61);
				match(TTagName);
				setState(63);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TS) {
					{
					setState(62);
					match(TS);
					}
				}

				setState(65);
				match(TENDI);
				setState(66);
				valueContent();
				setState(67);
				match(TCLOSE_V);
				}
				break;
			case CSTART_V:
				enterOuterAlt(_localctx, 2);
				{
				setState(69);
				match(CSTART_V);
				setState(70);
				match(CS);
				setState(71);
				match(CTagName);
				setState(73);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CS) {
					{
					setState(72);
					match(CS);
					}
				}

				setState(75);
				match(CENDI);
				setState(76);
				valueContent();
				setState(77);
				match(CCLOSE_V);
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
	public static class TagBContext extends ParserRuleContext {
		public TerminalNode TSTART_B() { return getToken(TemplateMainParser.TSTART_B, 0); }
		public List<TerminalNode> TS() { return getTokens(TemplateMainParser.TS); }
		public TerminalNode TS(int i) {
			return getToken(TemplateMainParser.TS, i);
		}
		public TerminalNode TTagName() { return getToken(TemplateMainParser.TTagName, 0); }
		public TerminalNode TENDI() { return getToken(TemplateMainParser.TENDI, 0); }
		public BlockContentContext blockContent() {
			return getRuleContext(BlockContentContext.class,0);
		}
		public TerminalNode TCLOSE_B() { return getToken(TemplateMainParser.TCLOSE_B, 0); }
		public TerminalNode CSTART_B() { return getToken(TemplateMainParser.CSTART_B, 0); }
		public List<TerminalNode> CS() { return getTokens(TemplateMainParser.CS); }
		public TerminalNode CS(int i) {
			return getToken(TemplateMainParser.CS, i);
		}
		public TerminalNode CTagName() { return getToken(TemplateMainParser.CTagName, 0); }
		public TerminalNode CENDI() { return getToken(TemplateMainParser.CENDI, 0); }
		public TerminalNode CCLOSE_B() { return getToken(TemplateMainParser.CCLOSE_B, 0); }
		public TagBContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tagB; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).enterTagB(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).exitTagB(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateMainParserVisitor ) return ((TemplateMainParserVisitor<? extends T>)visitor).visitTagB(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TagBContext tagB() throws RecognitionException {
		TagBContext _localctx = new TagBContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_tagB);
		int _la;
		try {
			setState(101);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TSTART_B:
				enterOuterAlt(_localctx, 1);
				{
				setState(81);
				match(TSTART_B);
				setState(82);
				match(TS);
				setState(83);
				match(TTagName);
				setState(85);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TS) {
					{
					setState(84);
					match(TS);
					}
				}

				setState(87);
				match(TENDI);
				setState(88);
				blockContent();
				setState(89);
				match(TCLOSE_B);
				}
				break;
			case CSTART_B:
				enterOuterAlt(_localctx, 2);
				{
				setState(91);
				match(CSTART_B);
				setState(92);
				match(CS);
				setState(93);
				match(CTagName);
				setState(95);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CS) {
					{
					setState(94);
					match(CS);
					}
				}

				setState(97);
				match(CENDI);
				setState(98);
				blockContent();
				setState(99);
				match(CCLOSE_B);
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
	public static class TagBVContext extends ParserRuleContext {
		public TerminalNode TSTART_BV() { return getToken(TemplateMainParser.TSTART_BV, 0); }
		public List<TerminalNode> TS() { return getTokens(TemplateMainParser.TS); }
		public TerminalNode TS(int i) {
			return getToken(TemplateMainParser.TS, i);
		}
		public TerminalNode TTagName() { return getToken(TemplateMainParser.TTagName, 0); }
		public TerminalNode TENDI() { return getToken(TemplateMainParser.TENDI, 0); }
		public BlockContentContext blockContent() {
			return getRuleContext(BlockContentContext.class,0);
		}
		public TerminalNode TCLOSE_BV() { return getToken(TemplateMainParser.TCLOSE_BV, 0); }
		public TerminalNode CSTART_BV() { return getToken(TemplateMainParser.CSTART_BV, 0); }
		public List<TerminalNode> CS() { return getTokens(TemplateMainParser.CS); }
		public TerminalNode CS(int i) {
			return getToken(TemplateMainParser.CS, i);
		}
		public TerminalNode CTagName() { return getToken(TemplateMainParser.CTagName, 0); }
		public TerminalNode CENDI() { return getToken(TemplateMainParser.CENDI, 0); }
		public TerminalNode CCLOSE_BV() { return getToken(TemplateMainParser.CCLOSE_BV, 0); }
		public TagBVContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tagBV; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).enterTagBV(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).exitTagBV(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateMainParserVisitor ) return ((TemplateMainParserVisitor<? extends T>)visitor).visitTagBV(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TagBVContext tagBV() throws RecognitionException {
		TagBVContext _localctx = new TagBVContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_tagBV);
		int _la;
		try {
			setState(123);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TSTART_BV:
				enterOuterAlt(_localctx, 1);
				{
				setState(103);
				match(TSTART_BV);
				setState(104);
				match(TS);
				setState(105);
				match(TTagName);
				setState(107);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TS) {
					{
					setState(106);
					match(TS);
					}
				}

				setState(109);
				match(TENDI);
				setState(110);
				blockContent();
				setState(111);
				match(TCLOSE_BV);
				}
				break;
			case CSTART_BV:
				enterOuterAlt(_localctx, 2);
				{
				setState(113);
				match(CSTART_BV);
				setState(114);
				match(CS);
				setState(115);
				match(CTagName);
				setState(117);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CS) {
					{
					setState(116);
					match(CS);
					}
				}

				setState(119);
				match(CENDI);
				setState(120);
				blockContent();
				setState(121);
				match(CCLOSE_BV);
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
	public static class TagBAContext extends ParserRuleContext {
		public TerminalNode TSTART_BA() { return getToken(TemplateMainParser.TSTART_BA, 0); }
		public List<TerminalNode> TS() { return getTokens(TemplateMainParser.TS); }
		public TerminalNode TS(int i) {
			return getToken(TemplateMainParser.TS, i);
		}
		public TerminalNode TTagName() { return getToken(TemplateMainParser.TTagName, 0); }
		public TerminalNode TENDI() { return getToken(TemplateMainParser.TENDI, 0); }
		public BlockContentContext blockContent() {
			return getRuleContext(BlockContentContext.class,0);
		}
		public TerminalNode TCLOSE_BA() { return getToken(TemplateMainParser.TCLOSE_BA, 0); }
		public TerminalNode CSTART_BA() { return getToken(TemplateMainParser.CSTART_BA, 0); }
		public List<TerminalNode> CS() { return getTokens(TemplateMainParser.CS); }
		public TerminalNode CS(int i) {
			return getToken(TemplateMainParser.CS, i);
		}
		public TerminalNode CTagName() { return getToken(TemplateMainParser.CTagName, 0); }
		public TerminalNode CENDI() { return getToken(TemplateMainParser.CENDI, 0); }
		public TerminalNode CCLOSE_BA() { return getToken(TemplateMainParser.CCLOSE_BA, 0); }
		public TagBAContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tagBA; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).enterTagBA(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).exitTagBA(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateMainParserVisitor ) return ((TemplateMainParserVisitor<? extends T>)visitor).visitTagBA(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TagBAContext tagBA() throws RecognitionException {
		TagBAContext _localctx = new TagBAContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_tagBA);
		int _la;
		try {
			setState(145);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TSTART_BA:
				enterOuterAlt(_localctx, 1);
				{
				setState(125);
				match(TSTART_BA);
				setState(126);
				match(TS);
				setState(127);
				match(TTagName);
				setState(129);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TS) {
					{
					setState(128);
					match(TS);
					}
				}

				setState(131);
				match(TENDI);
				setState(132);
				blockContent();
				setState(133);
				match(TCLOSE_BA);
				}
				break;
			case CSTART_BA:
				enterOuterAlt(_localctx, 2);
				{
				setState(135);
				match(CSTART_BA);
				setState(136);
				match(CS);
				setState(137);
				match(CTagName);
				setState(139);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CS) {
					{
					setState(138);
					match(CS);
					}
				}

				setState(141);
				match(CENDI);
				setState(142);
				blockContent();
				setState(143);
				match(CCLOSE_BA);
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
	public static class BlockDataContext extends ParserRuleContext {
		public List<TerminalNode> TEXT() { return getTokens(TemplateMainParser.TEXT); }
		public TerminalNode TEXT(int i) {
			return getToken(TemplateMainParser.TEXT, i);
		}
		public BlockDataContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blockData; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).enterBlockData(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).exitBlockData(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateMainParserVisitor ) return ((TemplateMainParserVisitor<? extends T>)visitor).visitBlockData(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockDataContext blockData() throws RecognitionException {
		BlockDataContext _localctx = new BlockDataContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_blockData);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(148); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(147);
					match(TEXT);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(150); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
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
	public static class ValueDataContext extends ParserRuleContext {
		public List<TerminalNode> TEXT() { return getTokens(TemplateMainParser.TEXT); }
		public TerminalNode TEXT(int i) {
			return getToken(TemplateMainParser.TEXT, i);
		}
		public ValueDataContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valueData; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).enterValueData(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TemplateMainParserListener ) ((TemplateMainParserListener)listener).exitValueData(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TemplateMainParserVisitor ) return ((TemplateMainParserVisitor<? extends T>)visitor).visitValueData(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueDataContext valueData() throws RecognitionException {
		ValueDataContext _localctx = new ValueDataContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_valueData);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(153); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(152);
					match(TEXT);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(155); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
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
		"\u0004\u0001\u0019\u009e\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0001\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0005"+
		"\u0001\u001e\b\u0001\n\u0001\f\u0001!\t\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0005\u0002\'\b\u0002\n\u0002\f\u0002*\t\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u00030\b\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003"+
		"7\b\u0003\u0001\u0003\u0003\u0003:\b\u0003\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0003\u0004@\b\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003"+
		"\u0004J\b\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003"+
		"\u0004P\b\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003"+
		"\u0005V\b\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005`\b\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005f\b\u0005\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0003\u0006l\b\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0003\u0006v\b\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0003\u0006|\b\u0006\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u0082\b\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003"+
		"\u0007\u008c\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003"+
		"\u0007\u0092\b\u0007\u0001\b\u0004\b\u0095\b\b\u000b\b\f\b\u0096\u0001"+
		"\t\u0004\t\u009a\b\t\u000b\t\f\t\u009b\u0001\t\u0000\u0000\n\u0000\u0002"+
		"\u0004\u0006\b\n\f\u000e\u0010\u0012\u0000\u0000\u00ae\u0000\u0014\u0001"+
		"\u0000\u0000\u0000\u0002\u001f\u0001\u0000\u0000\u0000\u0004(\u0001\u0000"+
		"\u0000\u0000\u00069\u0001\u0000\u0000\u0000\bO\u0001\u0000\u0000\u0000"+
		"\ne\u0001\u0000\u0000\u0000\f{\u0001\u0000\u0000\u0000\u000e\u0091\u0001"+
		"\u0000\u0000\u0000\u0010\u0094\u0001\u0000\u0000\u0000\u0012\u0099\u0001"+
		"\u0000\u0000\u0000\u0014\u0015\u0003\u0002\u0001\u0000\u0015\u0016\u0005"+
		"\u0000\u0000\u0001\u0016\u0001\u0001\u0000\u0000\u0000\u0017\u001e\u0003"+
		"\u0010\b\u0000\u0018\u001e\u0003\u0006\u0003\u0000\u0019\u001e\u0003\b"+
		"\u0004\u0000\u001a\u001e\u0003\n\u0005\u0000\u001b\u001e\u0003\f\u0006"+
		"\u0000\u001c\u001e\u0003\u000e\u0007\u0000\u001d\u0017\u0001\u0000\u0000"+
		"\u0000\u001d\u0018\u0001\u0000\u0000\u0000\u001d\u0019\u0001\u0000\u0000"+
		"\u0000\u001d\u001a\u0001\u0000\u0000\u0000\u001d\u001b\u0001\u0000\u0000"+
		"\u0000\u001d\u001c\u0001\u0000\u0000\u0000\u001e!\u0001\u0000\u0000\u0000"+
		"\u001f\u001d\u0001\u0000\u0000\u0000\u001f \u0001\u0000\u0000\u0000 \u0003"+
		"\u0001\u0000\u0000\u0000!\u001f\u0001\u0000\u0000\u0000\"\'\u0003\u0012"+
		"\t\u0000#\'\u0003\n\u0005\u0000$\'\u0003\f\u0006\u0000%\'\u0003\u000e"+
		"\u0007\u0000&\"\u0001\u0000\u0000\u0000&#\u0001\u0000\u0000\u0000&$\u0001"+
		"\u0000\u0000\u0000&%\u0001\u0000\u0000\u0000\'*\u0001\u0000\u0000\u0000"+
		"(&\u0001\u0000\u0000\u0000()\u0001\u0000\u0000\u0000)\u0005\u0001\u0000"+
		"\u0000\u0000*(\u0001\u0000\u0000\u0000+,\u0005\u0002\u0000\u0000,-\u0005"+
		"\u0014\u0000\u0000-/\u0005\u0015\u0000\u0000.0\u0005\u0014\u0000\u0000"+
		"/.\u0001\u0000\u0000\u0000/0\u0001\u0000\u0000\u000001\u0001\u0000\u0000"+
		"\u00001:\u0005\u0013\u0000\u000023\u0005\u0004\u0000\u000034\u0005\u0018"+
		"\u0000\u000046\u0005\u0019\u0000\u000057\u0005\u0018\u0000\u000065\u0001"+
		"\u0000\u0000\u000067\u0001\u0000\u0000\u000078\u0001\u0000\u0000\u0000"+
		"8:\u0005\u0017\u0000\u00009+\u0001\u0000\u0000\u000092\u0001\u0000\u0000"+
		"\u0000:\u0007\u0001\u0000\u0000\u0000;<\u0005\u0002\u0000\u0000<=\u0005"+
		"\u0014\u0000\u0000=?\u0005\u0015\u0000\u0000>@\u0005\u0014\u0000\u0000"+
		"?>\u0001\u0000\u0000\u0000?@\u0001\u0000\u0000\u0000@A\u0001\u0000\u0000"+
		"\u0000AB\u0005\u0012\u0000\u0000BC\u0003\u0004\u0002\u0000CD\u0005\u0001"+
		"\u0000\u0000DP\u0001\u0000\u0000\u0000EF\u0005\u0004\u0000\u0000FG\u0005"+
		"\u0018\u0000\u0000GI\u0005\u0019\u0000\u0000HJ\u0005\u0018\u0000\u0000"+
		"IH\u0001\u0000\u0000\u0000IJ\u0001\u0000\u0000\u0000JK\u0001\u0000\u0000"+
		"\u0000KL\u0005\u0016\u0000\u0000LM\u0003\u0004\u0002\u0000MN\u0005\u0003"+
		"\u0000\u0000NP\u0001\u0000\u0000\u0000O;\u0001\u0000\u0000\u0000OE\u0001"+
		"\u0000\u0000\u0000P\t\u0001\u0000\u0000\u0000QR\u0005\u0006\u0000\u0000"+
		"RS\u0005\u0014\u0000\u0000SU\u0005\u0015\u0000\u0000TV\u0005\u0014\u0000"+
		"\u0000UT\u0001\u0000\u0000\u0000UV\u0001\u0000\u0000\u0000VW\u0001\u0000"+
		"\u0000\u0000WX\u0005\u0012\u0000\u0000XY\u0003\u0002\u0001\u0000YZ\u0005"+
		"\u0005\u0000\u0000Zf\u0001\u0000\u0000\u0000[\\\u0005\b\u0000\u0000\\"+
		"]\u0005\u0018\u0000\u0000]_\u0005\u0019\u0000\u0000^`\u0005\u0018\u0000"+
		"\u0000_^\u0001\u0000\u0000\u0000_`\u0001\u0000\u0000\u0000`a\u0001\u0000"+
		"\u0000\u0000ab\u0005\u0016\u0000\u0000bc\u0003\u0002\u0001\u0000cd\u0005"+
		"\u0007\u0000\u0000df\u0001\u0000\u0000\u0000eQ\u0001\u0000\u0000\u0000"+
		"e[\u0001\u0000\u0000\u0000f\u000b\u0001\u0000\u0000\u0000gh\u0005\n\u0000"+
		"\u0000hi\u0005\u0014\u0000\u0000ik\u0005\u0015\u0000\u0000jl\u0005\u0014"+
		"\u0000\u0000kj\u0001\u0000\u0000\u0000kl\u0001\u0000\u0000\u0000lm\u0001"+
		"\u0000\u0000\u0000mn\u0005\u0012\u0000\u0000no\u0003\u0002\u0001\u0000"+
		"op\u0005\t\u0000\u0000p|\u0001\u0000\u0000\u0000qr\u0005\f\u0000\u0000"+
		"rs\u0005\u0018\u0000\u0000su\u0005\u0019\u0000\u0000tv\u0005\u0018\u0000"+
		"\u0000ut\u0001\u0000\u0000\u0000uv\u0001\u0000\u0000\u0000vw\u0001\u0000"+
		"\u0000\u0000wx\u0005\u0016\u0000\u0000xy\u0003\u0002\u0001\u0000yz\u0005"+
		"\u000b\u0000\u0000z|\u0001\u0000\u0000\u0000{g\u0001\u0000\u0000\u0000"+
		"{q\u0001\u0000\u0000\u0000|\r\u0001\u0000\u0000\u0000}~\u0005\u000e\u0000"+
		"\u0000~\u007f\u0005\u0014\u0000\u0000\u007f\u0081\u0005\u0015\u0000\u0000"+
		"\u0080\u0082\u0005\u0014\u0000\u0000\u0081\u0080\u0001\u0000\u0000\u0000"+
		"\u0081\u0082\u0001\u0000\u0000\u0000\u0082\u0083\u0001\u0000\u0000\u0000"+
		"\u0083\u0084\u0005\u0012\u0000\u0000\u0084\u0085\u0003\u0002\u0001\u0000"+
		"\u0085\u0086\u0005\r\u0000\u0000\u0086\u0092\u0001\u0000\u0000\u0000\u0087"+
		"\u0088\u0005\u0010\u0000\u0000\u0088\u0089\u0005\u0018\u0000\u0000\u0089"+
		"\u008b\u0005\u0019\u0000\u0000\u008a\u008c\u0005\u0018\u0000\u0000\u008b"+
		"\u008a\u0001\u0000\u0000\u0000\u008b\u008c\u0001\u0000\u0000\u0000\u008c"+
		"\u008d\u0001\u0000\u0000\u0000\u008d\u008e\u0005\u0016\u0000\u0000\u008e"+
		"\u008f\u0003\u0002\u0001\u0000\u008f\u0090\u0005\u000f\u0000\u0000\u0090"+
		"\u0092\u0001\u0000\u0000\u0000\u0091}\u0001\u0000\u0000\u0000\u0091\u0087"+
		"\u0001\u0000\u0000\u0000\u0092\u000f\u0001\u0000\u0000\u0000\u0093\u0095"+
		"\u0005\u0011\u0000\u0000\u0094\u0093\u0001\u0000\u0000\u0000\u0095\u0096"+
		"\u0001\u0000\u0000\u0000\u0096\u0094\u0001\u0000\u0000\u0000\u0096\u0097"+
		"\u0001\u0000\u0000\u0000\u0097\u0011\u0001\u0000\u0000\u0000\u0098\u009a"+
		"\u0005\u0011\u0000\u0000\u0099\u0098\u0001\u0000\u0000\u0000\u009a\u009b"+
		"\u0001\u0000\u0000\u0000\u009b\u0099\u0001\u0000\u0000\u0000\u009b\u009c"+
		"\u0001\u0000\u0000\u0000\u009c\u0013\u0001\u0000\u0000\u0000\u0015\u001d"+
		"\u001f&(/69?IOU_eku{\u0081\u008b\u0091\u0096\u009b";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}