// Generated from java-escape by ANTLR 4.11.1

    package rife.template.antlr;

import rife.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TemplateMainParser}.
 */
public interface TemplateMainParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TemplateMainParser#document}.
	 * @param ctx the parse tree
	 */
	void enterDocument(TemplateMainParser.DocumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplateMainParser#document}.
	 * @param ctx the parse tree
	 */
	void exitDocument(TemplateMainParser.DocumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplateMainParser#blockContent}.
	 * @param ctx the parse tree
	 */
	void enterBlockContent(TemplateMainParser.BlockContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplateMainParser#blockContent}.
	 * @param ctx the parse tree
	 */
	void exitBlockContent(TemplateMainParser.BlockContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplateMainParser#valueContent}.
	 * @param ctx the parse tree
	 */
	void enterValueContent(TemplateMainParser.ValueContentContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplateMainParser#valueContent}.
	 * @param ctx the parse tree
	 */
	void exitValueContent(TemplateMainParser.ValueContentContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplateMainParser#tagV}.
	 * @param ctx the parse tree
	 */
	void enterTagV(TemplateMainParser.TagVContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplateMainParser#tagV}.
	 * @param ctx the parse tree
	 */
	void exitTagV(TemplateMainParser.TagVContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplateMainParser#tagVDefault}.
	 * @param ctx the parse tree
	 */
	void enterTagVDefault(TemplateMainParser.TagVDefaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplateMainParser#tagVDefault}.
	 * @param ctx the parse tree
	 */
	void exitTagVDefault(TemplateMainParser.TagVDefaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplateMainParser#tagB}.
	 * @param ctx the parse tree
	 */
	void enterTagB(TemplateMainParser.TagBContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplateMainParser#tagB}.
	 * @param ctx the parse tree
	 */
	void exitTagB(TemplateMainParser.TagBContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplateMainParser#tagBV}.
	 * @param ctx the parse tree
	 */
	void enterTagBV(TemplateMainParser.TagBVContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplateMainParser#tagBV}.
	 * @param ctx the parse tree
	 */
	void exitTagBV(TemplateMainParser.TagBVContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplateMainParser#tagBA}.
	 * @param ctx the parse tree
	 */
	void enterTagBA(TemplateMainParser.TagBAContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplateMainParser#tagBA}.
	 * @param ctx the parse tree
	 */
	void exitTagBA(TemplateMainParser.TagBAContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplateMainParser#blockData}.
	 * @param ctx the parse tree
	 */
	void enterBlockData(TemplateMainParser.BlockDataContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplateMainParser#blockData}.
	 * @param ctx the parse tree
	 */
	void exitBlockData(TemplateMainParser.BlockDataContext ctx);
	/**
	 * Enter a parse tree produced by {@link TemplateMainParser#valueData}.
	 * @param ctx the parse tree
	 */
	void enterValueData(TemplateMainParser.ValueDataContext ctx);
	/**
	 * Exit a parse tree produced by {@link TemplateMainParser#valueData}.
	 * @param ctx the parse tree
	 */
	void exitValueData(TemplateMainParser.ValueDataContext ctx);
}